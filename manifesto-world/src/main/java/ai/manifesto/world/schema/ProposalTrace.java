package ai.manifesto.world.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
