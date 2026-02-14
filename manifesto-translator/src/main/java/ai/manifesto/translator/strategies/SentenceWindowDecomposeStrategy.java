package ai.manifesto.translator.strategies;

import ai.manifesto.translator.TranslationRequest;
import ai.manifesto.translator.core.Chunk;
import ai.manifesto.translator.core.Span;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: 문장/윈도우 기준으로 텍스트를 분해하는 decompose 전략입니다.
 * EN: Decompose strategy that splits text by sentence/window boundaries.
 */
public final class SentenceWindowDecomposeStrategy implements DecomposeStrategy {
    @Override
    public List<Chunk> decompose(TranslationRequest request, DecomposeOptions options) {
        List<Chunk> chunks = new ArrayList<>();
        if (request == null || request.messages() == null || request.messages().isEmpty()) {
            return List.of();
        }
        int maxChunkLength = options == null ? DecomposeOptions.defaults().maxChunkLength() : options.maxChunkLength();
        int maxChunks = options == null ? DecomposeOptions.defaults().maxChunks() : options.maxChunks();

        String text = String.valueOf(request.messages().get(request.messages().size() - 1).content());
        String[] sentences = text.split("[.!?]+\\s*");
        int cursor = 0;
        int seq = 0;
        for (String raw : sentences) {
            if (seq >= maxChunks) {
                break;
            }
            String sentence = raw == null ? "" : raw.trim();
            if (sentence.isBlank()) {
                continue;
            }
            int start = cursor;
            int end = Math.min(cursor + sentence.length(), cursor + maxChunkLength);
            String body = sentence.length() > maxChunkLength ? sentence.substring(0, maxChunkLength) : sentence;
            chunks.add(new Chunk("chunk-" + seq, body, new Span(start, end)));
            cursor = end + 1;
            seq += 1;
        }
        return List.copyOf(chunks);
    }
}
