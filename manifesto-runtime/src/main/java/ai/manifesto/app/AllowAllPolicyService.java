package ai.manifesto.app;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;

/**
 * KR: 모든 액션을 허용하는 기본 정책 서비스입니다.
 * EN: Default policy service that allows every action.
 */
public final class AllowAllPolicyService implements AppPolicyService {
    @Override
    public PolicyDecision decide(Intent intent, Snapshot snapshot) {
        return PolicyDecision.allow();
    }
}
