package ai.manifesto.host;

import ai.manifesto.core.Requirement;
import ai.manifesto.core.Snapshot;
import ai.manifesto.host.runtime.HostRuntimeTraceEvent;
import ai.manifesto.host.runtime.HostRuntimeTraceSink;

/**
 * KR: effect 재시도/타임아웃/에러 경계를 포함해 핸들러를 실행하는 실행기입니다.
 * EN: Executor that runs effect handlers with retry/timeout/error boundaries.
 */
public final class EffectExecutor {

    public EffectExecutionOutcome execute(
        EffectHandler handler,
        Requirement requirement,
        String executionKey,
        String intentId,
        int computeIteration,
        Snapshot snapshot,
        HostRuntimeOptions options,
        HostRuntimeTraceSink traceSink
    ) {
        int attempts = options.getMaxEffectRetries() + 1;
        EffectExecutionError lastError = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            EffectExecutionContext context = options.getContextProvider().create(
                requirement,
                executionKey,
                intentId,
                computeIteration,
                attempt,
                snapshot
            );
            traceSink.onEvent(new HostRuntimeTraceEvent(
                "effect:attempt",
                executionKey,
                requirement.getType(),
                null,
                null,
                System.currentTimeMillis()
            ));

            try {
                long startedAt = System.nanoTime();
                EffectResult result = invoke(handler, requirement, context);
                long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;

                long maxDuration = options.getMaxEffectDurationMillis();
                if (maxDuration > 0L && elapsedMillis > maxDuration) {
                    lastError = new EffectExecutionError(
                        EffectExecutionErrorCode.TIMEOUT,
                        "Effect exceeded max duration: " + elapsedMillis + "ms",
                        attempt,
                        true
                    );
                    traceSink.onEvent(new HostRuntimeTraceEvent(
                        "effect:retry",
                        executionKey,
                        requirement.getType(),
                        null,
                        null,
                        System.currentTimeMillis()
                    ));
                    continue;
                }

                if (result == null) {
                    lastError = new EffectExecutionError(
                        EffectExecutionErrorCode.NULL_RESULT,
                        "Effect handler returned null result",
                        attempt,
                        false
                    );
                    traceSink.onEvent(new HostRuntimeTraceEvent(
                        "effect:failure",
                        executionKey,
                        requirement.getType(),
                        null,
                        null,
                        System.currentTimeMillis()
                    ));
                    return EffectExecutionOutcome.failure(lastError);
                }

                traceSink.onEvent(new HostRuntimeTraceEvent(
                    "effect:success",
                    executionKey,
                    requirement.getType(),
                    null,
                    null,
                    System.currentTimeMillis()
                ));
                return EffectExecutionOutcome.success(result);
            } catch (RuntimeException error) {
                lastError = new EffectExecutionError(
                    EffectExecutionErrorCode.HANDLER_EXCEPTION,
                    error.getMessage(),
                    attempt,
                    true
                );
                traceSink.onEvent(new HostRuntimeTraceEvent(
                    "effect:retry",
                    executionKey,
                    requirement.getType(),
                    null,
                    null,
                    System.currentTimeMillis()
                ));
            }
        }

        EffectExecutionError exhausted = new EffectExecutionError(
            EffectExecutionErrorCode.RETRY_EXHAUSTED,
            lastError == null ? "Effect execution failed" : lastError.message(),
            attempts,
            false
        );
        traceSink.onEvent(new HostRuntimeTraceEvent(
            "effect:failure",
            executionKey,
            requirement.getType(),
            null,
            null,
            System.currentTimeMillis()
        ));
        return EffectExecutionOutcome.failure(exhausted);
    }

    private EffectResult invoke(EffectHandler handler, Requirement requirement, EffectExecutionContext context) {
        if (handler instanceof ContextAwareEffectHandler awareHandler) {
            return awareHandler.handle(requirement.getParams(), context);
        }
        return handler.handle(requirement.getParams());
    }
}
