package ai.manifesto.app;

/**
 * KR: schema hash 기준의 복구 호환성 검사 유틸리티입니다.
 * EN: Utility for recovery compatibility checks based on schema hash.
 */
public final class SchemaCompatibilityUtils {
    private SchemaCompatibilityUtils() {
    }

    public static SchemaCompatibilityResult validate(String currentSchemaHash, String candidateSchemaHash) {
        if (currentSchemaHash == null || currentSchemaHash.isBlank()) {
            return SchemaCompatibilityResult.incompatible(currentSchemaHash, candidateSchemaHash, "current_schema_hash_missing");
        }
        if (candidateSchemaHash == null || candidateSchemaHash.isBlank()) {
            return SchemaCompatibilityResult.incompatible(currentSchemaHash, candidateSchemaHash, "candidate_schema_hash_missing");
        }
        if (!currentSchemaHash.equals(candidateSchemaHash)) {
            return SchemaCompatibilityResult.incompatible(currentSchemaHash, candidateSchemaHash, "schema_hash_mismatch");
        }
        return SchemaCompatibilityResult.compatible(currentSchemaHash, candidateSchemaHash);
    }
}
