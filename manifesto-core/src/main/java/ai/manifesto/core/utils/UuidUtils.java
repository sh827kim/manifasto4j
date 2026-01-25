package ai.manifesto.core.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * UuidUtils - 결정론적 UUID 생성
 *
 * Manifesto는 모든 동작이 결정론적이어야 한다.
 * 따라서 UUID도 난수가 아닌 입력 값의 해시로 생성한다.
 *
 * 같은 intentId + counter -> 같은 UUID
 */
public class UuidUtils {

    /**
     * 결정론적 UUID 생성
     * intentId와 counter 기반으로 해시 생성
     *
     * @param intentId Intent 식별자
     * @param counter 카운터 (0부터 증가)
     * @return 결정론적 UUID 문자열
     */
    public static String generateDeterministic(String intentId, int counter) {
        String seed = intentId + "-" + counter;
        return generateFromSeed(seed);
    }

    /**
     * seed 문자열로부터 UUID 생성
     * SHA-256 해시를 사용하여 결정론적 UUID 생성
     */
    private static String generateFromSeed(String seed) {
        try {
            // SHA-256 해시 생성
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes());

            // 처음 16바이트를 UUID 형식으로 변환
            long mostSignificant = 0;
            long leastSignificant = 0;

            for (int i = 0; i < 8; i++) {
                mostSignificant = (mostSignificant << 8) | (hash[i] & 0xFF);
            }

            for (int i = 8; i < 16; i++) {
                leastSignificant = (leastSignificant << 8) | (hash[i] & 0xFF);
            }

            // UUID 버전 4 (random) 형식 에뮬레이션
            // version bits를 설정
            mostSignificant = (mostSignificant & 0xFFFFFFFFFFFF0FFFL) | 0x4000;
            leastSignificant = (leastSignificant & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

            return new UUID(mostSignificant, leastSignificant).toString();
        } catch (NoSuchAlgorithmException e) {
            // 폴백: 간단한 해시 기반 UUID
            return generateSimpleDeterministic(seed);
        }
    }

    /**
     * 간단한 결정론적 UUID (SHA-256 실패 시 폴백)
     */
    private static String generateSimpleDeterministic(String seed) {
        int hash = seed.hashCode();
        long mostSignificant = ((long) hash << 32) | (hash & 0xFFFFFFFFL);
        long leastSignificant = ((long) (seed.length()) << 32) | (seed.length() & 0xFFFFFFFFL);

        mostSignificant = (mostSignificant & 0xFFFFFFFFFFFF0FFFL) | 0x4000;
        leastSignificant = (leastSignificant & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

        return new UUID(mostSignificant, leastSignificant).toString();
    }

    /**
     * 테스트용: 기존 UUID와 동일한 형식인지 확인
     */
    public static boolean isValidUuid(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
