package ai.manifesto.sdk;

import ai.manifesto.core.ComputeResult;
import ai.manifesto.core.ComputeStatus;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * KR: SDK 액션 실행 핸들입니다.
 * EN: SDK action execution handle.
 */
public final class ActionHandle {
    private final ai.manifesto.runtime.ActionHandle delegate;

    ActionHandle(ai.manifesto.runtime.ActionHandle delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    }

    public ComputeStatus getStatus() {
        return delegate.getStatus();
    }

    public ComputeResult getResult() {
        return delegate.getResult();
    }

    public ActionResult getActionResult() {
        return SdkMappings.toSdk(delegate.getActionResult());
    }

    public RuntimeKind getRuntimeKind() {
        return SdkMappings.toSdk(delegate.getRuntimeKind());
    }

    public List<ActionUpdate> getUpdates() {
        return delegate.getUpdates().stream()
            .map(SdkMappings::toSdk)
            .toList();
    }

    public ActionPhase getPhase() {
        return SdkMappings.toSdk(delegate.getPhase());
    }

    public Runnable subscribeUpdates(Consumer<ActionUpdate> observer) {
        Objects.requireNonNull(observer, "observer is required");
        return delegate.subscribeUpdates(update -> observer.accept(SdkMappings.toSdk(update)));
    }

    public ComputeResult await() {
        return delegate.await();
    }

    public ComputeResult await(Duration timeout) {
        return delegate.await(timeout);
    }

    public boolean cancel(String reason) {
        return delegate.cancel(reason);
    }

    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    public String getCancelReason() {
        return delegate.getCancelReason();
    }
}
