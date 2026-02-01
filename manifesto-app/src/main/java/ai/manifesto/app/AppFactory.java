package ai.manifesto.app;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.HostRuntime;

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
}
