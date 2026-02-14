package ai.manifesto.translator.strategies;

import ai.manifesto.translator.core.Chunk;
import ai.manifesto.translator.core.IntentGraph;

import java.util.List;

/**
 * KR: 청크 목록을 intent graph로 변환하는 전략 계약입니다.
 * EN: Strategy contract for converting chunks into an intent graph.
 */
public interface TranslateStrategy {
    IntentGraph translate(List<Chunk> chunks, TranslateOptions options);
}
