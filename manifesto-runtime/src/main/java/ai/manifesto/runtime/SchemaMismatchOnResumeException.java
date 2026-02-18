package ai.manifesto.runtime;

/**
 * KR: 세션/브랜치 복구 시 schema hash가 일치하지 않을 때 발생하는 예외입니다.
 * EN: Exception raised when schema hash mismatches during session/branch resume.
 */
public final class SchemaMismatchOnResumeException extends ManifestoAppException {
    private final String currentSchemaHash;
    private final String candidateSchemaHash;

    public SchemaMismatchOnResumeException(String currentSchemaHash, String candidateSchemaHash) {
        super("APP-SCHEMA-MISMATCH-ON-RESUME",
            "Schema mismatch on resume. current=" + currentSchemaHash + ", candidate=" + candidateSchemaHash);
        this.currentSchemaHash = currentSchemaHash;
        this.candidateSchemaHash = candidateSchemaHash;
    }

    public String getCurrentSchemaHash() {
        return currentSchemaHash;
    }

    public String getCandidateSchemaHash() {
        return candidateSchemaHash;
    }
}
