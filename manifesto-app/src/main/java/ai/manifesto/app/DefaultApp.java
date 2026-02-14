package ai.manifesto.app;

import ai.manifesto.core.ComputeResult;
import ai.manifesto.core.ComputeStatus;
import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.TraceGraph;
import ai.manifesto.core.evaluator.ComputedEvaluator;
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
import ai.manifesto.world.types.IntentKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * KR: DefaultApp는 App 인터페이스의 기본 구현으로 snapshot 갱신, host 실행, world 연계를 조정합니다.
 * EN: DefaultApp is the default App implementation that coordinates snapshot updates, host execution, and world integration.
 */
public final class DefaultApp implements App {
    private final DomainSchema schema;
    private final HostRuntime host;
    private Snapshot snapshot;
    private final List<Subscription> subscriptions = new ArrayList<>();
    private final List<AppHook> hooks = new ArrayList<>();
    private final String sessionId;
    private final AppSnapshotStore snapshotStore;

    private final ManifestoWorld world;
    private final ActorRef appActor;
    private WorldId currentWorldId;

    public DefaultApp(DomainSchema schema, Snapshot initialSnapshot, HostRuntime host) {
        this(schema, initialSnapshot, host, null, null, null, null);
    }

    public DefaultApp(
            DomainSchema schema,
            Snapshot initialSnapshot,
            HostRuntime host,
            ManifestoWorld world,
            ActorRef appActor
    ) {
        this(schema, initialSnapshot, host, world, appActor, null, null);
    }

    public DefaultApp(
            DomainSchema schema,
            Snapshot initialSnapshot,
            HostRuntime host,
            ManifestoWorld world,
            ActorRef appActor,
            String sessionId,
            AppSnapshotStore snapshotStore
    ) {
        this.schema = Objects.requireNonNull(schema, "schema is required");
        this.snapshot = Objects.requireNonNull(initialSnapshot, "snapshot is required");
        this.host = Objects.requireNonNull(host, "host is required");
        this.world = world;
        this.appActor = appActor;
        this.sessionId = sessionId;
        this.snapshotStore = snapshotStore;
        Snapshot restored = loadSessionSnapshot();
        if (restored != null) {
            this.snapshot = restored;
        }
    }

    @Override
    public void ready() {
        if (world == null) {
            emitReadyHook();
            return;
        }

        World genesis = world.getStore().getGenesis();
        if (genesis == null) {
            Snapshot genesisSnapshot = evaluateGenesisComputed(snapshot);
            genesis = world.createGenesis(genesisSnapshot);
        }
        this.currentWorldId = genesis.getWorldId();

        Snapshot genesisSnapshot = world.getStore().getSnapshot(genesis.getWorldId());
        if (genesisSnapshot != null) {
            this.snapshot = genesisSnapshot;
            persistSessionSnapshot();
        }
        emitReadyHook();
    }

    @Override
    public ActionHandle act(Intent intent) throws Exception {
        emitBeforeActHook(intent);
        List<ActionUpdate> updates = new ArrayList<>();
        appendUpdate(intent, updates, ActionPhase.PREPARING, "Preparing action");

        if (world == null) {
            appendUpdate(intent, updates, ActionPhase.EXECUTING, "Executing action via HostRuntime");
            ComputeResult result = host.run(schema, snapshot, intent, 5);
            snapshot = result.getSnapshot();
            persistSessionSnapshot();
            notifySubscribers(snapshot);
            appendTerminalUpdate(intent, updates, result.getStatus(), null);
            ActionHandle handle = new ActionHandle(result, updates);
            emitAfterActHook(intent, handle);
            return handle;
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
                IntentKeys.computeIntentKey(world.getSchemaHash(), new IntentBody(intent.getType(), intent.getInput(), null)),
                new IntentMeta(new IntentOrigin(
                        "app:default",
                        new IntentSource("app", "event-" + intent.getIntentId()),
                        appActor
                ))
        );

        ProposalResult proposalResult = world.submitProposal(appActor.getActorId(), intentInstance, currentWorldId, null);
        appendUpdate(intent, updates, ActionPhase.SUBMITTED, "Proposal submitted to world");

        if (proposalResult.getError() != null) {
            appendUpdate(intent, updates, ActionPhase.FAILED, "World proposal failed: " + proposalResult.getError());
            ActionHandle handle = new ActionHandle(ComputeResult.builder()
                    .snapshot(snapshot)
                    .trace((TraceGraph) null)
                    .status(ComputeStatus.ERROR)
                    .build(), updates);
            emitAfterActHook(intent, handle);
            return handle;
        }

        if (proposalResult.getResultWorld() != null) {
            appendUpdate(intent, updates, ActionPhase.EXECUTING, "Proposal approved and executed");
            currentWorldId = proposalResult.getResultWorld().getWorldId();
            Snapshot terminalSnapshot = world.getStore().getSnapshot(currentWorldId);
            if (terminalSnapshot != null) {
                snapshot = terminalSnapshot;
                persistSessionSnapshot();
                notifySubscribers(snapshot);
            }
        }

        ProposalStatus status = proposalResult.getProposal().getStatus();
        ComputeStatus computeStatus = toComputeStatus(status);

        List<ActionUpdate> finalized = finalizeWorldUpdates(intent, updates, status);
        ActionHandle handle = new ActionHandle(ComputeResult.builder()
                .snapshot(snapshot)
                .trace((TraceGraph) null)
                .status(computeStatus)
                .build(), finalized);
        emitAfterActHook(intent, handle);
        return handle;
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
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean hasSessionPersistence() {
        return sessionId != null && snapshotStore != null;
    }

    @Override
    public ManifestoWorld getWorld() {
        return world;
    }

    @Override
    public WorldId getCurrentBranchId() {
        return currentWorldId;
    }

    @Override
    public List<WorldId> listBranches() {
        if (world == null) {
            return List.of();
        }
        return world.getStore().listWorlds().stream()
            .map(World::getWorldId)
            .toList();
    }

    @Override
    public void addHook(AppHook hook) {
        if (hook != null) {
            hooks.add(hook);
        }
    }

    @Override
    public void removeHook(AppHook hook) {
        hooks.remove(hook);
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
            persistSessionSnapshot();
            notifySubscribers(snapshot);
        }
        emitBranchSwitchedHook(worldId);
    }

    private Snapshot loadSessionSnapshot() {
        if (sessionId == null || snapshotStore == null) {
            return null;
        }
        return snapshotStore.load(sessionId);
    }

    private void persistSessionSnapshot() {
        if (sessionId == null || snapshotStore == null) {
            return;
        }
        snapshotStore.save(sessionId, snapshot);
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

    private Snapshot evaluateGenesisComputed(Snapshot baseSnapshot) {
        var computedResult = ComputedEvaluator.evaluateComputed(schema, baseSnapshot);
        if (computedResult.isOk()) {
            return baseSnapshot.withComputed(computedResult.unwrap());
        }
        return baseSnapshot;
    }

    private record Subscription(Function<Snapshot, Object> selector, Consumer<Object> handler) {}

    private List<ActionUpdate> finalizeWorldUpdates(Intent intent, List<ActionUpdate> updates, ProposalStatus status) {
        if (status == ProposalStatus.COMPLETED || status == ProposalStatus.APPROVED) {
            appendUpdate(intent, updates, ActionPhase.COMPLETED, "World proposal completed");
            return updates;
        }
        if (status == ProposalStatus.REJECTED) {
            appendUpdate(intent, updates, ActionPhase.REJECTED, "World proposal rejected");
            return updates;
        }
        if (status == ProposalStatus.FAILED) {
            appendUpdate(intent, updates, ActionPhase.FAILED, "World proposal failed");
            return updates;
        }
        appendUpdate(intent, updates, ActionPhase.EXECUTING, "World proposal pending: " + status);
        return updates;
    }

    private void appendTerminalUpdate(Intent intent, List<ActionUpdate> updates, ComputeStatus status, String message) {
        if (status == ComputeStatus.COMPLETE || status == ComputeStatus.HALTED) {
            appendUpdate(intent, updates, ActionPhase.COMPLETED, message == null ? "Action completed" : message);
            return;
        }
        if (status == ComputeStatus.ERROR) {
            appendUpdate(intent, updates, ActionPhase.FAILED, message == null ? "Action failed" : message);
            return;
        }
        appendUpdate(intent, updates, ActionPhase.EXECUTING, message == null ? "Action is still running" : message);
    }

    private void appendUpdate(Intent intent, List<ActionUpdate> updates, ActionPhase phase, String message) {
        ActionUpdate update = new ActionUpdate(phase, message, System.currentTimeMillis());
        updates.add(update);
        emitActionUpdateHook(intent, update);
    }

    private void emitReadyHook() {
        for (AppHook hook : hooks) {
            hook.onReady(snapshot);
        }
    }

    private void emitBeforeActHook(Intent intent) {
        for (AppHook hook : hooks) {
            hook.onBeforeAct(intent, snapshot);
        }
    }

    private void emitActionUpdateHook(Intent intent, ActionUpdate update) {
        for (AppHook hook : hooks) {
            hook.onActionUpdate(intent, update, snapshot);
        }
    }

    private void emitAfterActHook(Intent intent, ActionHandle handle) {
        for (AppHook hook : hooks) {
            hook.onAfterAct(intent, handle, snapshot);
        }
    }

    private void emitBranchSwitchedHook(WorldId worldId) {
        for (AppHook hook : hooks) {
            hook.onBranchSwitched(worldId, snapshot);
        }
    }
}
