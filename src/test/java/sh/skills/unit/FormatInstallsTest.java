package sh.skills.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sh.skills.commands.FindCommand;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for install count formatting to match upstream output.
 * Upstream format: 260837 -> "260.8K installs", 1000000 -> "1M installs"
 */
@DisplayName("formatInstalls")
class FormatInstallsTest {

    @Test
    void zeroReturnsEmpty() {
        assertThat(FindCommand.formatInstalls(0)).isEmpty();
    }

    @Test
    void negativeReturnsEmpty() {
        assertThat(FindCommand.formatInstalls(-5)).isEmpty();
    }

    @Test
    void singleInstall() {
        assertThat(FindCommand.formatInstalls(1)).isEqualTo("1 install");
    }

    @Test
    void pluralInstalls() {
        assertThat(FindCommand.formatInstalls(42)).isEqualTo("42 installs");
    }

    @Test
    void thousandsWithDecimal() {
        assertThat(FindCommand.formatInstalls(10058)).isEqualTo("10.1K installs");
    }

    @Test
    void thousandsRoundNumber() {
        assertThat(FindCommand.formatInstalls(6000)).isEqualTo("6K installs");
    }

    @Test
    void hundredThousands() {
        assertThat(FindCommand.formatInstalls(260837)).isEqualTo("260.8K installs");
    }

    @Test
    void millions() {
        assertThat(FindCommand.formatInstalls(1500000)).isEqualTo("1.5M installs");
    }

    @Test
    void millionsRound() {
        assertThat(FindCommand.formatInstalls(1000000)).isEqualTo("1M installs");
    }

    @Test
    void hundredNinetyNine() {
        assertThat(FindCommand.formatInstalls(999)).isEqualTo("999 installs");
    }
}
