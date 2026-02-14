package ai.manifesto.translator.strategies;

import ai.manifesto.translator.TranslationRequest;
import ai.manifesto.translator.core.Chunk;

import java.util.List;

/**
 * KR: 요청 텍스트를 청크로 분해하는 전략 계약입니다.
 * EN: Strategy contract for decomposing request text into chunks.
 */
public interface DecomposeStrategy {
    List<Chunk> decompose(TranslationRequest request, DecomposeOptions options);
}
