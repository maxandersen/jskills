# sanitizeName() Conflict Analysis

## Summary

There are **two conflicting test suites** for `PathUtils.sanitizeName()`:

1. **SubpathTraversalTest** (with subpath-traversal-cases.json) - 5 failures
2. **SanitizeNameTest** (with sanitize-name-cases.json) - all passing

The current Java implementation matches SanitizeNameTest but breaks SubpathTraversalTest.

## TypeScript Original Implementation

From `vercel-labs/skills/src/installer.ts`:

```typescript
export function sanitizeName(name: string): string {
  const sanitized = name
    .toLowerCase()
    // Replace any sequence of characters that are NOT lowercase letters (a-z),
    // digits (0-9), dots (.), or underscores (_) with a single hyphen.
    // This converts spaces, special chars, and path traversal attempts (../) into hyphens.
    .replace(/[^a-z0-9._]+/g, '-')
    // Remove leading/trailing dots and hyphens to prevent hidden files (.) and
    // ensure clean directory names.
    .replace(/^[.\-]+|[.\-]+$/g, '');

  // Limit to 255 chars (common filesystem limit), fallback to 'unnamed-skill' if empty
  return sanitized.substring(0, 255) || 'unnamed-skill';
}
```

## Behavior Verification

Testing the TypeScript implementation:

| Input | TypeScript Output | SubpathTraversalTest Expects | SanitizeNameTest Expects |
|-------|-------------------|------------------------------|--------------------------|
| `foo/bar` | `foo-bar` | `bar` ❌ | `foobar` ❌ |
| `foo\bar` | `foo-bar` | `bar` ❌ | N/A |
| `../etc/passwd` | `etc-passwd` | `passwd` ❌ | N/A |
| `.hidden-skill` | `hidden-skill` | `hidden-skill` ✅ | N/A |
| `my skill@v2!` | `my-skill-v2` | `my-skill-v2` ✅ | N/A |
| `my/skill` | `my-skill` | N/A | `myskill` ❌ |
| `my@skill!` | `my-skill` | N/A | `myskill` ❌ |
| `my.skill` | `my.skill` | N/A | `my.skill` ✅ |
| `my-skill-café` | `my-skill-caf` | N/A | `my-skill-café` ❌ |

## Key Findings

### 1. SubpathTraversalTest is WRONG

The expectations in `subpath-traversal-cases.json` for sanitizeName are **incorrect**:

- It expects `foo/bar` → `bar` (takes last path component)
- It expects `../etc/passwd` → `passwd` (extracts filename)

This describes a **different function** (like `basename()` or path extraction), NOT what `sanitizeName()` actually does.

The TypeScript implementation **replaces** special characters with hyphens, it does NOT extract path components.

### 2. SanitizeNameTest is MOSTLY WRONG

The expectations in `sanitize-name-cases.json` are also incorrect:

- It expects `my/skill` → `myskill` (removes slashes)
- It expects `my@skill!` → `myskill` (removes special chars)

The TypeScript implementation **replaces** these with hyphens:
- `my/skill` → `my-skill`
- `my@skill!` → `my-skill`

### 3. Unicode Handling Discrepancy

- **TypeScript**: `/[^a-z0-9._]+/` - only ASCII lowercase letters, removes Unicode
  - `café` → `caf`
- **Java current**: Uses `\p{L}\p{N}` - preserves Unicode
  - `café` → `café`
- **SanitizeNameTest expects**: Unicode preserved (`my-skill-café`)

This appears to be an **intentional Java enhancement** to support international characters.

### 4. Current Java Implementation Issues

The current Java implementation:

```java
// Remove slashes and other special characters
// Keep only alphanumeric (including unicode), hyphens, underscores, and dots
name = name.replaceAll("[^\\p{L}\\p{N}\\-_.]", "");
```

This **REMOVES** special characters instead of **REPLACING** them with hyphens, which is wrong.

## Root Cause

The test expectations in `subpath-traversal-cases.json` were written **without consulting the TypeScript source**. They describe idealized security behavior (extracting filenames, stripping traversal attempts) rather than the actual implementation (replacing special chars with hyphens).

## Solution: Fix Implementation + Both Test Suites

The correct approach is:

### Option 1: Match TypeScript Exactly (Recommended)

Update Java implementation to:
1. Replace `[^a-z0-9._]+` with `-` (not remove)
2. Strip leading/trailing dots and hyphens
3. Only allow ASCII (like TypeScript), OR
4. Enhance to allow Unicode (better for Java)

Update BOTH test suites to match the correct behavior.

### Recommended Implementation (with Unicode support)

```java
public static String sanitizeName(String name) {
    if (name == null) return "unnamed-skill";

    String sanitized = name.toLowerCase()
        // Replace special chars with hyphen (keep letters, digits, dots, underscores)
        // Using \p{L}\p{N} for Unicode support (enhancement over TypeScript)
        .replaceAll("[^\\p{Ll}\\p{Nd}._]+", "-")
        // Strip leading/trailing dots and hyphens
        .replaceAll("^[.\\-]+|[.\\-]+$", "");

    // Limit to 255 chars, fallback to unnamed-skill if empty
    sanitized = sanitized.substring(0, Math.min(sanitized.length(), 255));
    return sanitized.isEmpty() ? "unnamed-skill" : sanitized;
}
```

### Fix SubpathTraversalTest

Update `subpath-traversal-cases.json` sanitizeName section:

```json
{
  "description": "strips path separator /",
  "input": "foo/bar",
  "expectedOutput": "foo-bar"  // NOT "bar"
},
{
  "description": "strips backslash on Windows",
  "input": "foo\\bar",
  "expectedOutput": "foo-bar"  // NOT "bar"
},
{
  "description": "strips .. traversal",
  "input": "../etc/passwd",
  "expectedOutput": "etc-passwd"  // NOT "passwd"
},
{
  "description": "replaces special chars with hyphen",
  "input": "my skill@v2!",
  "expectedOutput": "my-skill-v2"  // This one is correct
}
```

### Fix SanitizeNameTest

Update `sanitize-name-cases.json`:

```json
{
  "description": "slashes replaced with hyphens",
  "input": "my/skill",
  "expected": "my-skill"  // NOT "myskill"
},
{
  "description": "special chars replaced with hyphens",
  "input": "my@skill!",
  "expected": "my-skill"  // NOT "myskill"
}
```

## Verification

After fixes, all these should pass:

```javascript
sanitizeName("foo/bar") === "foo-bar"
sanitizeName("../etc/passwd") === "etc-passwd"
sanitizeName(".hidden-skill") === "hidden-skill"
sanitizeName("my skill@v2!") === "my-skill-v2"
sanitizeName("my.skill") === "my.skill"
sanitizeName("my_skill") === "my_skill"
sanitizeName("my-skill-café") === "my-skill-café"  // Java Unicode enhancement
sanitizeName("") === "unnamed-skill"
sanitizeName("...") === "unnamed-skill"
```

## Recommendation

**Implement Option 1**: Fix the implementation to match TypeScript behavior (with Unicode enhancement), then update both test suites to have correct expectations.

This is the only way to properly port the TypeScript function and have accurate tests.
