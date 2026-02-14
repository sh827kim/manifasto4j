package ai.manifesto.codegen;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenRunnerTest {

    @Test
    void runnerDispatchesToMatchingPlugin() {
        CodegenRunner runner = CodegenRunner.withDefaults();

        CodegenRequest request = new CodegenRequest(
            Map.of("state", Map.of("fields", Map.of("name", Map.of("type", "string", "required", true)))),
            "ai.manifesto.generated",
            new CodegenTarget("java-dto", "1.0")
        );

        var artifacts = runner.generate(request);
        assertEquals(1, artifacts.size());
        assertTrue(artifacts.get(0).relativePath().endsWith("StateDto.java"));
    }

    @Test
    void runnerFailsWhenNoPluginMatches() {
        CodegenRunner runner = CodegenRunner.withDefaults();

        CodegenRequest request = new CodegenRequest(
            Map.of("state", Map.of("fields", Map.of())),
            "ai.manifesto.generated",
            new CodegenTarget("kotlin-dto", "1.0")
        );

        assertThrows(IllegalArgumentException.class, () -> runner.generate(request));
    }
}
