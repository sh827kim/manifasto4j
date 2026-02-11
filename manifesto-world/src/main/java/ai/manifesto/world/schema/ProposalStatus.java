package ai.manifesto.world.schema;

import java.util.EnumSet;
import java.util.Set;

/**
 * KR: ProposalStatus는 World 도메인 상태/분류 체계를 정의하는 열거형입니다.
 * EN: ProposalStatus is an enum defining World-domain status or classification categories.
 */
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
