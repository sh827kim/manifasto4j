package ai.manifesto.runtime;

import ai.manifesto.world.schema.WorldId;

/**
 * KR: ForkOptions는 브랜치 포크 시 이름/기준 월드를 지정하는 옵션 계약입니다.
 * EN: ForkOptions defines branch name and base world when forking a branch.
 */
public record ForkOptions(
    String branchName,
    WorldId baseWorldId
) {
}
