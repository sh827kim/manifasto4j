package ai.manifesto.world.schema;

import java.util.Objects;

public final class ArtifactRef {
    private final String uri;
    private final String hash;

    public ArtifactRef(String uri, String hash) {
        this.uri = Objects.requireNonNull(uri, "uri is required");
        this.hash = Objects.requireNonNull(hash, "hash is required");
    }

    public String getUri() {
        return uri;
    }

    public String getHash() {
        return hash;
    }
}
