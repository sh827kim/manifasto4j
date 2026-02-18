package ai.manifesto.app;

import ai.manifesto.core.ComputeResult;
import ai.manifesto.core.ComputeStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * KR: ActionHandle는 비동기 액션 실행의 완료 결과를 조회/구독하기 위한 핸들 객체입니다.
 * EN: ActionHandle is a handle object used to observe or await asynchronous action execution results.
 */
public final class ActionHandle {
    private final List<ActionUpdate> updates = new CopyOnWriteArrayList<>();
    private final List<Consumer<ActionUpdate>> updateObservers = new CopyOnWriteArrayList<>();
    private final CompletableFuture<ComputeResult> completion = new CompletableFuture<>();
    private volatile ComputeResult result;
    private volatile ActionResult actionResult;
    private volatile RuntimeKind runtimeKind = RuntimeKind.DOMAIN;
    private volatile String cancelReason;

    public ActionHandle(ComputeResult result) {
        this(result, List.of());
    }

    public ActionHandle(ComputeResult result, List<ActionUpdate> updates) {
        this.result = Objects.requireNonNull(result, "result is required");
        if (updates != null) {
            this.updates.addAll(updates);
        }
        this.actionResult = inferActionResult(result, this.runtimeKind, null);
        this.completion.complete(result);
    }

    public static ActionHandle start(RuntimeKind runtimeKind) {
        ActionHandle handle = new ActionHandle();
        handle.runtimeKind = runtimeKind == null ? RuntimeKind.DOMAIN : runtimeKind;
        return handle;
    }

    private ActionHandle() {
    }

    public ComputeStatus getStatus() {
        ComputeResult current = result;
        return current == null ? ComputeStatus.PENDING : current.getStatus();
    }

    public ComputeResult getResult() {
        ComputeResult current = result;
        if (current == null) {
            throw new IllegalStateException("Action result is not available yet");
        }
        return current;
    }

    public ActionResult getActionResult() {
        return actionResult;
    }

    public RuntimeKind getRuntimeKind() {
        return runtimeKind;
    }

    public List<ActionUpdate> getUpdates() {
        return List.copyOf(updates);
    }

    public ActionPhase getPhase() {
        if (updates.isEmpty()) {
            return toPhase(getStatus());
        }
        return updates.get(updates.size() - 1).phase();
    }

    public Runnable subscribeUpdates(Consumer<ActionUpdate> observer) {
        Objects.requireNonNull(observer, "observer is required");
        updateObservers.add(observer);
        for (ActionUpdate update : updates) {
            observer.accept(update);
        }
        return () -> updateObservers.remove(observer);
    }

    public void recordUpdate(ActionUpdate update) {
        Objects.requireNonNull(update, "update is required");
        updates.add(update);
        for (Consumer<ActionUpdate> observer : updateObservers) {
            observer.accept(update);
        }
    }

    public void complete(ComputeResult result, ActionResult actionResult) {
        this.result = Objects.requireNonNull(result, "result is required");
        this.actionResult = actionResult == null ? inferActionResult(result, runtimeKind, null) : actionResult;
        completion.complete(result);
    }

    public ComputeResult await() {
        try {
            return completion.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for action completion", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed while waiting for action completion", e);
        }
    }

    public ComputeResult await(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout is required");
        try {
            return completion.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for action completion", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed while waiting for action completion", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Action completion timed out", e);
        }
    }

    public boolean cancel(String reason) {
        if (completion.isDone()) {
            return false;
        }
        this.cancelReason = reason;
        return completion.cancel(false);
    }

    public boolean isCancelled() {
        return completion.isCancelled();
    }

    public String getCancelReason() {
        return cancelReason;
    }

    private ActionResult inferActionResult(ComputeResult result, RuntimeKind runtimeKind, String worldId) {
        if (result == null || result.getStatus() == null) {
            return new FailedActionResult("unknown_status", worldId, runtimeKind);
        }
        return switch (result.getStatus()) {
            case COMPLETE, HALTED -> new CompletedActionResult(worldId, runtimeKind);
            case ERROR -> new FailedActionResult("compute_error", worldId, runtimeKind);
            default -> new PreparationFailedActionResult("incomplete_state", runtimeKind);
        };
    }

    private ActionPhase toPhase(ComputeStatus status) {
        if (status == null) {
            return ActionPhase.FAILED;
        }
        return switch (status) {
            case COMPLETE, HALTED -> ActionPhase.COMPLETED;
            case ERROR -> ActionPhase.FAILED;
            default -> ActionPhase.EXECUTING;
        };
    }
}
