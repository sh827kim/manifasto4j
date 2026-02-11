package ai.manifesto.world.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KR: ProposalTrace는 제안 처리 흐름의 상태와 메타데이터를 표현하는 도메인 타입입니다.
 * EN: ProposalTrace is a domain type that represents state and metadata in proposal processing flow.
 */
public final class ProposalTrace {
    private final String summary;
    private final String reasoning;
    private final Map<String, Object> context;

    public ProposalTrace(String summary, String reasoning, Map<String, Object> context) {
        this.summary = Objects.requireNonNull(summary, "summary is required");
        this.reasoning = reasoning;
        this.context = Collections.unmodifiableMap(new LinkedHashMap<>(context != null ? context : Map.of()));
    }

    public String getSummary() {
        return summary;
    }

    public String getReasoning() {
        return reasoning;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
