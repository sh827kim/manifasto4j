package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.compiler.diagnostics.DiagnosticCode;
import ai.manifesto.compiler.diagnostics.DiagnosticSeverity;
import ai.manifesto.compiler.diagnostics.SourceSpan;
import ai.manifesto.compiler.parser.ActionNode;
import ai.manifesto.compiler.parser.ArrayLiteralExprNode;
import ai.manifesto.compiler.parser.BinaryExprNode;
import ai.manifesto.compiler.parser.ComputedNode;
import ai.manifesto.compiler.parser.DomainNode;
import ai.manifesto.compiler.parser.DomainMember;
import ai.manifesto.compiler.parser.EffectStmtNode;
import ai.manifesto.compiler.parser.ExprNode;
import ai.manifesto.compiler.parser.FailStmtNode;
import ai.manifesto.compiler.parser.FunctionCallExprNode;
import ai.manifesto.compiler.parser.IdentifierExprNode;
import ai.manifesto.compiler.parser.IndexAccessExprNode;
import ai.manifesto.compiler.parser.InnerStmtNode;
import ai.manifesto.compiler.parser.IterationVarExprNode;
import ai.manifesto.compiler.parser.LiteralExprNode;
import ai.manifesto.compiler.parser.ObjectLiteralExprNode;
import ai.manifesto.compiler.parser.ObjectTypeNode;
import ai.manifesto.compiler.parser.OnceIntentStmtNode;
import ai.manifesto.compiler.parser.OnceStmtNode;
import ai.manifesto.compiler.parser.PatchStmtNode;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.parser.PropertyAccessExprNode;
import ai.manifesto.compiler.parser.StateNode;
import ai.manifesto.compiler.parser.StateFieldNode;
import ai.manifesto.compiler.parser.StopStmtNode;
import ai.manifesto.compiler.parser.SystemIdentExprNode;
import ai.manifesto.compiler.parser.TernaryExprNode;
import ai.manifesto.compiler.parser.TypeExprNode;
import ai.manifesto.compiler.parser.UnaryExprNode;
import ai.manifesto.compiler.parser.WhenStmtNode;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: SemanticValidator는 컴파일러 분석 계층에서 semantic validator 역할을 수행하는 구현 타입입니다.
 * EN: SemanticValidator is an implementation type performing semantic validator roles in the compiler analyzer layer.
 */
public final class SemanticValidator {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public ValidationResult validate(ProgramNode program) {
        diagnostics.clear();
        if (program == null) {
            return new ValidationResult(true, List.copyOf(diagnostics));
        }
        validateDomain(program.domain());
        boolean valid = diagnostics.stream().noneMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
        return new ValidationResult(valid, List.copyOf(diagnostics));
    }

    private void validateDomain(DomainNode domain) {
        for (DomainMember member : domain.members()) {
            if (member instanceof StateNode state) {
                for (StateFieldNode field : state.fields()) {
                    checkAnonymousObjectType(field.typeExpr(), field.location());
                    if (field.initializer() != null) {
                        validateExpr(field.initializer(), ExprContext.STATE_INIT);
                    }
                }
            } else if (member instanceof ComputedNode computed) {
                validateExpr(computed.expression(), ExprContext.COMPUTED);
            } else if (member instanceof ActionNode action) {
                if (action.available() != null) {
                    validateExpr(action.available(), ExprContext.AVAILABLE);
                    warnIfNonBoolCondition(action.available());
                }
                validateActionBody(action);
            }
        }
    }

    private void validateActionBody(ActionNode action) {
        for (var stmt : action.body()) {
            if (stmt instanceof WhenStmtNode || stmt instanceof OnceStmtNode || stmt instanceof OnceIntentStmtNode) {
                validateGuardedStmt(stmt);
            } else {
                diagnostics.add(Diagnostic.error(
                    DiagnosticCode.E_UNGUARDED_STMT,
                    "Statement must be inside a guard (when, once, or onceIntent)",
                    spanOf(stmt.location())
                ));
            }
        }
    }

    private void validateGuardedStmt(Object stmt) {
        if (stmt instanceof WhenStmtNode whenStmt) {
            validateExpr(whenStmt.condition(), ExprContext.GENERAL);
            warnIfNonBoolCondition(whenStmt.condition());
            for (InnerStmtNode inner : whenStmt.body()) {
                validateInnerStmt(inner);
            }
            return;
        }
        if (stmt instanceof OnceStmtNode onceStmt) {
            if (onceStmt.condition() != null) {
                validateExpr(onceStmt.condition(), ExprContext.GENERAL);
                warnIfNonBoolCondition(onceStmt.condition());
            }
            for (InnerStmtNode inner : onceStmt.body()) {
                validateInnerStmt(inner);
            }
            return;
        }
        if (stmt instanceof OnceIntentStmtNode onceIntentStmt) {
            if (onceIntentStmt.condition() != null) {
                validateExpr(onceIntentStmt.condition(), ExprContext.GENERAL);
                warnIfNonBoolCondition(onceIntentStmt.condition());
            }
            for (InnerStmtNode inner : onceIntentStmt.body()) {
                validateInnerStmt(inner);
            }
            return;
        }
    }

    private void validateInnerStmt(InnerStmtNode stmt) {
        if (stmt instanceof PatchStmtNode patchStmt) {
            if (patchStmt.value() != null) {
                validateExpr(patchStmt.value(), ExprContext.GENERAL);
            }
            return;
        }
        if (stmt instanceof EffectStmtNode effectStmt) {
            for (var arg : effectStmt.args()) {
                if (!arg.isPath()) {
                    validateExpr((ExprNode) arg.value(), ExprContext.GENERAL);
                }
            }
            return;
        }
        if (stmt instanceof FailStmtNode failStmt) {
            if (failStmt.message() != null) {
                validateExpr(failStmt.message(), ExprContext.GENERAL);
            }
            return;
        }
        if (stmt instanceof StopStmtNode) {
            StopStmtNode stopStmt = (StopStmtNode) stmt;
            String reason = stopStmt.reason() == null ? "" : stopStmt.reason().toLowerCase();
            if (reason.contains("wait") || reason.contains("pending") || reason.contains("processing")) {
                diagnostics.add(Diagnostic.error(
                    DiagnosticCode.E008,
                    "stop message suggests waiting/pending - use 'Already processed' style instead",
                    spanOf(stopStmt.location())
                ));
            }
            return;
        }
        if (stmt instanceof WhenStmtNode || stmt instanceof OnceStmtNode || stmt instanceof OnceIntentStmtNode) {
            validateGuardedStmt(stmt);
        }
    }

    private void validateExpr(ExprNode expr, ExprContext context) {
        if (expr == null) {
            return;
        }
        if (expr instanceof LiteralExprNode || expr instanceof IdentifierExprNode) {
            return;
        }
        if (expr instanceof SystemIdentExprNode systemIdent) {
            if (!systemIdent.path().isEmpty()) {
                String base = systemIdent.path().get(0);
                if ("system".equals(base)) {
                    if (context == ExprContext.COMPUTED) {
                        diagnostics.add(Diagnostic.error(
                            DiagnosticCode.E001,
                            "$system.* cannot be used in computed expressions",
                            spanOf(systemIdent.location())
                        ));
                    } else if (context == ExprContext.STATE_INIT) {
                        diagnostics.add(Diagnostic.error(
                            DiagnosticCode.E002,
                            "$system.* cannot be used in state initializers",
                            spanOf(systemIdent.location())
                        ));
                    } else if (context == ExprContext.AVAILABLE) {
                        diagnostics.add(Diagnostic.error(
                            DiagnosticCode.E005,
                            "$system.* cannot be used in available condition",
                            spanOf(systemIdent.location())
                        ));
                    }
                }
                if ("input".equals(base) && context == ExprContext.AVAILABLE) {
                    diagnostics.add(Diagnostic.error(
                        DiagnosticCode.E005,
                        "$input.* cannot be used in available condition",
                        spanOf(systemIdent.location())
                    ));
                }
            }
            return;
        }
        if (expr instanceof PropertyAccessExprNode prop) {
            validateExpr(prop.object(), context);
            return;
        }
        if (expr instanceof IndexAccessExprNode index) {
            validateExpr(index.object(), context);
            validateExpr(index.index(), context);
            return;
        }
        if (expr instanceof FunctionCallExprNode call) {
            String name = call.name();
            if (isForbiddenReduce(name)) {
                diagnostics.add(Diagnostic.error(
                    DiagnosticCode.E011,
                    "reduce/fold/scan is forbidden - use sum, min, max for aggregation",
                    spanOf(call.location())
                ));
            }
            if (isAggregation(name) && context != ExprContext.COMPUTED) {
                diagnostics.add(Diagnostic.error(
                    DiagnosticCode.E009,
                    "Primitive aggregation (sum, min, max) only allowed in computed",
                    spanOf(call.location())
                ));
            }
            if (isAggregation(name)) {
                for (ExprNode arg : call.args()) {
                    if (!isSimpleRef(arg)) {
                        diagnostics.add(Diagnostic.error(
                            DiagnosticCode.E010,
                            "Primitive aggregation does not allow composition - use direct reference only",
                            spanOf(call.location())
                        ));
                        break;
                    }
                }
            }
            for (ExprNode arg : call.args()) {
                validateExpr(arg, context);
            }
            return;
        }
        if (expr instanceof UnaryExprNode unary) {
            validateExpr(unary.operand(), context);
            return;
        }
        if (expr instanceof BinaryExprNode binary) {
            validateExpr(binary.left(), context);
            validateExpr(binary.right(), context);
            return;
        }
        if (expr instanceof TernaryExprNode ternary) {
            validateExpr(ternary.condition(), context);
            validateExpr(ternary.consequent(), context);
            validateExpr(ternary.alternate(), context);
            return;
        }
        if (expr instanceof ObjectLiteralExprNode obj) {
            obj.properties().forEach(p -> validateExpr(p.value(), context));
            return;
        }
        if (expr instanceof ArrayLiteralExprNode arr) {
            arr.elements().forEach(e -> validateExpr(e, context));
        }
    }

    private void checkAnonymousObjectType(TypeExprNode typeExpr, ai.manifesto.compiler.lexer.SourceLocation location) {
        if (typeExpr instanceof ObjectTypeNode objectType) {
            diagnostics.add(Diagnostic.warning(
                DiagnosticCode.W012,
                "Anonymous object type in state field. Use a named type declaration instead.",
                spanOf(location)
            ));
            for (var field : objectType.fields()) {
                checkAnonymousObjectType(field.typeExpr(), field.location());
            }
        }
    }

    private void warnIfNonBoolCondition(ExprNode expr) {
        if (expr instanceof LiteralExprNode literal) {
            if (!(literal.value() instanceof Boolean)) {
                diagnostics.add(Diagnostic.warning(
                    DiagnosticCode.W_NON_BOOL_COND,
                    "Condition may not be boolean",
                    spanOf(literal.location())
                ));
            }
            return;
        }
        if (expr instanceof ObjectLiteralExprNode || expr instanceof ArrayLiteralExprNode) {
            diagnostics.add(Diagnostic.warning(
                DiagnosticCode.W_NON_BOOL_COND,
                "Condition may not be boolean",
                spanOf(expr.location())
            ));
        }
    }

    private SourceSpan spanOf(ai.manifesto.compiler.lexer.SourceLocation location) {
        return SourceSpan.of(location.start().line(), location.start().column(), 1);
    }

    private enum ExprContext {
        GENERAL,
        COMPUTED,
        STATE_INIT,
        AVAILABLE
    }

    private boolean isAggregation(String name) {
        return "sum".equals(name) || "min".equals(name) || "max".equals(name);
    }

    private boolean isForbiddenReduce(String name) {
        return "reduce".equals(name) || "fold".equals(name) || "scan".equals(name);
    }

    private boolean isSimpleRef(ExprNode expr) {
        return expr instanceof IdentifierExprNode
            || expr instanceof PropertyAccessExprNode
            || expr instanceof SystemIdentExprNode
            || expr instanceof IterationVarExprNode;
    }
}
