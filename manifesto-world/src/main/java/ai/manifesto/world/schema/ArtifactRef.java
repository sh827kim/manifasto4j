package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: ArtifactRef는 World 도메인 객체를 참조하기 위한 참조 타입입니다.
 * EN: ArtifactRef is a reference type used to point to a World-domain object.
 */
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
