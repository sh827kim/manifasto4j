package ai.manifesto.world.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IntentScope {
    private final List<String> paths;

    public IntentScope(List<String> paths) {
        this.paths = Collections.unmodifiableList(new ArrayList<>(paths != null ? paths : List.of()));
    }

    public List<String> getPaths() {
        return paths;
    }
}
