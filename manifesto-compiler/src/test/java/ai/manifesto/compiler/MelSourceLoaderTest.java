package ai.manifesto.compiler;

import ai.manifesto.core.schema.DomainSchema;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MelSourceLoaderTest {

    private static final String SIMPLE_MEL = String.join("\n",
        "schema urn:test.loader 1.0.0",
        "field count number required",
        "action increment halt"
    );

    @Test
    void loadAndCompileFromFileWorks() throws Exception {
        MelSourceLoader loader = new MelSourceLoader();
        Path temp = Files.createTempFile("manifesto-loader-", ".mel");
        try {
            Files.writeString(temp, SIMPLE_MEL);

            String loaded = loader.loadFromFile(temp);
            assertTrue(loaded.contains("schema urn:test.loader 1.0.0"));

            CompilationResult result = loader.compileFromFile(new SimpleCompiler(), temp, new CompileDomainOptions(null));
            assertTrue(result.isOk());
            assertEquals("urn:test.loader", result.getSchema().getId());
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    void compileFromClasspathOrThrowWorks() {
        MelSourceLoader loader = new MelSourceLoader();
        DomainSchema schema = loader.compileFromClasspathOrThrow(
            new SimpleCompiler(),
            "golden/loader/sample-loader-domain.mel",
            new CompileDomainOptions(null)
        );

        assertEquals("urn:test.loader.classpath", schema.getId());
        assertTrue(schema.getActions().containsKey("ship"));
    }

    @Test
    void loadFromMissingClasspathThrows() {
        MelSourceLoader loader = new MelSourceLoader();
        assertThrows(IllegalArgumentException.class, () -> loader.loadFromClasspath("missing/not-found.mel"));
    }
}
