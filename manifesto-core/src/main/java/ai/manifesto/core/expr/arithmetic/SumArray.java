package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

/**
 * SumArray - sum over array elements.
 */
public record SumArray(ExprNode array) implements ExprNode {
}
