package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * KR: ArrayLiteralExprNode는 트리 구조에서 단일 노드를 표현하는 데이터 타입입니다.
 * EN: ArrayLiteralExprNode is a data type representing a single node in a tree structure.
 */
public record ArrayLiteralExprNode(
    List<ExprNode> elements,
    SourceLocation location
) implements ExprNode {
}
