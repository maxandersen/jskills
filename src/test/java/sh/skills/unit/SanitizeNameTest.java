package sh.skills.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sh.skills.util.PathUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for name sanitization logic.
 * Driven by test-fixtures/sanitize-name-cases.json
 */
@DisplayName("Sanitize Name")
class SanitizeNameTest {

    record TestCase(String description, String input, String expected) {}

    static Stream<TestCase> loadCases() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = SanitizeNameTest.class
            .getResourceAsStream("/test-fixtures/sanitize-name-cases.json");
        if (is == null) {
            is = java.nio.file.Files.newInputStream(
                java.nio.file.Paths.get("test-fixtures/sanitize-name-cases.json"));
        }
        JsonNode root = mapper.readTree(is);
        List<TestCase> cases = new ArrayList<>();

        for (JsonNode c : root.get("cases")) {
            cases.add(new TestCase(
                c.get("description").asText(),
                c.get("input").asText(),
                c.get("expected").asText()
            ));
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loadCases")
    void testSanitizeName(TestCase tc) {
        String result = PathUtils.sanitizeName(tc.input());
        assertThat(result)
            .as(tc.description())
            .isEqualTo(tc.expected());
    }
}
