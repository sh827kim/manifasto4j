package ai.manifesto.compiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompilerCliTest {

    @TempDir
    Path tempDir;

    @Test
    void compileCommandWritesCanonicalSchemaToFile() throws Exception {
        Path source = tempDir.resolve("todo-simple.mel");
        Files.writeString(source, """
            schema todo 1.0.0
            field count number required
            action increment halt
            """, StandardCharsets.UTF_8);

        Path output = tempDir.resolve("out/schema.json");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitCode = CompilerCli.execute(
            new String[]{"compile", "--source=" + source, "--out=" + output},
            new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8)
        );

        assertEquals(0, exitCode);
        String schemaJson = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(schemaJson.contains("\"id\":\"todo\""));
        assertTrue(schemaJson.contains("\"actions\""));
    }

    @Test
    void checkCommandReturnsNonZeroForInvalidMel() throws Exception {
        Path source = tempDir.resolve("invalid.mel");
        Files.writeString(source, "field count number\n", StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitCode = CompilerCli.execute(
            new String[]{"check", "--source=" + source},
            new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8)
        );

        assertEquals(1, exitCode);
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("Check failed"));
    }

    @Test
    void formatCommandPrintsRenderedMel() throws Exception {
        Path source = tempDir.resolve("domain.mel");
        Files.writeString(source, "domain Todo {\nstate {\ncount: number\n}\n}\n", StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitCode = CompilerCli.execute(
            new String[]{"format", "--source=" + source, "--indent=    ", "--newline=crlf"},
            new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8)
        );

        assertEquals(0, exitCode);
        String rendered = out.toString(StandardCharsets.UTF_8);
        assertTrue(rendered.contains("\r\n"));
        assertTrue(rendered.contains("    state"));
    }

    @Test
    void parseCommandPrintsProgramSummaryJson() throws Exception {
        Path source = tempDir.resolve("parse-domain.mel");
        Files.writeString(source, "domain Todo {\nstate {\ncount: number\n}\naction increment() {\n}\n}\n", StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitCode = CompilerCli.execute(
            new String[]{"parse", "--source=" + source},
            new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8)
        );

        assertEquals(0, exitCode);
        String payload = out.toString(StandardCharsets.UTF_8);
        assertTrue(payload.contains("\"ok\":true"));
        assertTrue(payload.contains("\"domain\":\"Todo\""));
        assertTrue(payload.contains("\"actionCount\":1"));
    }

    @Test
    void tokensCommandPrintsTokenJson() throws Exception {
        Path source = tempDir.resolve("tokens-domain.mel");
        Files.writeString(source, "domain Todo {\nstate {\ncount: number\n}\n}\n", StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitCode = CompilerCli.execute(
            new String[]{"tokens", "--source=" + source},
            new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8)
        );

        assertEquals(0, exitCode);
        String payload = out.toString(StandardCharsets.UTF_8);
        assertTrue(payload.contains("\"ok\":true"));
        assertTrue(payload.contains("\"kind\":\"DOMAIN\""));
        assertTrue(payload.contains("\"kind\":\"EOF\""));
    }
}
