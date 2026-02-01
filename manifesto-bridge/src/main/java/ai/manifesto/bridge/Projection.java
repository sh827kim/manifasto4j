package ai.manifesto.bridge;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;

/**
 * Projection - SourceEvent를 Intent로 투사
 */
@FunctionalInterface
public interface Projection {
    Intent project(SourceEvent event, SnapshotView view);
}
