package ai.manifesto.host;

import ai.manifesto.core.Requirement;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KR: HostError 생성/매핑 유틸리티입니다.
 * EN: Utility for creating and mapping HostError values.
 */
public final class HostErrors {
    private HostErrors() {
    }

    public static HostError intentIdMissing() {
        return HostError.of(HostErrorCode.INTENT_ID_MISSING, "intent.intentId is required");
    }

    public static HostError handlerNotFound(Requirement requirement) {
        Map<String, Object> details = new LinkedHashMap<>();
        if (requirement != null) {
            details.put("requirementId", requirement.getId());
            details.put("requirementType", requirement.getType());
        }
        return HostError.of(HostErrorCode.HANDLER_NOT_FOUND, "Effect handler is not registered", details);
    }

    public static HostError effectFailure(Requirement requirement, EffectExecutionError error) {
        Map<String, Object> details = new LinkedHashMap<>();
        if (requirement != null) {
            details.put("requirementId", requirement.getId());
            details.put("requirementType", requirement.getType());
        }
        if (error != null) {
            details.put("effectErrorCode", error.code().name());
            details.put("attempts", error.attempts());
            details.put("retryable", error.retryable());
        }
        return HostError.of(
            HostErrorCode.EFFECT_EXECUTION_FAILED,
            error == null ? "Effect execution failed" : error.message(),
            details
        );
    }

    public static HostError iterationLimitExceeded(int maxIterations) {
        return HostError.of(
            HostErrorCode.ITERATION_LIMIT_EXCEEDED,
            "Host runtime exceeded max iterations: " + maxIterations,
            Map.of("maxIterations", maxIterations)
        );
    }
}
