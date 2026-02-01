package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

/**
 * StrLen - string length.
 */
public record StrLen(ExprNode str) implements ExprNode {
}
