package ai.manifesto.world.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KR: IntentBody는 World 스키마 계층에서 intent body 역할을 수행하는 구현 타입입니다.
 * EN: IntentBody is an implementation type performing intent body roles in the World schema layer.
 */
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
