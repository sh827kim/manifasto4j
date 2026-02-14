package ai.manifesto.compiler;

import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.renderer.MelRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * KR: CompilerCliSupport는 compiler CLI에서 사용하는 인자 파싱/포맷팅 보조 유틸입니다.
 * EN: CompilerCliSupport provides argument parsing and formatting helpers for compiler CLI usage.
 */
public final class CompilerCliSupport {
    private CompilerCliSupport() {
    }

    public static CliOptions parseArgs(String[] args) {
        String sourceFile = null;
        String classpathResource = null;
        String outFile = null;
        boolean formatOnly = false;
        String indent = "  ";
        String newline = "\n";

        List<String> unknown = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (arg == null || arg.isBlank()) {
                    continue;
                }
                if (arg.startsWith("--source=")) {
                    sourceFile = arg.substring("--source=".length());
                } else if (arg.startsWith("--classpath=")) {
                    classpathResource = arg.substring("--classpath=".length());
                } else if (arg.startsWith("--out=")) {
                    outFile = arg.substring("--out=".length());
                } else if ("--format-only".equals(arg)) {
                    formatOnly = true;
                } else if (arg.startsWith("--indent=")) {
                    indent = arg.substring("--indent=".length());
                } else if (arg.startsWith("--newline=")) {
                    String value = arg.substring("--newline=".length());
                    newline = "crlf".equalsIgnoreCase(value) ? "\r\n" : "\n";
                } else {
                    unknown.add(arg);
                }
            }
        }

        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Unknown CLI arguments: " + String.join(", ", unknown));
        }
        if (sourceFile != null && classpathResource != null) {
            throw new IllegalArgumentException("Use only one input source: --source or --classpath");
        }
        if (sourceFile == null && classpathResource == null) {
            throw new IllegalArgumentException("Input source is required: --source or --classpath");
        }
        if (indent.isEmpty()) {
            throw new IllegalArgumentException("indent must not be empty");
        }

        return new CliOptions(sourceFile, classpathResource, outFile, formatOnly, indent, newline);
    }

    public static String formatMel(String melText, String indent, String newline) {
        Objects.requireNonNull(melText, "melText must not be null");
        Lexer.LexResult lex = new Lexer(melText).tokenize();
        ParseResult parsed = new Parser(lex.tokens()).parse();
        if (parsed.program() == null) {
            throw new IllegalArgumentException("Failed to parse MEL for formatting");
        }
        return MelRenderer.renderProgram(parsed.program(), new MelRenderer.RenderOptions(indent, newline));
    }

    public record CliOptions(
        String sourceFile,
        String classpathResource,
        String outFile,
        boolean formatOnly,
        String indent,
        String newline
    ) {
    }
}
