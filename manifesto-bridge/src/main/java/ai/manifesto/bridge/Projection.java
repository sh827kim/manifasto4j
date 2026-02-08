package ai.manifesto.bridge;

/**
 * Projection converts SourceEvent to ProjectionResult.
 */
@FunctionalInterface
public interface Projection {
    ProjectionResult project(SourceEvent event, SnapshotView view);
}
