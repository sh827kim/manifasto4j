package ai.manifesto.app;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;

/**
 * KR: App 액션 실행 허용 여부를 결정하는 정책 서비스 경계입니다.
 * EN: Policy service boundary deciding whether an App action is allowed.
 */
public interface AppPolicyService {
    PolicyDecision decide(Intent intent, Snapshot snapshot);

    record PolicyDecision(boolean allowed, String reason) {
        public static PolicyDecision allow() {
            return new PolicyDecision(true, null);
        }

        public static PolicyDecision reject(String reason) {
            return new PolicyDecision(false, reason == null ? "rejected_by_policy" : reason);
        }
    }
}
