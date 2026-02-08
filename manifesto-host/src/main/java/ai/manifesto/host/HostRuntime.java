package ai.manifesto.host;

import ai.manifesto.core.*;
import ai.manifesto.core.core.Apply;
import ai.manifesto.core.core.Compute;
import ai.manifesto.core.schema.DomainSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HostRuntime - compute-effect loop 최소 구현
 */
public final class HostRuntime {
    private final Map<String, EffectHandler> handlers = new HashMap<>();

    public HostRuntime register(String effectType, EffectHandler handler) {
        Objects.requireNonNull(effectType, "effectType is required");
        Objects.requireNonNull(handler, "handler is required");
        handlers.put(effectType, handler);
        return this;
    }

    public ComputeResult run(DomainSchema schema, Snapshot snapshot, Intent intent, int timeoutSeconds)
        throws Exception {
        return run(schema, snapshot, intent, HostRuntimeOptions.forTimeoutSeconds(timeoutSeconds));
    }

    public ComputeResult run(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent,
        HostRuntimeOptions options
    ) throws Exception {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(snapshot, "snapshot is required");
        Objects.requireNonNull(intent, "intent is required");
        Objects.requireNonNull(options, "options is required");

        Snapshot current = snapshot;
        int iteration = 0;
        TraceGraph lastTrace = null;
        while (true) {
            if (iteration >= options.getMaxIterations()) {
                return ComputeResult.error(current, lastTrace);
            }
            iteration += 1;
            HostContext computeContext = HostContext.forSnapshot(current);
            ComputeResult result = Compute.computeSync(
                schema,
                current,
                intent,
                computeContext,
                options.getTimeoutSeconds()
            );
            lastTrace = result.getTrace();
            if (result.getStatus() != ComputeStatus.PENDING) {
                return result;
            }

            List<Requirement> requirements = result.getRequirements();
            if (requirements.isEmpty()) {
                return result;
            }
            List<Patch> patches = new java.util.ArrayList<>();
            for (Requirement requirement : requirements) {
                EffectHandler handler = handlers.get(requirement.getType());
                if (handler == null) {
                    return result;
                }
                EffectResult effectResult = handler.handle(requirement.getParams());
                patches.addAll(effectResult.getPatches());
            }
            // Host는 처리된 requirement를 명시적으로 비운다.
            patches.add(Patch.set("system.pendingRequirements", java.util.List.of()));

            HostContext applyContext = HostContext.forSnapshot(result.getSnapshot());
            Result<Snapshot, ErrorValue> applied = Apply.apply(schema, result.getSnapshot(), patches, applyContext);
            if (applied.isErr()) {
                return ComputeResult.error(result.getSnapshot(), lastTrace);
            }
            current = applied.unwrap();
        }
    }
}
