package ai.manifesto.codegen.runtime;

/**
 * KR: generated 파일 헤더 생성 옵션입니다.
 * EN: Header generation options for generated files.
 */
public record HeaderOptions(
    String sourceId,
    String schemaHash,
    boolean stamp
) {}
