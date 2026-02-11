package ai.manifesto.core.utils;

import ai.manifesto.core.core.ValidationUtils;

/**
 * KR: CanonicalUtils는 재사용 가능한 정적 보조 함수를 제공하는 유틸리티 타입입니다.
 * EN: CanonicalUtils is a utility type providing reusable static helper functions.
 */
public final class CanonicalUtils {
    private CanonicalUtils() {}

    public static String toCanonical(Object value) {
        return ValidationUtils.toCanonicalJson(value);
    }

    public static boolean canonicalEqual(Object a, Object b) {
        return toCanonical(a).equals(toCanonical(b));
    }
}
