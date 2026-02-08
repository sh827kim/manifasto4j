package ai.manifesto.bridge;

/**
 * ExternalEventAdapter maps external runtime events into {@link SourceEvent}.
 *
 * <p>Implementation contract:
 * 1) Input validation
 *    - Reject null input with {@link NullPointerException} or {@link IllegalArgumentException}.
 *    - Reject structurally invalid input (missing mandatory source fields) with {@link IllegalArgumentException}.
 * 2) SourceEvent mapping
 *    - {@code kind}: map external source/channel into one of {@link SourceEvent.Kind}.
 *    - {@code eventId}: provide a stable identifier for dedup/audit. Generate one when absent.
 *    - {@code payload}: preserve business payload as-is when possible and keep map keys deterministic.
 *    - {@code occurredAt}: map original event timestamp in epoch millis when available; otherwise null.
 * 3) Determinism and side effects
 *    - {@code adapt} must be pure (no I/O, no global mutable state updates).
 *    - For the same input, output must be equivalent except intentionally generated fallback IDs.
 * 4) Error boundary
 *    - Do not swallow parse/validation errors. Throw explicit exceptions with actionable messages.
 *
 * <p>This interface is framework-neutral by design. Concrete adapters should live in integration
 * modules (e.g., Spring AI, Kafka, HTTP gateway), not in core bridge runtime.
 *
 * @param <T> external event input type
 */
@FunctionalInterface
public interface ExternalEventAdapter<T> {
    SourceEvent adapt(T event);
}
