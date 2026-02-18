package ai.manifesto.compiler;

import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ParseResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * KR: CompilerCli는 compile/format/check 서브커맨드를 제공하는 compiler 실행 진입점입니다.
 * EN: CompilerCli is the compiler execution entrypoint exposing compile/format/check/parse/tokens subcommands.
 */
public final class CompilerCli {
    private CompilerCli() {
    }

    public static void main(String[] args) {
        int exitCode = execute(args, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int execute(String[] args, PrintStream out, PrintStream err) {
        Objects.requireNonNull(out, "out must not be null");
        Objects.requireNonNull(err, "err must not be null");
        if (args == null || args.length == 0) {
            err.println(usage());
            return 2;
        }

        String command = args[0];
        String[] optionArgs = Arrays.copyOfRange(args, 1, args.length);
        try {
            return switch (command) {
                case "compile" -> runCompile(optionArgs, out, err);
                case "format" -> runFormat(optionArgs, out, err);
                case "check" -> runCheck(optionArgs, out, err);
                case "parse" -> runParse(optionArgs, out, err);
                case "tokens" -> runTokens(optionArgs, out, err);
                default -> {
                    err.println("Unknown command: " + command);
                    err.println(usage());
                    yield 2;
                }
            };
        } catch (IllegalArgumentException error) {
            err.println("CLI error: " + error.getMessage());
            return 2;
        }
    }

    private static int runCompile(String[] optionArgs, PrintStream out, PrintStream err) {
        CompilerCliSupport.CliOptions options = CompilerCliSupport.parseArgs(optionArgs);
        String mel = loadMel(options);
        if (options.formatOnly()) {
            String formatted = CompilerCliSupport.formatMel(mel, options.indent(), options.newline());
            writeOutput(options.outFile(), formatted, out);
            return 0;
        }

        CompilationResult result = new SimpleCompiler().compileDomain(mel, new CompileDomainOptions(null));
        if (!result.isOk()) {
            err.println("Compile failed: " + result.getError());
            return 1;
        }
        String canonicalSchema = ValidationUtils.toCanonicalJson(result.getSchema());
        writeOutput(options.outFile(), canonicalSchema, out);
        return 0;
    }

    private static int runFormat(String[] optionArgs, PrintStream out, PrintStream err) {
        CompilerCliSupport.CliOptions options = CompilerCliSupport.parseArgs(optionArgs);
        String mel = loadMel(options);
        String formatted = CompilerCliSupport.formatMel(mel, options.indent(), options.newline());
        writeOutput(options.outFile(), formatted, out);
        return 0;
    }

    private static int runCheck(String[] optionArgs, PrintStream out, PrintStream err) {
        CompilerCliSupport.CliOptions options = CompilerCliSupport.parseArgs(optionArgs);
        String mel = loadMel(options);
        CompilationResult result = new SimpleCompiler().compileDomain(mel, new CompileDomainOptions(null));
        if (!result.isOk()) {
            err.println("Check failed: " + result.getError());
            return 1;
        }
        out.println("OK");
        return 0;
    }

    private static int runParse(String[] optionArgs, PrintStream out, PrintStream err) {
        CompilerCliSupport.CliOptions options = CompilerCliSupport.parseArgs(optionArgs);
        String mel = loadMel(options);
        ParseResult result = CompilerCliSupport.parseMel(mel);
        if (result.diagnostics().stream().anyMatch(d -> d.severity() == ai.manifesto.compiler.diagnostics.DiagnosticSeverity.ERROR)) {
            String message = result.diagnostics().stream()
                .filter(d -> d.severity() == ai.manifesto.compiler.diagnostics.DiagnosticSeverity.ERROR)
                .map(d -> d.message())
                .findFirst()
                .orElse("Unknown parse error");
            err.println("Parse failed: " + message);
            return 1;
        }
        String json = CompilerCliSupport.renderParseResultJson(result);
        writeOutput(options.outFile(), json, out);
        return 0;
    }

    private static int runTokens(String[] optionArgs, PrintStream out, PrintStream err) {
        CompilerCliSupport.CliOptions options = CompilerCliSupport.parseArgs(optionArgs);
        String mel = loadMel(options);
        Lexer.LexResult result = CompilerCliSupport.tokenizeMel(mel);
        if (result.diagnostics().stream().anyMatch(d -> d.severity() == ai.manifesto.compiler.diagnostics.DiagnosticSeverity.ERROR)) {
            String message = result.diagnostics().stream()
                .filter(d -> d.severity() == ai.manifesto.compiler.diagnostics.DiagnosticSeverity.ERROR)
                .map(d -> d.message())
                .findFirst()
                .orElse("Unknown tokenize error");
            err.println("Tokenize failed: " + message);
            return 1;
        }
        String json = CompilerCliSupport.renderTokenResultJson(result);
        writeOutput(options.outFile(), json, out);
        return 0;
    }

    private static String loadMel(CompilerCliSupport.CliOptions options) {
        MelSourceLoader loader = new MelSourceLoader();
        if (options.sourceFile() != null) {
            return loader.loadFromFile(Path.of(options.sourceFile()));
        }
        return loader.loadFromClasspath(options.classpathResource());
    }

    private static void writeOutput(String outFile, String content, PrintStream out) {
        if (outFile == null || outFile.isBlank()) {
            out.println(content);
            return;
        }
        try {
            Path output = Path.of(outFile);
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(output, content, StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new IllegalArgumentException("Failed to write output file: " + outFile, error);
        }
    }

    private static String usage() {
        return "Usage: compiler-cli <compile|format|check|parse|tokens> [--source=path | --classpath=resource] "
            + "[--out=path] [--format-only] [--indent=  ] [--newline=lf|crlf]";
    }
}
