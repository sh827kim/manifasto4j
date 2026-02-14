package ai.manifesto.translator;

import java.util.Set;

/**
 * KR: 도메인별 translator 검증 정책(허용 action/필수 context 키)을 정의합니다.
 * EN: Defines domain-level translator verification policy (allowed actions/required context keys).
 */
public record TranslatorDomainPolicy(
    String domainName,
    Set<String> allowedActions,
    Set<String> requiredContextKeys
) {}
