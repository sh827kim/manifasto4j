package ai.manifesto.translator.adapters.profile;

import ai.manifesto.translator.adapters.spi.provider.ProviderCapabilityProfile;

/**
 * KR: 기본 provider capability profile 팩토리입니다.
 * EN: Factory for default provider capability profiles.
 */
public final class ProviderProfiles {
    private ProviderProfiles() {
    }

    public static ProviderCapabilityProfile openAi() {
        return new ProviderCapabilityProfile(
            "openai",
            "gpt-4o-mini",
            true,
            true,
            true,
            true
        );
    }

    public static ProviderCapabilityProfile ollama() {
        return new ProviderCapabilityProfile(
            "ollama",
            "llama3.1",
            true,
            false,
            false,
            false
        );
    }

    public static ProviderCapabilityProfile claude() {
        return new ProviderCapabilityProfile(
            "claude",
            "claude-3-5-sonnet-latest",
            true,
            false,
            false,
            true
        );
    }
}
