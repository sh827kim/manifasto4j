package ai.manifesto.sdk;

/**
 * KR: SDK 액션 결과 공통 계약입니다.
 * EN: Common contract for SDK action result.
 */
public interface ActionResult {
    String status();

    RuntimeKind runtimeKind();
}
