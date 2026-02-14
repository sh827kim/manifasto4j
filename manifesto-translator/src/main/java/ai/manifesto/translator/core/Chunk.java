package ai.manifesto.translator.core;

/**
 * KR: 번역 파이프라인에서 처리하는 텍스트 청크입니다.
 * EN: Text chunk processed in translator pipeline.
 */
public record Chunk(
    String id,
    String text,
    Span span
) {}
