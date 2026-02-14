package ai.manifesto.translator;

import java.util.Optional;
import java.util.Map;

/**
 * KR: domainName 기준 translator 정책을 조회하는 계약입니다.
 * EN: Contract for resolving translator policy by domainName.
 */
public interface TranslatorPolicyProvider {
    Optional<TranslatorDomainPolicy> findByDomain(String domainName);

    default Map<String, TranslatorDomainPolicy> snapshot() {
        return Map.of();
    }

    default void reload() {
    }
}
