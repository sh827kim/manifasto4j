package ai.manifesto.core.utils;

import ai.manifesto.core.core.ValidationUtils;

/**
 * CanonicalUtils - canonical JSON utilities
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
