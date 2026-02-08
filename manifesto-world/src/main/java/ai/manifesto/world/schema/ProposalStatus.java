package ai.manifesto.world.schema;

import java.util.EnumSet;
import java.util.Set;

public enum ProposalStatus {
    SUBMITTED,
    EVALUATING,
    APPROVED,
    REJECTED,
    EXECUTING,
    COMPLETED,
    FAILED;

    private static final Set<ProposalStatus> TERMINAL = EnumSet.of(COMPLETED, REJECTED, FAILED);
    private static final Set<ProposalStatus> INGRESS = EnumSet.of(SUBMITTED, EVALUATING);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }

    public boolean isIngress() {
        return INGRESS.contains(this);
    }
}
