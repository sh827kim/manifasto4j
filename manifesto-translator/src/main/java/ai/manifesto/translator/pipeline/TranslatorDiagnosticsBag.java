package ai.manifesto.translator.pipeline;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * KR: pipeline 전 단계에서 diagnostics를 수집/정규화하는 bag입니다.
 * EN: Bag aggregating diagnostics across pipeline stages.
 */
public final class TranslatorDiagnosticsBag {
    private final DiagnosticsAggregationPolicy policy;
    private final List<String> values = new ArrayList<>();

    public TranslatorDiagnosticsBag(DiagnosticsAggregationPolicy policy) {
        this.policy = policy == null ? DiagnosticsAggregationPolicy.DEDUP : policy;
    }

    public void add(String diagnostic) {
        if (diagnostic != null && !diagnostic.isBlank()) {
            values.add(diagnostic);
        }
    }

    public void addAll(List<String> diagnostics) {
        if (diagnostics == null) {
            return;
        }
        diagnostics.forEach(this::add);
    }

    public List<String> toList() {
        if (policy == DiagnosticsAggregationPolicy.PRESERVE) {
            return List.copyOf(values);
        }
        return List.copyOf(new LinkedHashSet<>(values));
    }
}
