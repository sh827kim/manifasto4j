package ai.manifesto.app;

/**
 * KR: 현재 schema와 복구 대상 schema의 호환성 검증 결과입니다.
 * EN: Compatibility check result between current schema and recovery candidate schema.
 */
public record SchemaCompatibilityResult(
    boolean compatible,
    String currentSchemaHash,
    String candidateSchemaHash,
    String reason
) {
    public static SchemaCompatibilityResult compatible(String currentSchemaHash, String candidateSchemaHash) {
        return new SchemaCompatibilityResult(true, currentSchemaHash, candidateSchemaHash, null);
    }

    public static SchemaCompatibilityResult incompatible(String currentSchemaHash, String candidateSchemaHash, String reason) {
        return new SchemaCompatibilityResult(false, currentSchemaHash, candidateSchemaHash, reason);
    }
}
