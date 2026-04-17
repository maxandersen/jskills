package sh.skills.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import sh.skills.commands.ListCommand;
import sh.skills.model.SkillLockEntry;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that list groups skills by pluginName from lock entries.
 */
@DisplayName("Plugin grouping in list")
class PluginGroupingTest {

    @Test
    @DisplayName("pluginName field should serialize/deserialize in SkillLockEntry")
    void pluginNameRoundTrips() throws Exception {
        SkillLockEntry entry = new SkillLockEntry("owner/repo", "github",
            "https://github.com/owner/repo.git", null, null, "abc123");
        entry.setPluginName("my-plugin");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(entry);
        assertThat(json).contains("\"pluginName\"");
        assertThat(json).contains("my-plugin");

        SkillLockEntry deserialized = mapper.readValue(json, SkillLockEntry.class);
        assertThat(deserialized.getPluginName()).isEqualTo("my-plugin");
    }

    @Test
    @DisplayName("list should show plugin group headers when pluginName is set")
    void showsPluginGroupHeaders(@TempDir Path tempDir) throws Exception {
        // Create two skills
        createSkill(tempDir.resolve(".claude/skills/skill-a"), "skill-a", "First skill");
        createSkill(tempDir.resolve(".claude/skills/skill-b"), "skill-b", "Second skill");
        createSkill(tempDir.resolve(".claude/skills/skill-c"), "skill-c", "Ungrouped skill");

        // Create a local-lock.json with pluginName for skill-a and skill-b
        Map<String, Object> lock = new LinkedHashMap<>();
        Map<String, Object> entryA = new LinkedHashMap<>();
        entryA.put("source", "owner/repo");
        entryA.put("pluginName", "my-awesome-plugin");
        lock.put("claude-code:skill-a", entryA);
        Map<String, Object> entryB = new LinkedHashMap<>();
        entryB.put("source", "owner/repo");
        entryB.put("pluginName", "my-awesome-plugin");
        lock.put("claude-code:skill-b", entryB);

        Files.writeString(tempDir.resolve("local-lock.json"),
            new ObjectMapper().writeValueAsString(lock));

        String output = runList(tempDir);
        // Should have group header (kebab-case to Title Case)
        assertThat(output).contains("My Awesome Plugin");
        // Should have "General" for ungrouped
        assertThat(output).contains("General");
    }

    @Test
    @DisplayName("list without any pluginName should show flat list (no General header)")
    void noGroupingWithoutPluginName(@TempDir Path tempDir) {
        createSkill(tempDir.resolve(".claude/skills/my-skill"), "my-skill", "A skill");

        String output = runList(tempDir);
        // No "General" header when there are no groups at all
        assertThat(output).doesNotContain("General");
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
