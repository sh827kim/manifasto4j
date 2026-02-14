package ai.manifesto.translator.adapters.spi.provider;

/**
 * KR: LLM provider 기능 프로파일입니다.
 * EN: Capability profile for an LLM provider family.
 */
public record ProviderCapabilityProfile(
    String providerId,
    String defaultModel,
    boolean supportsSystemPrompt,
    boolean supportsJsonResponseFormat,
    boolean supportsStopSequences,
    boolean supportsUsageStats
) {}
