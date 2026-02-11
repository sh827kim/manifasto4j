package ai.manifesto.core.utils;

import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.schema.DomainSchema;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * HashUtils - schema/hash helpers
 */
public final class HashUtils {
    private HashUtils() {}

    public static String sha256(String message) {
        return sha256Sync(message);
    }

    public static String sha256Sync(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(message.hashCode());
        }
    }

    public static String hashSchema(DomainSchema schema) {
        return ValidationUtils.computeSchemaHash(schema);
    }

    public static String hashSchemaEffective(DomainSchema schema) {
        return ValidationUtils.computeSchemaHashEffective(schema);
    }

    public static String generateRequirementId(String schemaHash, String intentId, String actionId, String flowNodePath) {
        String input = safe(schemaHash) + ":" + safe(intentId) + ":" + safe(actionId) + ":" + safe(flowNodePath);
        String hash = sha256Sync(input);
        return "req-" + hash.substring(0, 16);
    }

    public static String generateTraceId(int index) {
        return "trace-" + index;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
