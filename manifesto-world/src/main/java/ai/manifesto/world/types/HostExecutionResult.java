package ai.manifesto.world.types;

import ai.manifesto.core.ErrorValue;
import ai.manifesto.core.Snapshot;
import ai.manifesto.world.schema.ArtifactRef;

import java.util.Objects;

/**
 * KR: HostExecutionResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: HostExecutionResult is a result type carrying operation or execution outcomes.
 */
public final class HostExecutionResult {
    public enum Outcome {
        COMPLETED,
        FAILED
    }

    private final Outcome outcome;
    private final Snapshot terminalSnapshot;
    private final ArtifactRef traceRef;
    private final ErrorValue error;

    public HostExecutionResult(Outcome outcome, Snapshot terminalSnapshot, ArtifactRef traceRef, ErrorValue error) {
        this.outcome = Objects.requireNonNull(outcome, "outcome is required");
        this.terminalSnapshot = Objects.requireNonNull(terminalSnapshot, "terminalSnapshot is required");
        this.traceRef = traceRef;
        this.error = error;
    }

    public static HostExecutionResult completed(Snapshot terminalSnapshot) {
        return new HostExecutionResult(Outcome.COMPLETED, terminalSnapshot, null, null);
    }

    public static HostExecutionResult failed(Snapshot terminalSnapshot, ErrorValue error) {
        return new HostExecutionResult(Outcome.FAILED, terminalSnapshot, null, error);
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public Snapshot getTerminalSnapshot() {
        return terminalSnapshot;
    }

    public ArtifactRef getTraceRef() {
        return traceRef;
    }

    public ErrorValue getError() {
        return error;
    }
}
