package ai.manifesto.compiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompilerCliSupportTest {

    @Test
    void parseArgsSupportsSourceAndFormatterOptions() {
        CompilerCliSupport.CliOptions options = CompilerCliSupport.parseArgs(new String[]{
            "--source=/tmp/sample.mel",
            "--out=/tmp/out.json",
            "--format-only",
            "--indent=    ",
            "--newline=crlf"
        });

        assertEquals("/tmp/sample.mel", options.sourceFile());
        assertEquals("/tmp/out.json", options.outFile());
        assertTrue(options.formatOnly());
        assertEquals("    ", options.indent());
        assertEquals("\r\n", options.newline());
    }

    @Test
    void parseArgsRejectsInvalidInputCombination() {
        assertThrows(
            IllegalArgumentException.class,
            () -> CompilerCliSupport.parseArgs(new String[]{"--source=a.mel", "--classpath=b.mel"})
        );
    }

    @Test
    void formatMelUsesRendererOptions() {
        String mel = "domain Todo {\nstate {\ncount: number\n}\n}\n";
        String rendered = CompilerCliSupport.formatMel(mel, "    ", "\r\n");
        assertTrue(rendered.contains("\r\n"));
        assertTrue(rendered.contains("    state"));
    }
}
