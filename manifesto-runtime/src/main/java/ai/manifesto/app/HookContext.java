package ai.manifesto.app;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;

/**
 * KR: HookContext는 hook 실행 시 전달되는 읽기 전용 컨텍스트입니다.
 * EN: HookContext is the read-only context delivered to hook invocations.
 */
public record HookContext(
    AppHookEventType eventType,
    Snapshot snapshot,
    Intent intent,
    ActionHandle actionHandle,
    ActionUpdate actionUpdate
) {
}
