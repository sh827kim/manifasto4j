package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: AuthorityRef는 World 도메인 객체를 참조하기 위한 참조 타입입니다.
 * EN: AuthorityRef is a reference type used to point to a World-domain object.
 */
public final class AuthorityRef {
    private final String authorityId;
    private final AuthorityKind kind;

    public AuthorityRef(String authorityId, AuthorityKind kind) {
        this.authorityId = Objects.requireNonNull(authorityId, "authorityId is required");
        this.kind = Objects.requireNonNull(kind, "kind is required");
    }

    public String getAuthorityId() {
        return authorityId;
    }

    public AuthorityKind getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorityRef that)) return false;
        return Objects.equals(authorityId, that.authorityId) && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorityId, kind);
    }
}
