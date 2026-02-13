package ai.manifesto.codegen;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaDtoCodeGeneratorTest {

    @Test
    void generateProducesStateDtoArtifact() {
        JavaDtoCodeGenerator generator = new JavaDtoCodeGenerator();

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", Map.of("type", "string", "required", true));
        fields.put("count", Map.of("type", "number", "required", false));
        fields.put("status", Map.of("type", Map.of("enum", List.of("open", "done")), "required", false));
        fields.put("tags", Map.of("type", "array", "required", false));

        Map<String, Object> schema = Map.of(
            "state", Map.of(
                "fields", fields
            )
        );

        CodegenRequest request = new CodegenRequest(
            schema,
            "ai.manifesto.generated",
            new CodegenTarget("java-dto", "1.0")
        );

        List<GeneratedArtifact> artifacts = generator.generate(request);

        assertEquals(1, artifacts.size());
        GeneratedArtifact artifact = artifacts.get(0);
        assertEquals("ai/manifesto/generated/StateDto.java", artifact.relativePath());
        assertTrue(artifact.content().contains("package ai.manifesto.generated;"));
        assertTrue(artifact.content().contains("private String id;"));
        assertTrue(artifact.content().contains("private Double count;"));
        assertTrue(artifact.content().contains("private String status;"));
        assertTrue(artifact.content().contains("private List<Object> tags;"));
        assertTrue(artifact.content().contains("Objects.requireNonNull(id"));
    }

    @Test
    void generateRejectsUnsupportedTarget() {
        JavaDtoCodeGenerator generator = new JavaDtoCodeGenerator();
        CodegenRequest request = new CodegenRequest(
            Map.of("state", Map.of("fields", Map.of())),
            "ai.manifesto.generated",
            new CodegenTarget("typescript-client", "1.0")
        );

        assertThrows(IllegalArgumentException.class, () -> generator.generate(request));
    }

    @Test
    void generateRejectsMissingStateFields() {
        JavaDtoCodeGenerator generator = new JavaDtoCodeGenerator();
        CodegenRequest request = new CodegenRequest(
            Map.of("state", Map.of()),
            "ai.manifesto.generated",
            new CodegenTarget("java-dto", "1.0")
        );

        assertThrows(IllegalArgumentException.class, () -> generator.generate(request));
    }
}
