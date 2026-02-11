package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * KR: IndexSegmentNode는 트리 구조에서 단일 노드를 표현하는 데이터 타입입니다.
 * EN: IndexSegmentNode is a data type representing a single node in a tree structure.
 */
public record IndexSegmentNode(
    ExprNode index,
    SourceLocation location
) implements PathSegmentNode {
}
