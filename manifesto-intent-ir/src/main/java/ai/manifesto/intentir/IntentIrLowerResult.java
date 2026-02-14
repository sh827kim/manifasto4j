package ai.manifesto.intentir;

import ai.manifesto.core.Intent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * KR: Intent-IR lower 결과 모델입니다.
 * EN: Result model for Intent-IR lowering.
 */
public record IntentIrLowerResult(
    String domain,
    String action,
    Map<String, Object> input,
    Map<String, Object> meta,
    List<String> diagnostics
) {
    public Intent toIntent() {
        return new Intent(action, input, "lowered-" + UUID.randomUUID());
    }
}
