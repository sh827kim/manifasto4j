package ai.manifesto.bridge;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;

import java.util.Objects;

/**
 * BridgeRuntime - projection 실행기
 */
public final class BridgeRuntime {
    private final Projection projection;

    public BridgeRuntime(Projection projection) {
        this.projection = Objects.requireNonNull(projection, "projection is required");
    }

    public Intent project(SourceEvent event, Snapshot snapshot) {
        Objects.requireNonNull(event, "event is required");
        Objects.requireNonNull(snapshot, "snapshot is required");

        SnapshotView view = new SnapshotView(snapshot.getData(), snapshot.getComputed());
        return projection.project(event, view);
    }
}
