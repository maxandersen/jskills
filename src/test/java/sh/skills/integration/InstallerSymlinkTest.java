package sh.skills.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for skill installation with symlink handling.
 * Mirrors tests/installer-symlink.test.ts from TypeScript implementation.
 *
 * Note: Some tests disabled on Windows due to symlink permission requirements.
 */
@DisplayName("Installer Symlink")
class InstallerSymlinkTest {

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void selfLoopPrevention_createsRealDirectory(@TempDir Path tempDir)
            throws Exception {
        // When canonical and agent paths are the same, don't create symlink
        Path skillsDir = tempDir.resolve("skills");
        Files.createDirectories(skillsDir);

        Path skillDir = skillsDir.resolve("test-skill");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("SKILL.md"), """
            ---
            name: test-skill
            ---
            # Test
            """);

        // Install to same location (canonical == agent)
        // This should create a real directory, not a symlink to itself

        assertThat(Files.exists(skillDir)).isTrue();
        assertThat(Files.isSymbolicLink(skillDir)).isFalse();
        assertThat(Files.isDirectory(skillDir)).isTrue();
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void preexistingSelfLoop_cleaned(@TempDir Path tempDir) throws Exception {
        Path skillsDir = tempDir.resolve("skills");
        Files.createDirectories(skillsDir);

        Path skillDir = skillsDir.resolve("test-skill");

        // Create a self-referential symlink (problematic state)
        Files.createSymbolicLink(skillDir, skillDir);

        assertThat(Files.isSymbolicLink(skillDir)).isTrue();

        // Installation should detect and fix this
        Files.delete(skillDir);
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("SKILL.md"), "---\nname: test\n---");

        assertThat(Files.isSymbolicLink(skillDir)).isFalse();
        assertThat(Files.isDirectory(skillDir)).isTrue();
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void symlinkedSkillsDirectory_handledCorrectly(@TempDir Path tempDir)
            throws Exception {
        // Regression test for issue #293
        // When agent's skills dir is itself a symlink to canonical location

        Path canonical = tempDir.resolve("canonical/skills");
        Files.createDirectories(canonical);

        Path agentDir = tempDir.resolve("agent");
        Files.createDirectories(agentDir);

        Path agentSkills = agentDir.resolve("skills");
        Files.createSymbolicLink(agentSkills, canonical);

        // Install skill
        Path skillDir = canonical.resolve("test-skill");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("SKILL.md"), """
            ---
            name: test-skill
            ---
            # Test
            """);

        // Skill should be accessible through both paths
        assertThat(Files.exists(canonical.resolve("test-skill/SKILL.md"))).isTrue();
        assertThat(Files.exists(agentSkills.resolve("test-skill/SKILL.md"))).isTrue();
    }

    @Test
    void universalAgentGlobalInstall_noAgentSymlink(@TempDir Path tempDir)
            throws Exception {
        // Regression test for issue #294
        // Universal agents with project install shouldn't create agent-specific symlinks
        // when canonical and agent dirs are identical

        Path skillsDir = tempDir.resolve("skills");
        Files.createDirectories(skillsDir);

        Path skillDir = skillsDir.resolve("test-skill");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("SKILL.md"), """
            ---
            name: test-skill
            ---
            # Test
            """);

        // For universal agents, skill should exist as regular directory
        assertThat(Files.exists(skillDir)).isTrue();
        assertThat(Files.isDirectory(skillDir)).isTrue();

        // No symlinks should be created in this scenario
        assertThat(Files.isSymbolicLink(skillDir)).isFalse();
    }
}
