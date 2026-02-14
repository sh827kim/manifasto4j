package ai.manifesto.host;

import ai.manifesto.core.Requirement;
import ai.manifesto.core.Snapshot;

/**
 * KR: requirement와 런타임 상태에서 effect execution context를 생성하는 계약입니다.
 * EN: Contract creating effect execution context from requirement and runtime state.
 */
@FunctionalInterface
public interface EffectContextProvider {
    EffectExecutionContext create(
        Requirement requirement,
        String executionKey,
        String intentId,
        int computeIteration,
        int attempt,
        Snapshot snapshot
    );

    static EffectContextProvider defaultProvider() {
        return (requirement, executionKey, intentId, computeIteration, attempt, snapshot) -> {
            String requirementId = requirement == null || requirement.getId() == null ? "unknown" : requirement.getId();
            String requirementType = requirement == null || requirement.getType() == null ? "unknown" : requirement.getType();
            return new EffectExecutionContext(executionKey, intentId, requirementId, requirementType, computeIteration, attempt);
        };
    }
}
