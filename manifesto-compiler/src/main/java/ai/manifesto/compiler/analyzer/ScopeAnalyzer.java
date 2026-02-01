package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.compiler.diagnostics.DiagnosticCode;
import ai.manifesto.compiler.diagnostics.SourceSpan;
import ai.manifesto.compiler.parser.ActionNode;
import ai.manifesto.compiler.parser.ComputedNode;
import ai.manifesto.compiler.parser.DomainNode;
import ai.manifesto.compiler.parser.DomainMember;
import ai.manifesto.compiler.parser.ExprNode;
import ai.manifesto.compiler.parser.IdentifierExprNode;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.parser.StateNode;
import ai.manifesto.compiler.parser.StateFieldNode;

import java.util.ArrayList;
import java.util.List;

/**
 * ScopeAnalyzer - 기본 스코프 분석 (중복/미정의 체크)
 */
public final class ScopeAnalyzer {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public ScopeAnalysisResult analyze(ProgramNode program) {
        diagnostics.clear();
        if (program == null) {
            return new ScopeAnalysisResult(null, List.copyOf(diagnostics));
        }
        Scope domainScope = new Scope("domain", null);
        analyzeDomain(program.domain(), domainScope);
        return new ScopeAnalysisResult(domainScope, List.copyOf(diagnostics));
    }

    private void analyzeDomain(DomainNode domain, Scope domainScope) {
        for (DomainMember member : domain.members()) {
            if (member instanceof StateNode state) {
                for (StateFieldNode field : state.fields()) {
                    define(domainScope, field.name(), SymbolKind.STATE, field.location());
                }
            } else if (member instanceof ComputedNode computed) {
                define(domainScope, computed.name(), SymbolKind.COMPUTED, computed.location());
            } else if (member instanceof ActionNode action) {
                define(domainScope, action.name(), SymbolKind.ACTION, action.location());
            }
        }

        for (DomainMember member : domain.members()) {
            if (member instanceof ComputedNode computed) {
                analyzeExpr(computed.expression(), domainScope);
            } else if (member instanceof ActionNode action) {
                Scope actionScope = new Scope("action", domainScope);
                action.params().forEach(param -> define(actionScope, param.name(), SymbolKind.PARAM, param.location()));
                if (action.available() != null) {
                    analyzeExpr(action.available(), actionScope);
                }
            }
        }
    }

    private void analyzeExpr(ExprNode expr, Scope scope) {
        if (expr == null) {
            return;
        }
        if (expr instanceof IdentifierExprNode ident) {
            if (scope.lookup(ident.name()) == null) {
                diagnostics.add(Diagnostic.error(
                    DiagnosticCode.E_UNDEFINED,
                    "Undefined identifier: " + ident.name(),
                    spanOf(ident.location())
                ));
            }
            return;
        }
        // TODO: recursive traversal for other ExprNode types
    }

    private void define(Scope scope, String name, SymbolKind kind, ai.manifesto.compiler.lexer.SourceLocation location) {
        Symbol symbol = new Symbol(name, kind, location);
        if (!scope.define(symbol)) {
            diagnostics.add(Diagnostic.error(
                DiagnosticCode.E_DUPLICATE,
                "Duplicate identifier: " + name,
                spanOf(location)
            ));
        }
    }

    private SourceSpan spanOf(ai.manifesto.compiler.lexer.SourceLocation location) {
        return SourceSpan.of(location.start().line(), location.start().column(), 1);
    }
}
