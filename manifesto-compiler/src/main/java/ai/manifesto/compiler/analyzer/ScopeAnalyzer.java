package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.compiler.diagnostics.DiagnosticCode;
import ai.manifesto.compiler.diagnostics.SourceSpan;
import ai.manifesto.compiler.parser.ActionNode;
import ai.manifesto.compiler.parser.ArrayLiteralExprNode;
import ai.manifesto.compiler.parser.BinaryExprNode;
import ai.manifesto.compiler.parser.ComputedNode;
import ai.manifesto.compiler.parser.DomainNode;
import ai.manifesto.compiler.parser.DomainMember;
import ai.manifesto.compiler.parser.EffectStmtNode;
import ai.manifesto.compiler.parser.ExprNode;
import ai.manifesto.compiler.parser.FunctionCallExprNode;
import ai.manifesto.compiler.parser.IdentifierExprNode;
import ai.manifesto.compiler.parser.IndexAccessExprNode;
import ai.manifesto.compiler.parser.InnerStmtNode;
import ai.manifesto.compiler.parser.ObjectLiteralExprNode;
import ai.manifesto.compiler.parser.OnceIntentStmtNode;
import ai.manifesto.compiler.parser.OnceStmtNode;
import ai.manifesto.compiler.parser.PatchStmtNode;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.parser.PropertyAccessExprNode;
import ai.manifesto.compiler.parser.StateNode;
import ai.manifesto.compiler.parser.StateFieldNode;
import ai.manifesto.compiler.parser.TernaryExprNode;
import ai.manifesto.compiler.parser.UnaryExprNode;
import ai.manifesto.compiler.parser.WhenStmtNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * KR: ScopeAnalyzer는 컴파일러 분석 계층에서 scope analyzer 역할을 수행하는 구현 타입입니다.
 * EN: ScopeAnalyzer is an implementation type performing scope analyzer roles in the compiler analyzer layer.
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
                analyzeExpr(computed.expression(), domainScope, null);
            } else if (member instanceof ActionNode action) {
                Scope actionScope = new Scope("action", domainScope);
                action.params().forEach(param -> define(actionScope, param.name(), SymbolKind.PARAM, param.location()));
                if (action.available() != null) {
                    analyzeExpr(action.available(), actionScope, null);
                }
                Set<String> usedParams = new HashSet<>();
                analyzeActionBody(action, actionScope, usedParams);
                action.params().forEach(param -> {
                    if (!usedParams.contains(param.name())) {
                        diagnostics.add(Diagnostic.warning(
                            DiagnosticCode.W_UNUSED,
                            "Unused identifier: " + param.name(),
                            spanOf(param.location())
                        ));
                    }
                });
            }
        }
    }

    private void analyzeActionBody(ActionNode action, Scope scope, Set<String> usedParams) {
        for (var stmt : action.body()) {
            if (stmt instanceof WhenStmtNode whenStmt) {
                analyzeExpr(whenStmt.condition(), scope, usedParams);
                for (InnerStmtNode inner : whenStmt.body()) {
                    analyzeInnerStmt(inner, scope, usedParams);
                }
            } else if (stmt instanceof OnceStmtNode onceStmt) {
                if (onceStmt.condition() != null) {
                    analyzeExpr(onceStmt.condition(), scope, usedParams);
                }
                for (InnerStmtNode inner : onceStmt.body()) {
                    analyzeInnerStmt(inner, scope, usedParams);
                }
            } else if (stmt instanceof OnceIntentStmtNode onceIntentStmt) {
                if (onceIntentStmt.condition() != null) {
                    analyzeExpr(onceIntentStmt.condition(), scope, usedParams);
                }
                for (InnerStmtNode inner : onceIntentStmt.body()) {
                    analyzeInnerStmt(inner, scope, usedParams);
                }
            }
        }
    }

    private void analyzeInnerStmt(InnerStmtNode stmt, Scope scope, Set<String> usedParams) {
        if (stmt instanceof PatchStmtNode patchStmt) {
            if (patchStmt.value() != null) {
                analyzeExpr(patchStmt.value(), scope, usedParams);
            }
            return;
        }
        if (stmt instanceof EffectStmtNode effectStmt) {
            effectStmt.args().forEach(arg -> {
                if (!arg.isPath()) {
                    analyzeExpr((ExprNode) arg.value(), scope, usedParams);
                }
            });
            return;
        }
        if (stmt instanceof WhenStmtNode whenStmt) {
            analyzeExpr(whenStmt.condition(), scope, usedParams);
            for (InnerStmtNode inner : whenStmt.body()) {
                analyzeInnerStmt(inner, scope, usedParams);
            }
            return;
        }
        if (stmt instanceof OnceStmtNode onceStmt) {
            if (onceStmt.condition() != null) {
                analyzeExpr(onceStmt.condition(), scope, usedParams);
            }
            for (InnerStmtNode inner : onceStmt.body()) {
                analyzeInnerStmt(inner, scope, usedParams);
            }
            return;
        }
        if (stmt instanceof OnceIntentStmtNode onceIntentStmt) {
            if (onceIntentStmt.condition() != null) {
                analyzeExpr(onceIntentStmt.condition(), scope, usedParams);
            }
            for (InnerStmtNode inner : onceIntentStmt.body()) {
                analyzeInnerStmt(inner, scope, usedParams);
            }
        }
    }

    private void analyzeExpr(ExprNode expr, Scope scope, Set<String> usedParams) {
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
            } else if (usedParams != null) {
                usedParams.add(ident.name());
            }
            return;
        }
        if (expr instanceof PropertyAccessExprNode prop) {
            analyzeExpr(prop.object(), scope, usedParams);
            return;
        }
        if (expr instanceof IndexAccessExprNode index) {
            analyzeExpr(index.object(), scope, usedParams);
            analyzeExpr(index.index(), scope, usedParams);
            return;
        }
        if (expr instanceof FunctionCallExprNode call) {
            for (ExprNode arg : call.args()) {
                analyzeExpr(arg, scope, usedParams);
            }
            return;
        }
        if (expr instanceof UnaryExprNode unary) {
            analyzeExpr(unary.operand(), scope, usedParams);
            return;
        }
        if (expr instanceof BinaryExprNode binary) {
            analyzeExpr(binary.left(), scope, usedParams);
            analyzeExpr(binary.right(), scope, usedParams);
            return;
        }
        if (expr instanceof TernaryExprNode ternary) {
            analyzeExpr(ternary.condition(), scope, usedParams);
            analyzeExpr(ternary.consequent(), scope, usedParams);
            analyzeExpr(ternary.alternate(), scope, usedParams);
            return;
        }
        if (expr instanceof ObjectLiteralExprNode obj) {
            obj.properties().forEach(p -> analyzeExpr(p.value(), scope, usedParams));
            return;
        }
        if (expr instanceof ArrayLiteralExprNode arr) {
            arr.elements().forEach(e -> analyzeExpr(e, scope, usedParams));
        }
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
