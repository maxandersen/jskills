package sh.skills.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import sh.skills.commands.ListCommand;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ListCommand output format matching upstream.
 * Upstream groups by skill name (not agent), shows path and agents.
 */
@DisplayName("ListCommand output format")
class ListCommandTest {

    @Test
    @DisplayName("should show scope header (Project Skills / Global Skills)")
    void showsScopeHeader(@TempDir Path tempDir) {
        // Set up a skill in a .claude/skills dir
        Path skillDir = tempDir.resolve(".claude/skills/my-skill");
        createSkill(skillDir, "my-skill", "A test skill");

        String output = runList(tempDir);
        assertThat(output).contains("Project Skills");
    }

    @Test
    @DisplayName("should show skill name with path")
    void showsSkillNameWithPath(@TempDir Path tempDir) {
        Path skillDir = tempDir.resolve(".claude/skills/my-skill");
        createSkill(skillDir, "my-skill", "A test skill");

        String output = runList(tempDir);
        assertThat(output).contains("my-skill");
        // Should show a path (shortened)
        assertThat(output).contains(".claude/skills/my-skill");
    }

    @Test
    @DisplayName("should show agents for each skill")
    void showsAgentsForSkill(@TempDir Path tempDir) {
        Path skillDir = tempDir.resolve(".claude/skills/my-skill");
        createSkill(skillDir, "my-skill", "A test skill");

        String output = runList(tempDir);
        assertThat(output).contains("Agents:");
        assertThat(output).contains("Claude Code");
    }

    @Test
    @DisplayName("should show description")
    void showsDescription(@TempDir Path tempDir) {
        Path skillDir = tempDir.resolve(".claude/skills/my-skill");
        createSkill(skillDir, "my-skill", "A very useful test skill");

        String output = runList(tempDir);
        assertThat(output).contains("A very useful test skill");
    }

    @Test
    @DisplayName("should deduplicate same skill across agents into one entry")
    void deduplicatesSkills(@TempDir Path tempDir) {
        // Same skill in both .claude and .pi
        createSkill(tempDir.resolve(".claude/skills/shared-skill"), "shared-skill", "Shared");
        createSkill(tempDir.resolve(".pi/skills/shared-skill"), "shared-skill", "Shared");

        String output = runList(tempDir);
        // Should appear once with both agents listed
        long nameLines = output.lines()
            .filter(l -> l.contains("shared-skill") && !l.contains("Agents:"))
            .count();
        assertThat(nameLines).as("skill should appear only once (deduped by name)").isEqualTo(1);
        // Agents line should list both
        assertThat(output).contains("Claude Code");
        assertThat(output).contains("Pi");
    }

    @Test
    @DisplayName("should find skills in OpenClaw's 'skills/' directory")
    void findsSkillsInOpenClawDir(@TempDir Path tempDir) {
        // OpenClaw uses bare 'skills/' not '.openclaw/skills/'
        createSkill(tempDir.resolve("skills/my-openclaw-skill"), "my-openclaw-skill", "An openclaw skill");

        String output = runList(tempDir);
        assertThat(output).contains("my-openclaw-skill");
    }

    private void createSkill(Path dir, String name, String description) {
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("SKILL.md"),
                "---\nname: " + name + "\ndescription: " + description + "\n---\n# " + name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String runList(Path projectDir) {
        String origDir = System.getProperty("user.dir");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(out));
        System.setProperty("user.dir", projectDir.toString());
        try {
            new CommandLine(new ListCommand()).execute();
        } finally {
            System.setOut(oldOut);
            System.setProperty("user.dir", origDir);
        }
        return out.toString();
    }
}
