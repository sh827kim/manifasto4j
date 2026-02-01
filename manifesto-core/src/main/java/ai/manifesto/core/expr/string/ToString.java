package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

/**
 * ToString - explicit string conversion.
 */
public record ToString(ExprNode arg) implements ExprNode {
}
