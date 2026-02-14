package ai.manifesto.translator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * KR: 도메인 정책을 메모리에 보관하는 기본 provider 구현입니다.
 * EN: Default in-memory provider implementation for domain policies.
 */
public final class InMemoryTranslatorPolicyProvider implements TranslatorPolicyProvider {
    private final Map<String, TranslatorDomainPolicy> policiesByDomain;

    public InMemoryTranslatorPolicyProvider(Map<String, TranslatorDomainPolicy> policiesByDomain) {
        Objects.requireNonNull(policiesByDomain, "policiesByDomain must not be null");
        this.policiesByDomain = new LinkedHashMap<>();
        for (Map.Entry<String, TranslatorDomainPolicy> entry : policiesByDomain.entrySet()) {
            this.policiesByDomain.put(normalize(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public Optional<TranslatorDomainPolicy> findByDomain(String domainName) {
        if (domainName == null || domainName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(policiesByDomain.get(normalize(domainName)));
    }

    @Override
    public Map<String, TranslatorDomainPolicy> snapshot() {
        return Map.copyOf(policiesByDomain);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
