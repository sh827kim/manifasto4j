package ai.manifesto.world.schema;

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
