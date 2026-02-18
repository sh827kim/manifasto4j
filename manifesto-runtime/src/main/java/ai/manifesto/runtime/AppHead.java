package ai.manifesto.runtime;

import ai.manifesto.world.schema.WorldId;

/**
 * KR: AppHead는 branch별 현재 head world 식별자와 정렬용 생성 시각을 나타내는 조회 타입입니다.
 * EN: AppHead is a query type representing the current head world identifier and creation time per branch.
 */
public record AppHead(
    String branchName,
    WorldId worldId,
    long createdAt
) {
}
