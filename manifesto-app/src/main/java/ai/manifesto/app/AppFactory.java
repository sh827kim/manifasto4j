package ai.manifesto.app;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.HostRuntime;
import ai.manifesto.world.ManifestoWorld;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AutoApprovePolicy;
import ai.manifesto.world.types.HostExecutionOptions;
import ai.manifesto.world.types.HostExecutionResult;
import ai.manifesto.world.types.HostExecutor;

import java.util.Objects;

/**
 * AppFactory - server/CLI용 App 생성
 */
public final class AppFactory {
    private AppFactory() {}

    public static App createApp(DomainSchema schema, Snapshot initialSnapshot, HostRuntime host) {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(initialSnapshot, "initialSnapshot is required");
        Objects.requireNonNull(host, "host is required");
        return new DefaultApp(schema, initialSnapshot, host);
    }

    public static App createWorldApp(
            DomainSchema schema,
            Snapshot initialSnapshot,
            HostRuntime host,
            String actorId,
            ActorKind actorKind
    ) {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(initialSnapshot, "initialSnapshot is required");
        Objects.requireNonNull(host, "host is required");
        Objects.requireNonNull(actorId, "actorId is required");
        Objects.requireNonNull(actorKind, "actorKind is required");

        HostExecutor executor = new HostExecutor() {
            @Override
            public HostExecutionResult execute(String executionKey, Snapshot baseSnapshot, Intent intent, HostExecutionOptions options) {
                try {
                    var result = host.run(schema, baseSnapshot, intent, 5);
                    return new HostExecutionResult(
                            result.getStatus() == ai.manifesto.core.ComputeStatus.ERROR ? HostExecutionResult.Outcome.FAILED : HostExecutionResult.Outcome.COMPLETED,
                            result.getSnapshot(),
                            null,
                            result.getSnapshot().getSystem().getLastError()
                    );
                } catch (Exception e) {
                    return new HostExecutionResult(HostExecutionResult.Outcome.FAILED, baseSnapshot, null, null);
                }
            }
        };

        ManifestoWorld world = new ManifestoWorld(schema.getHash(), executor, null);
        ActorRef actor = new ActorRef(actorId, actorKind);
        world.registerActor(actor, new AutoApprovePolicy("app default actor"));

        return new DefaultApp(schema, initialSnapshot, host, world, actor);
    }
}
