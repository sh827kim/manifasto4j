package ai.manifesto.world.persistence;

import java.util.List;

/**
 * KR: world persistence 배치 처리 결과(contract)입니다.
 * EN: Contract for world persistence batch operation result.
 */
public record StoreBatchResult(
    boolean success,
    int processed,
    int failed,
    List<String> errors
) {
    public StoreBatchResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
