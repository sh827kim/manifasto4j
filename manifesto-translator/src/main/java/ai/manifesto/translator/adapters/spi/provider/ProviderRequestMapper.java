package ai.manifesto.translator.adapters.spi.provider;

import ai.manifesto.translator.adapters.spi.LlmRequest;

import java.util.Map;

/**
 * KR: 표준 LLM 요청을 provider payload로 변환하는 계약입니다.
 * EN: Contract that maps standard LLM request to provider-specific payload.
 */
public interface ProviderRequestMapper {
    Map<String, Object> toProviderPayload(LlmRequest request, ProviderCapabilityProfile profile);
}
