package ai.manifesto.sdk;

import ai.manifesto.world.schema.WorldId;

/**
 * KR: 브랜치 head 조회 결과입니다.
 * EN: Branch head query result.
 */
public record AppHead(
    String branchName,
    WorldId worldId,
    long createdAt
) {
}
