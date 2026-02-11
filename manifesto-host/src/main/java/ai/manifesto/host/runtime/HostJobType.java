package ai.manifesto.host.runtime;

/**
 * KR: HostJobType는 Host 런타임 계층에서 사용하는 host job type 분류 값을 열거합니다.
 * EN: HostJobType enumerates host job type classification values used in the Host runtime layer.
 */
public enum HostJobType {
    START_INTENT,
    CONTINUE_COMPUTE,
    FULFILL_REQUIREMENTS
}
