package ai.manifesto.translator.adapters.spi.provider;

import ai.manifesto.translator.adapters.spi.LlmRequest;
import ai.manifesto.translator.adapters.spi.LlmResponse;

import java.util.Map;
import java.util.Objects;

/**
 * KR: provider 프로파일과 mapper/normalizer를 묶는 불변 바인딩입니다.
 * EN: Immutable binding that groups provider profile with mapper and normalizer.
 */
public final class ProviderAdapterBinding {
    private final ProviderCapabilityProfile profile;
    private final ProviderRequestMapper requestMapper;
    private final ProviderResponseNormalizer responseNormalizer;

    public ProviderAdapterBinding(
        ProviderCapabilityProfile profile,
        ProviderRequestMapper requestMapper,
        ProviderResponseNormalizer responseNormalizer
    ) {
        this.profile = Objects.requireNonNull(profile, "profile must not be null");
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        this.responseNormalizer = Objects.requireNonNull(responseNormalizer, "responseNormalizer must not be null");
    }

    public ProviderCapabilityProfile profile() {
        return profile;
    }

    public Map<String, Object> toProviderPayload(LlmRequest request) {
        return requestMapper.toProviderPayload(request, profile);
    }

    public LlmResponse normalizeResponse(Map<String, Object> providerResponse) {
        return responseNormalizer.toLlmResponse(providerResponse, profile);
    }
}
