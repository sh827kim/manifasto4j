package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

/**
 * MaxArray - max over array elements.
 */
public record MaxArray(ExprNode array) implements ExprNode {
}
