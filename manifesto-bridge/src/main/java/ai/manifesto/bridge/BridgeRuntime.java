package ai.manifesto.bridge;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * BridgeRuntime - projection 실행기
 */
public final class BridgeRuntime {
    private final Projection defaultProjection;
    private final Map<SourceEvent.Kind, Projection> routedProjections;

    public BridgeRuntime(Projection projection) {
        this.defaultProjection = Objects.requireNonNull(projection, "projection is required");
        this.routedProjections = Map.of();
    }

    public BridgeRuntime(Map<SourceEvent.Kind, Projection> routes, Projection fallback) {
        Objects.requireNonNull(routes, "routes is required");
        EnumMap<SourceEvent.Kind, Projection> copied = new EnumMap<>(SourceEvent.Kind.class);
        for (Map.Entry<SourceEvent.Kind, Projection> entry : routes.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException("routes must not contain null key/value");
            }
            copied.put(entry.getKey(), entry.getValue());
        }
        this.routedProjections = Map.copyOf(copied);
        this.defaultProjection = fallback;
    }

    public ProjectionResult projectResult(SourceEvent event, Snapshot snapshot) {
        Objects.requireNonNull(event, "event is required");
        Objects.requireNonNull(snapshot, "snapshot is required");

        SnapshotView view = new SnapshotView(snapshot.getData(), snapshot.getComputed());
        Projection projection = routedProjections.get(event.kind());
        if (projection != null) {
            return projection.project(event, view);
        }
        if (defaultProjection == null) {
            throw new IllegalArgumentException("No projection route for event kind: " + event.kind());
        }
        return defaultProjection.project(event, view);
    }

    public Intent project(SourceEvent event, Snapshot snapshot) {
        ProjectionResult result = projectResult(event, snapshot);
        if (!result.hasIntent()) {
            throw new IllegalStateException("Projection produced no intent: " + result.getReason());
        }
        return result.getIntent();
    }
}
