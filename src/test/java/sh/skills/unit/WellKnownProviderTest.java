package sh.skills.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sh.skills.providers.WellKnownProvider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for well-known provider URL matching.
 * Driven by test-fixtures/wellknown-provider-cases.json
 */
@DisplayName("WellKnown Provider")
class WellKnownProviderTest {

    record TestCase(String description, String url, boolean shouldMatch) {}

    static Stream<TestCase> loadCases() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = WellKnownProviderTest.class
            .getResourceAsStream("/test-fixtures/wellknown-provider-cases.json");
        if (is == null) {
            is = java.nio.file.Files.newInputStream(
                java.nio.file.Paths.get("test-fixtures/wellknown-provider-cases.json"));
        }
        JsonNode root = mapper.readTree(is);
        List<TestCase> cases = new ArrayList<>();

        for (JsonNode c : root.get("cases")) {
            cases.add(new TestCase(
                c.get("description").asText(),
                c.get("url").asText(),
                c.get("shouldMatch").asBoolean()
            ));
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loadCases")
    void testWellKnownMatching(TestCase tc) {
        WellKnownProvider provider = new WellKnownProvider();
        boolean matches = provider.matches(tc.url());

        assertThat(matches)
            .as(tc.description())
            .isEqualTo(tc.shouldMatch());
    }
}
