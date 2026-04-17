package sh.skills.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import sh.skills.Skills;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that command aliases match upstream CLI.
 */
@DisplayName("Command aliases")
class CommandAliasTest {

    private CommandLine cli() {
        return new CommandLine(new Skills());
    }

    @Test @DisplayName("find has aliases: search, f, s")
    void findAliases() {
        var sub = cli().getSubcommands();
        assertThat(sub).containsKey("find");
        assertThat(sub).containsKey("search");
        assertThat(sub).containsKey("f");
        assertThat(sub).containsKey("s");
    }

    @Test @DisplayName("add has aliases: install, a, i")
    void addAliases() {
        var sub = cli().getSubcommands();
        assertThat(sub).containsKey("add");
        assertThat(sub).containsKey("install");
        assertThat(sub).containsKey("a");
        assertThat(sub).containsKey("i");
    }

    @Test @DisplayName("remove has aliases: rm, r")
    void removeAliases() {
        var sub = cli().getSubcommands();
        assertThat(sub).containsKey("remove");
        assertThat(sub).containsKey("rm");
        assertThat(sub).containsKey("r");
    }

    @Test @DisplayName("list has alias: ls")
    void listAliases() {
        var sub = cli().getSubcommands();
        assertThat(sub).containsKey("list");
        assertThat(sub).containsKey("ls");
    }

    @Test @DisplayName("update has aliases: upgrade, check")
    void updateAliases() {
        var sub = cli().getSubcommands();
        assertThat(sub).containsKey("update");
        assertThat(sub).containsKey("upgrade");
        assertThat(sub).containsKey("check");
    }
}
