package ai.manifesto.world;

import ai.manifesto.world.schema.DecisionRecord;
import ai.manifesto.world.schema.Proposal;
import ai.manifesto.world.schema.World;

/**
 * KR: ProposalResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: ProposalResult is a result type carrying operation or execution outcomes.
 */
public final class ProposalResult {
    private final Proposal proposal;
    private final DecisionRecord decision;
    private final World resultWorld;
    private final String error;

    public ProposalResult(Proposal proposal, DecisionRecord decision, World resultWorld, String error) {
        this.proposal = proposal;
        this.decision = decision;
        this.resultWorld = resultWorld;
        this.error = error;
    }

    public static ProposalResult of(Proposal proposal, DecisionRecord decision, World resultWorld) {
        return new ProposalResult(proposal, decision, resultWorld, null);
    }

    public static ProposalResult withError(Proposal proposal, String error) {
        return new ProposalResult(proposal, null, null, error);
    }

    public Proposal getProposal() {
        return proposal;
    }

    public DecisionRecord getDecision() {
        return decision;
    }

    public World getResultWorld() {
        return resultWorld;
    }

    public String getError() {
        return error;
    }
}
