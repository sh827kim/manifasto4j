package ai.manifesto.world.schema;

/**
 * KR: AutoApprovePolicy는 권한/거버넌스 정책 구성을 표현하는 값 객체입니다.
 * EN: AutoApprovePolicy is a value object describing authority/governance policy configuration.
 */
public final class AutoApprovePolicy implements AuthorityPolicy {
    private final String reason;

    public AutoApprovePolicy() {
        this(null);
    }

    public AutoApprovePolicy(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public AuthorityPolicyMode getMode() {
        return AuthorityPolicyMode.AUTO_APPROVE;
    }
}
