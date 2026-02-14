package ai.manifesto.app;

import java.util.Map;

/**
 * KR: ActOptions는 액션 실행 시 timeout/branch/metadata를 지정하는 옵션 계약입니다.
 * EN: ActOptions provides timeout, branch, and metadata options for action execution.
 */
public record ActOptions(
    Long timeoutMillis,
    String branchName,
    Map<String, Object> metadata
) {
}
