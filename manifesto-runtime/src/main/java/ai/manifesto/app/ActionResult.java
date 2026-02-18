package ai.manifesto.app;

/**
 * KR: App 액션 실행 결과의 공통 계약입니다.
 * EN: Common contract for App action execution result.
 */
public interface ActionResult {
    String status();

    RuntimeKind runtimeKind();
}
