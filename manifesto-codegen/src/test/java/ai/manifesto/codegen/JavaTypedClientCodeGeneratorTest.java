package ai.manifesto.codegen;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaTypedClientCodeGeneratorTest {

    @Test
    void generateProducesClientAndActionInputArtifacts() {
        JavaTypedClientCodeGenerator generator = new JavaTypedClientCodeGenerator();
        Map<String, Object> schema = Map.of(
            "id", "urn:todo",
            "actions", Map.of(
                "createTask", Map.of(
                    "input", Map.of(
                        "fields", Map.of(
                            "title", Map.of("type", "string", "required", true),
                            "priority", Map.of("type", "number", "required", false)
                        )
                    )
                ),
                "closeTask", Map.of(
                    "input", Map.of(
                        "fields", Map.of(
                            "taskId", Map.of("type", "string", "required", true)
                        )
                    )
                )
            )
        );
        CodegenRequest request = new CodegenRequest(
            schema,
            "ai.manifesto.generated.client",
            new CodegenTarget("java-typed-client", "1.0")
        );

        List<GeneratedArtifact> artifacts = generator.generate(request);

        assertEquals(3, artifacts.size());
        GeneratedArtifact client = artifacts.get(0);
        assertEquals("ai/manifesto/generated/client/TodoClient.java", client.relativePath());
        assertTrue(client.content().contains("public interface TodoClient"));
        assertTrue(client.content().contains("void closeTask(CloseTaskInput input);"));
        assertTrue(client.content().contains("void createTask(CreateTaskInput input);"));

        GeneratedArtifact closeInput = artifacts.get(1);
        assertEquals("ai/manifesto/generated/client/CloseTaskInput.java", closeInput.relativePath());
        assertTrue(closeInput.content().contains("private String taskId;"));

        GeneratedArtifact createInput = artifacts.get(2);
        assertEquals("ai/manifesto/generated/client/CreateTaskInput.java", createInput.relativePath());
        assertTrue(createInput.content().contains("private String title;"));
        assertTrue(createInput.content().contains("private Double priority;"));
        assertTrue(createInput.content().contains("Objects.requireNonNull(title"));
    }

    @Test
    void generateRejectsMissingActions() {
        JavaTypedClientCodeGenerator generator = new JavaTypedClientCodeGenerator();
        CodegenRequest request = new CodegenRequest(
            Map.of("id", "urn:todo"),
            "ai.manifesto.generated.client",
            new CodegenTarget("java-typed-client", "1.0")
        );

        assertThrows(IllegalArgumentException.class, () -> generator.generate(request));
    }
}
