package ai.manifesto.bridge;

import ai.manifesto.core.Intent;

import java.util.Objects;

/**
 * ProjectionResult expresses projection outcome:
 * - intent: projection produced an executable intent
 * - none: projection intentionally skipped intent emission
 */
public final class ProjectionResult {
    private final Intent intent;
    private final String reason;

    private ProjectionResult(Intent intent, String reason) {
        this.intent = intent;
        this.reason = reason;
    }

    public static ProjectionResult intent(Intent intent) {
        return new ProjectionResult(Objects.requireNonNull(intent, "intent is required"), null);
    }

    public static ProjectionResult none(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required for none result");
        }
        return new ProjectionResult(null, reason);
    }

    public boolean hasIntent() {
        return intent != null;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getReason() {
        return reason;
    }
}
