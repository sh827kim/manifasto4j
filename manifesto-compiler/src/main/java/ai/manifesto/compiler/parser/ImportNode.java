package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * KR: ImportNode는 트리 구조에서 단일 노드를 표현하는 데이터 타입입니다.
 * EN: ImportNode is a data type representing a single node in a tree structure.
 */
public record ImportNode(
    List<String> names,
    String from,
    SourceLocation location
) implements AstNode {
}
