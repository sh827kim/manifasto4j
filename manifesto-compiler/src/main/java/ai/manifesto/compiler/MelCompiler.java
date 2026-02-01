package ai.manifesto.compiler;

import ai.manifesto.compiler.analyzer.ScopeAnalyzer;
import ai.manifesto.compiler.analyzer.ScopeAnalysisResult;
import ai.manifesto.compiler.analyzer.SemanticValidator;
import ai.manifesto.compiler.analyzer.ValidationResult;
import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.compiler.diagnostics.DiagnosticSeverity;
import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.core.schema.DomainSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * MelCompiler - full MEL compiler pipeline
 */
public final class MelCompiler implements CompilerFacade {

    @Override
    public CompilationResult compileDomain(String melText) {
        return compileDomain(melText, null);
    }

    @Override
    public CompilationResult compileDomain(String melText, CompileDomainOptions options) {
        if (melText == null || melText.trim().isEmpty()) {
            return CompilationResult.error("MEL input is empty");
        }

        List<Diagnostic> diagnostics = new ArrayList<>();
        List<CompileTrace> trace = new ArrayList<>();
        String fnTableVersion = options == null ? null : options.fnTableVersion();

        long lexStart = System.nanoTime();
        Lexer lexer = new Lexer(melText);
        var lexResult = lexer.tokenize();
        diagnostics.addAll(lexResult.diagnostics());
        trace.add(CompileTrace.of(
            "lex",
            elapsedMs(lexStart),
            fnTableVersion == null
                ? java.util.Map.of("tokenCount", lexResult.tokens().size())
                : java.util.Map.of("tokenCount", lexResult.tokens().size(), "fnTableVersion", fnTableVersion)
        ));
        if (hasErrors(diagnostics)) {
            return CompilationResult.error("Lexer error", diagnostics, trace);
        }

        long parseStart = System.nanoTime();
        Parser parser = new Parser(lexResult.tokens());
        ParseResult parseResult = parser.parse();
        diagnostics.addAll(parseResult.diagnostics());
        trace.add(CompileTrace.of("parse", elapsedMs(parseStart)));
        if (parseResult.program() == null || hasErrors(diagnostics)) {
            return CompilationResult.error("Parser error", diagnostics, trace);
        }

        ProgramNode program = parseResult.program();

        long scopeStart = System.nanoTime();
        ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
        ScopeAnalysisResult scopeResult = scopeAnalyzer.analyze(program);
        diagnostics.addAll(scopeResult.diagnostics());
        trace.add(CompileTrace.of("analyze", elapsedMs(scopeStart)));
        if (hasErrors(diagnostics)) {
            return CompilationResult.error("Scope analysis error", diagnostics, trace);
        }

        long validationStart = System.nanoTime();
        SemanticValidator validator = new SemanticValidator();
        ValidationResult validation = validator.validate(program);
        diagnostics.addAll(validation.diagnostics());
        trace.add(CompileTrace.of("validate", elapsedMs(validationStart)));
        if (hasErrors(diagnostics)) {
            return CompilationResult.error("Semantic validation error", diagnostics, trace);
        }

        long genStart = System.nanoTime();
        AstIrGenerator generator = new AstIrGenerator();
        GenerateResult generateResult = generator.generate(program);
        diagnostics.addAll(generateResult.diagnostics());
        trace.add(CompileTrace.of("generate", elapsedMs(genStart)));
        if (generateResult.schema() == null || hasErrors(diagnostics)) {
            return CompilationResult.error("IR generation error", diagnostics, trace);
        }

        DomainSchema schema = generateResult.schema();
        return CompilationResult.ok(schema, diagnostics, trace);
    }

    private boolean hasErrors(List<Diagnostic> diagnostics) {
        return diagnostics.stream().anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
