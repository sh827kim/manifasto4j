package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

/**
 * MinArray - min over array elements.
 */
public record MinArray(ExprNode array) implements ExprNode {
}
