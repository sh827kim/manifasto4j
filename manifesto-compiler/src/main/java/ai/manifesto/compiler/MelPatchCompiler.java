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
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.expr.logical.And;
import ai.manifesto.core.expr.logical.Not;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MelPatchCompiler - MEL text -> runtime patch ops (action scoped)
 */
public final class MelPatchCompiler {

    public CompilePatchResult compilePatch(String melText, String actionName) {
        if (melText == null || melText.trim().isEmpty()) {
            return new CompilePatchResult(List.of(), List.of());
        }

        List<Diagnostic> diagnostics = new ArrayList<>();

        Lexer lexer = new Lexer(melText);
        var lex = lexer.tokenize();
        diagnostics.addAll(lex.diagnostics());
        if (hasErrors(diagnostics)) {
            return new CompilePatchResult(List.of(), diagnostics);
        }

        Parser parser = new Parser(lex.tokens());
        ParseResult parse = parser.parse();
        diagnostics.addAll(parse.diagnostics());
        if (parse.program() == null || hasErrors(diagnostics)) {
            return new CompilePatchResult(List.of(), diagnostics);
        }

        ProgramNode program = parse.program();

        ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
        ScopeAnalysisResult scope = scopeAnalyzer.analyze(program);
        diagnostics.addAll(scope.diagnostics());
        if (hasErrors(diagnostics)) {
            return new CompilePatchResult(List.of(), diagnostics);
        }

        SemanticValidator validator = new SemanticValidator();
        ValidationResult validation = validator.validate(program);
        diagnostics.addAll(validation.diagnostics());
        if (hasErrors(diagnostics)) {
            return new CompilePatchResult(List.of(), diagnostics);
        }

        AstIrGenerator generator = new AstIrGenerator();
        GenerateResult generated = generator.generate(program);
        diagnostics.addAll(generated.diagnostics());
        DomainSchema schema = generated.schema();
        if (schema == null || hasErrors(diagnostics)) {
            return new CompilePatchResult(List.of(), diagnostics);
        }

        ActionSpec action = schema.getAction(actionName);
        if (action == null) {
            return new CompilePatchResult(List.of(), diagnostics);
        }

        List<Map<String, Object>> ops = new ArrayList<>();
        lowerFlow(action.getFlow(), null, ops);
        return new CompilePatchResult(ops, diagnostics);
    }

    private void lowerFlow(FlowNode flow, ExprNode guard, List<Map<String, Object>> out) {
        if (flow == null) {
            return;
        }
        if (flow instanceof FlowNode.Seq seq) {
            for (FlowNode step : seq.getSteps()) {
                lowerFlow(step, guard, out);
            }
            return;
        }
        if (flow instanceof FlowNode.If ifFlow) {
            ExprNode cond = ifFlow.getCond();
            ExprNode nextGuard = guard == null ? cond : new And(List.of(guard, cond));
            lowerFlow(ifFlow.getThenBranch(), nextGuard, out);
            if (ifFlow.getElseBranch() != null) {
                ExprNode elseGuard = guard == null ? new Not(cond) : new And(List.of(guard, new Not(cond)));
                lowerFlow(ifFlow.getElseBranch(), elseGuard, out);
            }
            return;
        }
        if (flow instanceof FlowNode.Patch patch) {
            Map<String, Object> map = new LinkedHashMap<>();
            if (guard != null) {
                map.put("condition", ValidationUtils.exprToMap(guard));
            }
            map.put("op", patch.getOp().getCode());
            map.put("path", patch.getPath());
            if (patch.getValue() != null) {
                map.put("value", ValidationUtils.exprToMap(patch.getValue()));
            }
            out.add(map);
        }
    }

    private boolean hasErrors(List<Diagnostic> diagnostics) {
        return diagnostics.stream().anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
    }
}
