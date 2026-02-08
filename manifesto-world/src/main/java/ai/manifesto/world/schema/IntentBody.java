package ai.manifesto.world.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class IntentBody {
    private final String type;
    private final Map<String, Object> input;
    private final IntentScope scopeProposal;

    public IntentBody(String type, Map<String, Object> input, IntentScope scopeProposal) {
        this.type = Objects.requireNonNull(type, "type is required");
        this.input = Collections.unmodifiableMap(new LinkedHashMap<>(input != null ? input : Map.of()));
        this.scopeProposal = scopeProposal;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public IntentScope getScopeProposal() {
        return scopeProposal;
    }
}
