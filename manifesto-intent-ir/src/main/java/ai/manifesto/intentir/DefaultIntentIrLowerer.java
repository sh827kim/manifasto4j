package ai.manifesto.intentir;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: Intent-IR을 action/input/meta 실행 payload로 변환하는 기본 lower 구현입니다.
 * EN: Default lower implementation converting Intent-IR into action/input/meta execution payload.
 */
public final class DefaultIntentIrLowerer implements IntentIrLowerer {
    @Override
    public IntentIrLowerResult lower(IntentIrDocument document) {
        Objects.requireNonNull(document, "document must not be null");
        IntentIrDocument normalized = new DefaultIntentIrNormalizer().normalize(document);
        List<String> diagnostics = new ArrayList<>();

        String action = normalized.action();
        if (normalized.domain() == null || normalized.domain().isBlank()) {
            diagnostics.add("LRW000: unresolved domain lowered as blank");
        }
        if (action == null || action.isBlank()) {
            diagnostics.add("LRW002: unresolved action lowered as blank");
        } else if ("unknown".equalsIgnoreCase(action)) {
            diagnostics.add("LRW001: unresolved action lowered as unknown");
        }

        Map<String, Object> input = new LinkedHashMap<>();
        if (normalized.input() != null) {
            input.putAll(normalized.input());
        }
        input.putIfAbsent("_intentIr.domain", normalized.domain());
        input.putIfAbsent("_intentIr.schemaVersion", normalized.schemaVersion());

        Map<String, Object> meta = new LinkedHashMap<>();
        if (normalized.meta() != null) {
            meta.putAll(normalized.meta());
        }
        meta.put("lowered", true);
        meta.put("loweredAt", System.currentTimeMillis());
        meta.put("lowerDiagnosticsCount", diagnostics.size());

        return new IntentIrLowerResult(
            normalized.domain(),
            action,
            Map.copyOf(input),
            Map.copyOf(meta),
            List.copyOf(diagnostics)
        );
    }
}
