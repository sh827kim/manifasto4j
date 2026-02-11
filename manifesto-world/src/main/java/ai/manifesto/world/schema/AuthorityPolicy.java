package ai.manifesto.world.schema;

/**
 * KR: AuthorityPolicy는 권한/거버넌스 정책 구성을 표현하는 값 객체입니다.
 * EN: AuthorityPolicy is a value object describing authority/governance policy configuration.
 */
public interface AuthorityPolicy {
    AuthorityPolicyMode getMode();
}
