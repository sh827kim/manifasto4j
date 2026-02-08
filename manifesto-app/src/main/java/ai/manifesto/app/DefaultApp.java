package ai.manifesto.app;

import ai.manifesto.core.ComputeResult;
import ai.manifesto.core.ComputeStatus;
import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.TraceGraph;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.HostRuntime;
import ai.manifesto.world.ManifestoWorld;
import ai.manifesto.world.ProposalResult;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.IntentBody;
import ai.manifesto.world.schema.IntentInstance;
import ai.manifesto.world.schema.IntentMeta;
import ai.manifesto.world.schema.IntentOrigin;
import ai.manifesto.world.schema.IntentSource;
import ai.manifesto.world.schema.ProposalStatus;
import ai.manifesto.world.schema.World;
import ai.manifesto.world.schema.WorldId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * DefaultApp - server/CLI용 최소 App 구현체
 */
public final class DefaultApp implements App {
    private final DomainSchema schema;
    private final HostRuntime host;
    private Snapshot snapshot;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private final ManifestoWorld world;
    private final ActorRef appActor;
    private WorldId currentWorldId;

    public DefaultApp(DomainSchema schema, Snapshot initialSnapshot, HostRuntime host) {
        this(schema, initialSnapshot, host, null, null);
    }

    public DefaultApp(
            DomainSchema schema,
            Snapshot initialSnapshot,
            HostRuntime host,
            ManifestoWorld world,
            ActorRef appActor
    ) {
        this.schema = Objects.requireNonNull(schema, "schema is required");
        this.snapshot = Objects.requireNonNull(initialSnapshot, "snapshot is required");
        this.host = Objects.requireNonNull(host, "host is required");
        this.world = world;
        this.appActor = appActor;
    }

    @Override
    public void ready() {
        if (world == null) {
            return;
        }

        World genesis = world.getStore().getGenesis();
        if (genesis == null) {
            genesis = world.createGenesis(snapshot);
        }
        this.currentWorldId = genesis.getWorldId();

        Snapshot genesisSnapshot = world.getStore().getSnapshot(genesis.getWorldId());
        if (genesisSnapshot != null) {
            this.snapshot = genesisSnapshot;
        }
    }

    @Override
    public ActionHandle act(Intent intent) throws Exception {
        if (world == null) {
            ComputeResult result = host.run(schema, snapshot, intent, 5);
            snapshot = result.getSnapshot();
            notifySubscribers(snapshot);
            return new ActionHandle(result);
        }

        if (appActor == null) {
            throw new IllegalStateException("World-enabled app requires appActor");
        }
        if (currentWorldId == null) {
            throw new IllegalStateException("App is not ready. Call ready() before act().");
        }

        IntentInstance intentInstance = new IntentInstance(
                new IntentBody(intent.getType(), intent.getInput(), null),
                intent.getIntentId(),
                "intent-key-" + intent.getIntentId(),
                new IntentMeta(new IntentOrigin(
                        "app:default",
                        new IntentSource("app", "event-" + intent.getIntentId()),
                        appActor
                ))
        );

        ProposalResult proposalResult = world.submitProposal(appActor.getActorId(), intentInstance, currentWorldId, null);

        if (proposalResult.getError() != null) {
            return new ActionHandle(ComputeResult.builder()
                    .snapshot(snapshot)
                    .trace((TraceGraph) null)
                    .status(ComputeStatus.ERROR)
                    .build());
        }

        if (proposalResult.getResultWorld() != null) {
            currentWorldId = proposalResult.getResultWorld().getWorldId();
            Snapshot terminalSnapshot = world.getStore().getSnapshot(currentWorldId);
            if (terminalSnapshot != null) {
                snapshot = terminalSnapshot;
                notifySubscribers(snapshot);
            }
        }

        ProposalStatus status = proposalResult.getProposal().getStatus();
        ComputeStatus computeStatus = toComputeStatus(status);

        return new ActionHandle(ComputeResult.builder()
                .snapshot(snapshot)
                .trace((TraceGraph) null)
                .status(computeStatus)
                .build());
    }

    @Override
    public void subscribe(Function<Snapshot, Object> selector, Consumer<Object> handler) {
        subscriptions.add(new Subscription(selector, handler));
        handler.accept(selector.apply(snapshot));
    }

    @Override
    public DomainSchema getSchema() {
        return schema;
    }

    @Override
    public Snapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public ManifestoWorld getWorld() {
        return world;
    }

    @Override
    public void switchBranch(WorldId worldId) {
        if (world == null) {
            throw new UnsupportedOperationException("World integration is not enabled for this app");
        }
        world.switchBranch(worldId);
        this.currentWorldId = worldId;

        Snapshot branchSnapshot = world.getStore().getSnapshot(worldId);
        if (branchSnapshot != null) {
            this.snapshot = branchSnapshot;
            notifySubscribers(snapshot);
        }
    }

    private ComputeStatus toComputeStatus(ProposalStatus status) {
        if (status == ProposalStatus.EVALUATING) {
            return ComputeStatus.PENDING;
        }
        if (status == ProposalStatus.REJECTED || status == ProposalStatus.FAILED) {
            return ComputeStatus.ERROR;
        }
        if (status == ProposalStatus.COMPLETED) {
            return ComputeStatus.COMPLETE;
        }
        if (status == ProposalStatus.EXECUTING) {
            return ComputeStatus.PENDING;
        }
        if (status == ProposalStatus.APPROVED) {
            return ComputeStatus.COMPLETE;
        }
        return ComputeStatus.PENDING;
    }

    private void notifySubscribers(Snapshot snapshot) {
        for (Subscription sub : subscriptions) {
            Object value = sub.selector.apply(snapshot);
            sub.handler.accept(value);
        }
    }

    private record Subscription(Function<Snapshot, Object> selector, Consumer<Object> handler) {}
}
