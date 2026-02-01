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
        if (melText == null || melText.trim().isEmpty()) {
            return CompilationResult.error("MEL input is empty");
        }

        List<Diagnostic> diagnostics = new ArrayList<>();

        Lexer lexer = new Lexer(melText);
        var lexResult = lexer.tokenize();
        diagnostics.addAll(lexResult.diagnostics());
        if (hasErrors(diagnostics)) {
            return CompilationResult.error("Lexer error", diagnostics);
        }

        Parser parser = new Parser(lexResult.tokens());
        ParseResult parseResult = parser.parse();
        diagnostics.addAll(parseResult.diagnostics());
        if (parseResult.program() == null || hasErrors(diagnostics)) {
            return CompilationResult.error("Parser error", diagnostics);
        }

        ProgramNode program = parseResult.program();

        ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
        ScopeAnalysisResult scopeResult = scopeAnalyzer.analyze(program);
        diagnostics.addAll(scopeResult.diagnostics());
        if (hasErrors(diagnostics)) {
            return CompilationResult.error("Scope analysis error", diagnostics);
        }

        SemanticValidator validator = new SemanticValidator();
        ValidationResult validation = validator.validate(program);
        diagnostics.addAll(validation.diagnostics());
        if (hasErrors(diagnostics)) {
            return CompilationResult.error("Semantic validation error", diagnostics);
        }

        AstIrGenerator generator = new AstIrGenerator();
        GenerateResult generateResult = generator.generate(program);
        diagnostics.addAll(generateResult.diagnostics());
        if (generateResult.schema() == null || hasErrors(diagnostics)) {
            return CompilationResult.error("IR generation error", diagnostics);
        }

        DomainSchema schema = generateResult.schema();
        return CompilationResult.ok(schema, diagnostics);
    }

    private boolean hasErrors(List<Diagnostic> diagnostics) {
        return diagnostics.stream().anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
    }
}
