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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final List<AppPlugin> plugins = new ArrayList<>();
    private final Map<String, WorldId> branchAliases = new LinkedHashMap<>();
    private final String sessionId;
    private final AppSnapshotStore snapshotStore;

    private final ManifestoWorld world;
    private final ActorRef appActor;
    private final AppPolicyService policyService;
    private final AppWorldStore worldStore;
    private final SystemFacade systemFacade;
    private final MemoryFacade memoryFacade;

    private AppStatus status = AppStatus.CREATED;
    private WorldId currentWorldId;
    private String currentBranchName = "main";

    public DefaultApp(DomainSchema schema, Snapshot initialSnapshot, HostRuntime host) {
        this(schema, initialSnapshot, host, null, null, null, null, null, null);
    }

    public DefaultApp(
            DomainSchema schema,
            Snapshot initialSnapshot,
            HostRuntime host,
            ManifestoWorld world,
            ActorRef appActor
    ) {
        this(schema, initialSnapshot, host, world, appActor, null, null, null, null);
    }

    public DefaultApp(
            DomainSchema schema,
            Snapshot initialSnapshot,
            HostRuntime host,
            ManifestoWorld world,
            ActorRef appActor,
            String sessionId,
            AppSnapshotStore snapshotStore,
            AppPolicyService policyService,
            AppWorldStore worldStore
    ) {
        this.schema = Objects.requireNonNull(schema, "schema is required");
        this.snapshot = Objects.requireNonNull(initialSnapshot, "snapshot is required");
        this.host = Objects.requireNonNull(host, "host is required");
        this.world = world;
        this.appActor = appActor;
        this.sessionId = sessionId;
        this.snapshotStore = snapshotStore;
        this.policyService = policyService == null ? new AllowAllPolicyService() : policyService;
        this.worldStore = worldStore;
        this.systemFacade = new DefaultSystemFacade(this);
        this.memoryFacade = new InMemoryMemoryFacade();

        Snapshot restored = loadSessionSnapshot();
        if (restored != null) {
            this.snapshot = restored;
        }
    }

    @Override
    public void ready() {
        ensureNotDisposed();
        if (world == null) {
            status = AppStatus.READY;
            for (AppPlugin plugin : plugins) {
                plugin.onInit(this);
            }
            emitReadyHook();
            return;
        }

        World genesis = world.getStore().getGenesis();
        if (genesis == null) {
            Snapshot genesisSnapshot = evaluateGenesisComputed(snapshot);
            genesis = world.createGenesis(genesisSnapshot);
        }
        this.currentWorldId = genesis.getWorldId();
        branchAliases.putIfAbsent("main", currentWorldId);

        Snapshot genesisSnapshot = world.getStore().getSnapshot(genesis.getWorldId());
        if (genesisSnapshot != null) {
            this.snapshot = genesisSnapshot;
            persistSessionSnapshot();
            persistWorldStoreState(currentBranchName, currentWorldId, genesisSnapshot);
        }
        status = AppStatus.READY;
        for (AppPlugin plugin : plugins) {
            plugin.onInit(this);
        }
        emitReadyHook();
    }

    @Override
    public void dispose() {
        if (status == AppStatus.DISPOSED) {
            return;
        }
        status = AppStatus.DISPOSING;
        for (AppPlugin plugin : plugins) {
            plugin.onDispose(this);
        }
        plugins.clear();
        hooks.clear();
        subscriptions.clear();
        status = AppStatus.DISPOSED;
    }

    @Override
    public ActionHandle act(Intent intent) throws Exception {
        ensureReady();
        ensureNotDisposed();
        Objects.requireNonNull(intent, "intent is required");

        RuntimeKind runtimeKind = intent.getType() != null && intent.getType().startsWith("system.")
            ? RuntimeKind.SYSTEM
            : RuntimeKind.DOMAIN;
        ActionHandle handle = ActionHandle.start(runtimeKind);

        AppPolicyService.PolicyDecision policyDecision = policyService.decide(intent, snapshot);
        if (!policyDecision.allowed()) {
            appendUpdate(intent, handle, ActionPhase.REJECTED, "Rejected by policy: " + policyDecision.reason());
            ComputeResult result = ComputeResult.builder()
                .snapshot(snapshot)
                .trace((TraceGraph) null)
                .status(ComputeStatus.ERROR)
                .build();
            handle.complete(result, new RejectedActionResult(policyDecision.reason(), runtimeKind));
            emitAfterActHook(intent, handle);
            return handle;
        }

        for (AppPlugin plugin : sortedPlugins()) {
            plugin.beforeAct(intent, snapshot);
        }
        emitBeforeActHook(intent);
        appendUpdate(intent, handle, ActionPhase.PREPARING, "Preparing action");

        if (world == null) {
            appendUpdate(intent, handle, ActionPhase.EXECUTING, "Executing action via HostRuntime");
            ComputeResult result = host.run(schema, snapshot, intent, 5);
            snapshot = result.getSnapshot();
            persistSessionSnapshot();
            notifySubscribers(snapshot);
            appendTerminalUpdate(intent, handle, result.getStatus(), null);
            ActionResult actionResult = toActionResult(result.getStatus(), runtimeKind, null, null);
            handle.complete(result, actionResult);
            for (AppPlugin plugin : sortedPlugins()) {
                plugin.afterAct(intent, handle, snapshot);
            }
            emitAfterActHook(intent, handle);
            return handle;
        }

        if (appActor == null) {
            ActionResult failure = new PreparationFailedActionResult("world_actor_missing", runtimeKind);
            ComputeResult result = ComputeResult.builder()
                .snapshot(snapshot)
                .trace((TraceGraph) null)
                .status(ComputeStatus.ERROR)
                .build();
            appendUpdate(intent, handle, ActionPhase.PREPARATION_FAILED, "World-enabled app requires appActor");
            handle.complete(result, failure);
            for (AppPlugin plugin : sortedPlugins()) {
                plugin.afterAct(intent, handle, snapshot);
            }
            emitAfterActHook(intent, handle);
            return handle;
        }
        if (currentWorldId == null) {
            ActionResult failure = new PreparationFailedActionResult("app_not_ready", runtimeKind);
            ComputeResult result = ComputeResult.builder()
                .snapshot(snapshot)
                .trace((TraceGraph) null)
                .status(ComputeStatus.ERROR)
                .build();
            appendUpdate(intent, handle, ActionPhase.PREPARATION_FAILED, "App is not ready. Call ready() before act().");
            handle.complete(result, failure);
            for (AppPlugin plugin : sortedPlugins()) {
                plugin.afterAct(intent, handle, snapshot);
            }
            emitAfterActHook(intent, handle);
            return handle;
        }

        IntentBody body = new IntentBody(intent.getType(), intent.getInput(), null);
        IntentInstance intentInstance = new IntentInstance(
            body,
            intent.getIntentId(),
            IntentKeys.computeIntentKey(world.getSchemaHash(), body),
            new IntentMeta(new IntentOrigin(
                "app:default",
                new IntentSource("app", "event-" + intent.getIntentId()),
                appActor
            ))
        );

        ProposalResult proposalResult = world.submitProposal(appActor.getActorId(), intentInstance, currentWorldId, null);
        appendUpdate(intent, handle, ActionPhase.SUBMITTED, "Proposal submitted to world");

        if (proposalResult.getError() != null) {
            appendUpdate(intent, handle, ActionPhase.FAILED, "World proposal failed: " + proposalResult.getError());
            ComputeResult errorResult = ComputeResult.builder()
                .snapshot(snapshot)
                .trace((TraceGraph) null)
                .status(ComputeStatus.ERROR)
                .build();
            ActionResult actionResult = new FailedActionResult(proposalResult.getError(), worldIdValue(currentWorldId), runtimeKind);
            handle.complete(errorResult, actionResult);
            for (AppPlugin plugin : sortedPlugins()) {
                plugin.afterAct(intent, handle, snapshot);
            }
            emitAfterActHook(intent, handle);
            return handle;
        }

        if (proposalResult.getResultWorld() != null) {
            appendUpdate(intent, handle, ActionPhase.EXECUTING, "Proposal approved and executed");
            currentWorldId = proposalResult.getResultWorld().getWorldId();
            branchAliases.put(currentBranchName, currentWorldId);
            Snapshot terminalSnapshot = world.getStore().getSnapshot(currentWorldId);
            if (terminalSnapshot != null) {
                snapshot = terminalSnapshot;
                persistSessionSnapshot();
                persistWorldStoreState(currentBranchName, currentWorldId, terminalSnapshot);
                notifySubscribers(snapshot);
            }
        }

        ProposalStatus proposalStatus = proposalResult.getProposal().getStatus();
        appendWorldTerminalUpdate(intent, handle, proposalStatus);

        ComputeStatus computeStatus = toComputeStatus(proposalStatus);
        ComputeResult result = ComputeResult.builder()
            .snapshot(snapshot)
            .trace((TraceGraph) null)
            .status(computeStatus)
            .build();
        ActionResult actionResult = toActionResult(
            computeStatus,
            runtimeKind,
            worldIdValue(currentWorldId),
            proposalResult.getError()
        );
        handle.complete(result, actionResult);
        for (AppPlugin plugin : sortedPlugins()) {
            plugin.afterAct(intent, handle, snapshot);
        }
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
    public AppStatus getStatus() {
        return status;
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
    public String getCurrentBranchName() {
        return currentBranchName;
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
    public List<String> listBranchNames() {
        return List.copyOf(branchAliases.keySet());
    }

    @Override
    public void createBranch(String branchName, WorldId worldId) {
        Objects.requireNonNull(branchName, "branchName is required");
        Objects.requireNonNull(worldId, "worldId is required");
        if (branchName.isBlank()) {
            throw new IllegalArgumentException("branchName must not be blank");
        }
        branchAliases.put(branchName.trim(), worldId);
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
            persistWorldStoreState(currentBranchName, worldId, branchSnapshot);
        }
        emitBranchSwitchedHook(worldId);
    }

    @Override
    public void switchBranch(String branchName) {
        Objects.requireNonNull(branchName, "branchName is required");
        WorldId worldId = branchAliases.get(branchName);
        if (worldId == null) {
            throw new IllegalArgumentException("Unknown branch alias: " + branchName);
        }
        currentBranchName = branchName;
        switchBranch(worldId);
    }

    @Override
    public SystemFacade getSystemFacade() {
        return systemFacade;
    }

    @Override
    public MemoryFacade getMemoryFacade() {
        return memoryFacade;
    }

    @Override
    public AppPolicyService getPolicyService() {
        return policyService;
    }

    @Override
    public AppWorldStore getWorldStore() {
        return worldStore;
    }

    @Override
    public void addPlugin(AppPlugin plugin) {
        if (plugin != null) {
            plugins.add(plugin);
        }
    }

    @Override
    public void removePlugin(AppPlugin plugin) {
        plugins.remove(plugin);
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

    private void persistWorldStoreState(String branchName, WorldId worldId, Snapshot snapshot) {
        if (worldStore == null || branchName == null || worldId == null || snapshot == null) {
            return;
        }
        worldStore.save(branchName, worldId, snapshot);
    }

    private ComputeStatus toComputeStatus(ProposalStatus status) {
        if (status == ProposalStatus.EVALUATING || status == ProposalStatus.EXECUTING) {
            return ComputeStatus.PENDING;
        }
        if (status == ProposalStatus.REJECTED || status == ProposalStatus.FAILED) {
            return ComputeStatus.ERROR;
        }
        if (status == ProposalStatus.COMPLETED || status == ProposalStatus.APPROVED) {
            return ComputeStatus.COMPLETE;
        }
        return ComputeStatus.PENDING;
    }

    private ActionResult toActionResult(
        ComputeStatus computeStatus,
        RuntimeKind runtimeKind,
        String worldId,
        String error
    ) {
        if (computeStatus == ComputeStatus.COMPLETE || computeStatus == ComputeStatus.HALTED) {
            return new CompletedActionResult(worldId, runtimeKind);
        }
        if (computeStatus == ComputeStatus.ERROR) {
            return new FailedActionResult(error == null ? "failed" : error, worldId, runtimeKind);
        }
        return new PreparationFailedActionResult("pending", runtimeKind);
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

    private void appendWorldTerminalUpdate(Intent intent, ActionHandle handle, ProposalStatus status) {
        if (status == ProposalStatus.COMPLETED || status == ProposalStatus.APPROVED) {
            appendUpdate(intent, handle, ActionPhase.COMPLETED, "World proposal completed");
            return;
        }
        if (status == ProposalStatus.REJECTED) {
            appendUpdate(intent, handle, ActionPhase.REJECTED, "World proposal rejected");
            return;
        }
        if (status == ProposalStatus.FAILED) {
            appendUpdate(intent, handle, ActionPhase.FAILED, "World proposal failed");
            return;
        }
        appendUpdate(intent, handle, ActionPhase.EXECUTING, "World proposal pending: " + status);
    }

    private void appendTerminalUpdate(Intent intent, ActionHandle handle, ComputeStatus status, String message) {
        if (status == ComputeStatus.COMPLETE || status == ComputeStatus.HALTED) {
            appendUpdate(intent, handle, ActionPhase.COMPLETED, message == null ? "Action completed" : message);
            return;
        }
        if (status == ComputeStatus.ERROR) {
            appendUpdate(intent, handle, ActionPhase.FAILED, message == null ? "Action failed" : message);
            return;
        }
        appendUpdate(intent, handle, ActionPhase.EXECUTING, message == null ? "Action is still running" : message);
    }

    private void appendUpdate(Intent intent, ActionHandle handle, ActionPhase phase, String message) {
        ActionUpdate update = new ActionUpdate(phase, message, System.currentTimeMillis());
        handle.recordUpdate(update);
        emitActionUpdateHook(intent, update);
    }

    private List<AppHook> sortedHooks() {
        List<AppHook> ordered = new ArrayList<>(hooks);
        ordered.sort(Comparator.comparingInt(AppHook::priority).reversed());
        return ordered;
    }

    private List<AppPlugin> sortedPlugins() {
        return List.copyOf(plugins);
    }

    private void runHook(AppHookEventType type, Runnable invoker, AppHook hook) {
        if (!hook.supports(type)) {
            return;
        }
        try {
            invoker.run();
        } catch (RuntimeException e) {
            if (hook.errorMode() == AppHookErrorMode.FAIL_FAST) {
                throw e;
            }
        }
    }

    private void emitReadyHook() {
        for (AppHook hook : sortedHooks()) {
            runHook(AppHookEventType.READY, () -> hook.onReady(snapshot), hook);
        }
    }

    private void emitBeforeActHook(Intent intent) {
        for (AppHook hook : sortedHooks()) {
            runHook(AppHookEventType.BEFORE_ACT, () -> hook.onBeforeAct(intent, snapshot), hook);
        }
    }

    private void emitActionUpdateHook(Intent intent, ActionUpdate update) {
        for (AppHook hook : sortedHooks()) {
            runHook(AppHookEventType.ACTION_UPDATE, () -> hook.onActionUpdate(intent, update, snapshot), hook);
        }
    }

    private void emitAfterActHook(Intent intent, ActionHandle handle) {
        for (AppHook hook : sortedHooks()) {
            runHook(AppHookEventType.AFTER_ACT, () -> hook.onAfterAct(intent, handle, snapshot), hook);
        }
    }

    private void emitBranchSwitchedHook(WorldId worldId) {
        for (AppHook hook : sortedHooks()) {
            runHook(AppHookEventType.BRANCH_SWITCHED, () -> hook.onBranchSwitched(worldId, snapshot), hook);
        }
    }

    private void ensureReady() {
        if (status == AppStatus.CREATED) {
            throw new IllegalStateException("App is not ready. Call ready() before act().");
        }
    }

    private void ensureNotDisposed() {
        if (status == AppStatus.DISPOSING || status == AppStatus.DISPOSED) {
            throw new IllegalStateException("App is disposed");
        }
    }

    private String worldIdValue(WorldId worldId) {
        return worldId == null ? null : worldId.value();
    }

    private record Subscription(Function<Snapshot, Object> selector, Consumer<Object> handler) {}
}
