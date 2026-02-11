package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * KR: TypeFieldNode는 트리 구조에서 단일 노드를 표현하는 데이터 타입입니다.
 * EN: TypeFieldNode is a data type representing a single node in a tree structure.
 */
public record TypeFieldNode(
    String name,
    TypeExprNode typeExpr,
    boolean optional,
    SourceLocation location
) implements AstNode {
}
