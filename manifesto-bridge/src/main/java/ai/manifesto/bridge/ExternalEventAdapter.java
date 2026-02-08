package ai.manifesto.bridge;

/**
 * ExternalEventAdapter maps external framework events into SourceEvent.
 *
 * @param <T> external event type
 */
@FunctionalInterface
public interface ExternalEventAdapter<T> {
    SourceEvent adapt(T event);
}
