package ai.manifesto.compiler;

import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.lexer.Token;
import ai.manifesto.compiler.parser.ActionNode;
import ai.manifesto.compiler.parser.ComputedNode;
import ai.manifesto.compiler.parser.DomainMember;
import ai.manifesto.compiler.parser.DomainNode;
import ai.manifesto.compiler.parser.ImportNode;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.parser.StateNode;
import ai.manifesto.compiler.renderer.MelRenderer;
import ai.manifesto.core.core.ValidationUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public static Lexer.LexResult tokenizeMel(String melText) {
        return new Lexer(melText == null ? "" : melText).tokenize();
    }

    public static ParseResult parseMel(String melText) {
        Lexer.LexResult lexResult = tokenizeMel(melText);
        if (hasErrors(lexResult.diagnostics())) {
            return new ParseResult(null, List.copyOf(lexResult.diagnostics()));
        }
        ParseResult parsed = new Parser(lexResult.tokens()).parse();
        List<ai.manifesto.compiler.diagnostics.Diagnostic> diagnostics = new ArrayList<>(lexResult.diagnostics());
        diagnostics.addAll(parsed.diagnostics());
        return new ParseResult(parsed.program(), List.copyOf(diagnostics));
    }

    public static String renderParseResultJson(ParseResult result) {
        Objects.requireNonNull(result, "result must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", result.program() != null && !hasErrors(result.diagnostics()));
        payload.put("diagnostics", diagnosticsPayload(result.diagnostics()));
        if (result.program() != null) {
            payload.put("program", programSummary(result.program()));
        }
        return ValidationUtils.toCanonicalJson(payload);
    }

    public static String renderTokenResultJson(Lexer.LexResult result) {
        Objects.requireNonNull(result, "result must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", !hasErrors(result.diagnostics()));
        payload.put("diagnostics", diagnosticsPayload(result.diagnostics()));
        payload.put("tokens", tokensPayload(result.tokens()));
        return ValidationUtils.toCanonicalJson(payload);
    }

    private static boolean hasErrors(List<ai.manifesto.compiler.diagnostics.Diagnostic> diagnostics) {
        return diagnostics != null && diagnostics.stream().anyMatch(d ->
            d.severity() == ai.manifesto.compiler.diagnostics.DiagnosticSeverity.ERROR
        );
    }

    private static List<Map<String, Object>> diagnosticsPayload(
        List<ai.manifesto.compiler.diagnostics.Diagnostic> diagnostics
    ) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (ai.manifesto.compiler.diagnostics.Diagnostic diagnostic : diagnostics) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("severity", String.valueOf(diagnostic.severity()));
            item.put("code", String.valueOf(diagnostic.code()));
            item.put("message", diagnostic.message());
            if (diagnostic.span() != null) {
                item.put("line", diagnostic.span().line());
                item.put("column", diagnostic.span().column());
                item.put("length", diagnostic.span().length());
            }
            items.add(item);
        }
        return List.copyOf(items);
    }

    private static List<Map<String, Object>> tokensPayload(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Token token : tokens) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("kind", String.valueOf(token.kind()));
            item.put("lexeme", token.lexeme());
            if (token.value() != null) {
                item.put("value", token.value());
            }
            if (token.location() != null && token.location().start() != null) {
                item.put("line", token.location().start().line());
                item.put("column", token.location().start().column());
            }
            items.add(item);
        }
        return List.copyOf(items);
    }

    private static Map<String, Object> programSummary(ProgramNode program) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("importCount", program.imports() == null ? 0 : program.imports().size());
        payload.put("imports", importSummary(program.imports()));

        DomainNode domain = program.domain();
        if (domain != null) {
            payload.put("domain", domain.name());
            payload.put("typeDeclCount", domain.types() == null ? 0 : domain.types().size());
            payload.put("memberCount", domain.members() == null ? 0 : domain.members().size());
            payload.put("stateFieldCount", stateFieldCount(domain.members()));
            payload.put("actionCount", countMembers(domain.members(), ActionNode.class));
            payload.put("computedCount", countMembers(domain.members(), ComputedNode.class));
        }
        return payload;
    }

    private static List<Map<String, Object>> importSummary(List<ImportNode> imports) {
        if (imports == null || imports.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (ImportNode importNode : imports) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("from", importNode.from());
            item.put("names", importNode.names() == null ? List.of() : importNode.names());
            items.add(item);
        }
        return List.copyOf(items);
    }

    private static long countMembers(List<DomainMember> members, Class<?> type) {
        if (members == null || members.isEmpty()) {
            return 0L;
        }
        return members.stream().filter(type::isInstance).count();
    }

    private static long stateFieldCount(List<DomainMember> members) {
        if (members == null || members.isEmpty()) {
            return 0L;
        }
        long count = 0L;
        for (DomainMember member : members) {
            if (member instanceof StateNode stateNode && stateNode.fields() != null) {
                count += stateNode.fields().size();
            }
        }
        return count;
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
