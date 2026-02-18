package ai.manifesto.runtime;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.world.schema.WorldId;

import java.util.Objects;

/**
 * KR: AppRefImpl은 App 인스턴스를 AppRef 읽기 포트로 노출하는 어댑터입니다.
 * EN: AppRefImpl adapts an App instance to the AppRef read-only port.
 */
public final class AppRefImpl implements AppRef {
    private final App app;

    public AppRefImpl(App app) {
        this.app = Objects.requireNonNull(app, "app is required");
    }

    public static AppRef create(App app) {
        return new AppRefImpl(app);
    }

    @Override
    public AppStatus getStatus() {
        return app.getStatus();
    }

    @Override
    public Snapshot getSnapshot() {
        return app.getSnapshot();
    }

    @Override
    public DomainSchema getSchema() {
        return app.getSchema();
    }

    @Override
    public WorldId getCurrentBranchId() {
        return app.getCurrentBranchId();
    }

    @Override
    public String getCurrentBranchName() {
        return app.getCurrentBranchName();
    }
}
