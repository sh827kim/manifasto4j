package ai.manifesto.translator.helpers;

import ai.manifesto.translator.core.Chunk;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: chunk 목록의 중복/겹침/빈값을 검증합니다.
 * EN: Validates chunk list for empties/overlaps/duplicates.
 */
public final class TranslatorChunkValidator {
    public ChunkValidationResult validate(List<Chunk> chunks) {
        List<String> diagnostics = new ArrayList<>();
        if (chunks == null || chunks.isEmpty()) {
            diagnostics.add("CHV001: chunks must not be empty");
            return new ChunkValidationResult(false, List.copyOf(diagnostics));
        }
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if (chunk == null) {
                diagnostics.add("CHV002: chunk entry must not be null");
                continue;
            }
            if (chunk.text() == null || chunk.text().isBlank()) {
                diagnostics.add("CHV003: chunk text must not be blank");
            }
            for (int j = i + 1; j < chunks.size(); j++) {
                Chunk other = chunks.get(j);
                if (other != null && chunk.span() != null && other.span() != null && chunk.span().overlaps(other.span())) {
                    diagnostics.add("CHV004: chunk spans overlap: " + chunk.id() + " vs " + other.id());
                }
            }
        }
        return new ChunkValidationResult(diagnostics.isEmpty(), List.copyOf(diagnostics));
    }
}
