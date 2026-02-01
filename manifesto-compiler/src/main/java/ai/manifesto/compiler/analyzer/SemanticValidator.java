package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.compiler.diagnostics.DiagnosticCode;
import ai.manifesto.compiler.diagnostics.SourceSpan;
import ai.manifesto.compiler.parser.ActionNode;
import ai.manifesto.compiler.parser.ComputedNode;
import ai.manifesto.compiler.parser.DomainNode;
import ai.manifesto.compiler.parser.DomainMember;
import ai.manifesto.compiler.parser.ExprNode;
import ai.manifesto.compiler.parser.ObjectTypeNode;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.parser.StateNode;
import ai.manifesto.compiler.parser.StateFieldNode;
import ai.manifesto.compiler.parser.SystemIdentExprNode;
import ai.manifesto.compiler.parser.TypeExprNode;

import java.util.ArrayList;
import java.util.List;

/**
 * SemanticValidator - MEL semantic validation (subset)
 */
public final class SemanticValidator {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public ValidationResult validate(ProgramNode program) {
        diagnostics.clear();
        if (program == null) {
            return new ValidationResult(true, List.copyOf(diagnostics));
        }
        validateDomain(program.domain());
        boolean valid = diagnostics.stream().noneMatch(d -> d.severity().name().equals("ERROR"));
        return new ValidationResult(valid, List.copyOf(diagnostics));
    }

    private void validateDomain(DomainNode domain) {
        for (DomainMember member : domain.members()) {
            if (member instanceof StateNode state) {
                for (StateFieldNode field : state.fields()) {
                    checkAnonymousObjectType(field.typeExpr(), field.location());
                }
            } else if (member instanceof ComputedNode computed) {
                validateExpr(computed.expression());
            } else if (member instanceof ActionNode action) {
                if (action.available() != null) {
                    validateAvailableExpr(action.available());
                }
            }
        }
    }

    private void validateAvailableExpr(ExprNode expr) {
        if (expr instanceof SystemIdentExprNode systemIdent) {
            if (!systemIdent.path().isEmpty()) {
                String base = systemIdent.path().get(0);
                if ("system".equals(base) || "input".equals(base)) {
                    diagnostics.add(Diagnostic.error(
                        DiagnosticCode.E005,
                        "$system.* or $input.* cannot be used in available condition",
                        spanOf(systemIdent.location())
                    ));
                }
            }
            return;
        }
        // TODO: recursive traversal for composite expressions
    }

    private void validateExpr(ExprNode expr) {
        // TODO: additional semantic checks (E001/E002/etc)
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

    private SourceSpan spanOf(ai.manifesto.compiler.lexer.SourceLocation location) {
        return SourceSpan.of(location.start().line(), location.start().column(), 1);
    }
}
