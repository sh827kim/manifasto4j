package ai.manifesto.world.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * KR: IntentScope는 World 스키마 계층에서 intent scope 역할을 수행하는 구현 타입입니다.
 * EN: IntentScope is an implementation type performing intent scope roles in the World schema layer.
 */
public final class IntentScope {
    private final List<String> paths;

    public IntentScope(List<String> paths) {
        this.paths = Collections.unmodifiableList(new ArrayList<>(paths != null ? paths : List.of()));
    }

    public List<String> getPaths() {
        return paths;
    }
}
