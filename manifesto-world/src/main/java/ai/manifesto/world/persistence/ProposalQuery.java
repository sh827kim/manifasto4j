package ai.manifesto.world.persistence;

import ai.manifesto.world.schema.ProposalStatus;

import java.util.Set;

/**
 * KR: proposal 조회의 filter/sort/limit 계약입니다.
 * EN: Filter/sort/limit contract for proposal queries.
 */
public record ProposalQuery(
    Set<ProposalStatus> statuses,
    String actorId,
    String baseWorldId,
    Long submittedAfterInclusive,
    Long submittedBeforeInclusive,
    boolean sortSubmittedAtDesc,
    int offset,
    int limit
) {
    public static ProposalQuery defaults() {
        return new ProposalQuery(Set.of(), null, null, null, null, true, 0, 0);
    }

    public int safeOffset() {
        return Math.max(0, offset);
    }

    public int safeLimit() {
        return Math.max(0, limit);
    }
}
