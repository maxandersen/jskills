package sh.skills.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Security-critical path utilities.
 * Ports sanitizeName() and isSubpathSafe() from the TypeScript source.
 */
public class PathUtils {

    /**
     * Sanitize a skill name to prevent path traversal and injection.
     * Ports the TypeScript implementation from vercel-labs/skills/src/installer.ts
     * with Unicode support enhancement.
     *
     * Replaces special characters with hyphens and strips leading/trailing dots and hyphens.
     */
    public static String sanitizeName(String name) {
        if (name == null) return "unnamed-skill";

        String sanitized = name.toLowerCase()
            // Replace any sequence of characters that are NOT lowercase letters,
            // digits, dots, or underscores with a single hyphen.
            // Using \p{Ll}\p{Nd} for Unicode support (enhancement over TypeScript)
            .replaceAll("[^\\p{Ll}\\p{Nd}._]+", "-")
            // Remove leading/trailing dots and hyphens
            .replaceAll("^[.\\-]+|[.\\-]+$", "");

        // Limit to 255 chars (common filesystem limit), fallback to unnamed-skill if empty
        sanitized = sanitized.substring(0, Math.min(sanitized.length(), 255));
        return sanitized.isEmpty() ? "unnamed-skill" : sanitized;
    }

    /**
     * Verify that the resolved path is safely within the expected base directory.
     * Prevents path traversal attacks during skill extraction.
     */
    public static boolean isSubpathSafe(Path basePath, Path subPath) {
        try {
            Path normalizedBase = basePath.toRealPath();
            Path normalizedSub = subPath.normalize().toAbsolutePath();
            // Use startsWith on normalized paths
            return normalizedSub.startsWith(normalizedBase);
        } catch (IOException e) {
            // If we can't resolve the real path, use normalize only
            Path normalizedBase = basePath.normalize().toAbsolutePath();
            Path normalizedSub = subPath.normalize().toAbsolutePath();
            return normalizedSub.startsWith(normalizedBase);
        }
    }

    /**
     * Returns the XDG data home directory.
     * Follows XDG Base Directory specification.
     */
    public static Path xdgDataHome() {
        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isEmpty()) {
            return Paths.get(xdgDataHome);
        }
        return Paths.get(System.getProperty("user.home"), ".local", "share");
    }

    /**
     * Returns the XDG config home directory.
     */
    public static Path xdgConfigHome() {
        String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
        if (xdgConfigHome != null && !xdgConfigHome.isEmpty()) {
            return Paths.get(xdgConfigHome);
        }
        return Paths.get(System.getProperty("user.home"), ".config");
    }

    /**
     * Returns the global skill lock file path (~/.skill-lock.json).
     */
    public static Path globalSkillLockPath() {
        return Paths.get(System.getProperty("user.home"), ".skill-lock.json");
    }

    /**
     * Normalizes a skill path:
     * - Removes ./ prefix
     * - Converts backslashes to forward slashes
     * - Collapses multiple slashes
     * - Removes trailing slashes
     * - Resolves .. references
     */
    public static String normalizeSkillPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        // Convert backslashes to forward slashes
        String normalized = path.replace('\\', '/');

        // Remove ./ prefix
        while (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }

        // Collapse multiple slashes
        normalized = normalized.replaceAll("/+", "/");

        // Remove trailing slash
        if (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        // Resolve .. references using Path.normalize()
        try {
            java.nio.file.Path p = java.nio.file.Paths.get(normalized).normalize();
            normalized = p.toString().replace('\\', '/');
        } catch (Exception e) {
            // If path normalization fails, return as-is
        }

        return normalized;
    }
}
