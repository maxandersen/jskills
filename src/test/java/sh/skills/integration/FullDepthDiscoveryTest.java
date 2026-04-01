package sh.skills.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sh.skills.model.Skill;
import sh.skills.providers.SkillDiscovery;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for full-depth skill discovery.
 * Mirrors tests/full-depth-discovery.test.ts from TypeScript implementation.
 */
@DisplayName("Full Depth Discovery")
class FullDepthDiscoveryTest {

    @Test
    void fullDepthFalse_onlyRootSkill(@TempDir Path tempDir) throws Exception {
        // Create root skill
        createSkill(tempDir, "root-skill", "Root Skill");

        // Create nested skills
        Path sub1 = tempDir.resolve("sub1");
        Files.createDirectories(sub1);
        createSkill(sub1, "nested-1", "Nested 1");

        Path sub2 = tempDir.resolve("sub2");
        Files.createDirectories(sub2);
        createSkill(sub2, "nested-2", "Nested 2");

        SkillDiscovery discovery = new SkillDiscovery();
        List<Skill> skills = discovery.discover(tempDir, null, false);

        assertThat(skills).hasSize(1);
        assertThat(skills.get(0).getName()).isEqualTo("root-skill");
    }

    @Test
    void fullDepthTrue_allSkillsDiscovered(@TempDir Path tempDir) throws Exception {
        // Create root skill
        createSkill(tempDir, "root-skill", "Root Skill");

        // Create nested skills
        Path sub1 = tempDir.resolve("sub1");
        Files.createDirectories(sub1);
        createSkill(sub1, "nested-1", "Nested 1");

        Path sub2 = tempDir.resolve("sub2");
        Files.createDirectories(sub2);
        createSkill(sub2, "nested-2", "Nested 2");

        SkillDiscovery discovery = new SkillDiscovery();
        List<Skill> skills = discovery.discover(tempDir, null, true);

        assertThat(skills).hasSize(3);
        assertThat(skills).extracting(Skill::getName)
            .containsExactlyInAnyOrder("root-skill", "nested-1", "nested-2");
    }

    @Test
    void noRootSkill_findsNestedRegardlessOfDepth(@TempDir Path tempDir)
            throws Exception {
        // Create only nested skills, no root SKILL.md
        Path sub1 = tempDir.resolve("sub1");
        Files.createDirectories(sub1);
        createSkill(sub1, "nested-1", "Nested 1");

        Path sub2 = tempDir.resolve("sub2");
        Files.createDirectories(sub2);
        createSkill(sub2, "nested-2", "Nested 2");

        SkillDiscovery discovery = new SkillDiscovery();

        // Even with fullDepth=false, should find nested when no root exists
        List<Skill> skills = discovery.discover(tempDir, null, false);

        assertThat(skills).hasSize(2);
        assertThat(skills).extracting(Skill::getName)
            .containsExactlyInAnyOrder("nested-1", "nested-2");
    }

    @Test
    void deduplication_sameNameOnlyOnce(@TempDir Path tempDir) throws Exception {
        // Create root skill
        createSkill(tempDir, "duplicate-name", "Root");

        // Create nested skill with same name
        Path sub = tempDir.resolve("sub");
        Files.createDirectories(sub);
        createSkill(sub, "duplicate-name", "Nested");

        SkillDiscovery discovery = new SkillDiscovery();
        List<Skill> skills = discovery.discover(tempDir, null, true);

        // Should only return one skill with this name
        assertThat(skills).hasSize(1);
        assertThat(skills.get(0).getName()).isEqualTo("duplicate-name");
    }

    // Helper method
    private void createSkill(Path parentDir, String name, String description)
            throws Exception {
        Path skillMd = parentDir.resolve("SKILL.md");
        String content = String.format("""
            ---
            name: %s
            description: %s
            ---
            # %s
            """, name, description, name);
        Files.writeString(skillMd, content);
    }
}
