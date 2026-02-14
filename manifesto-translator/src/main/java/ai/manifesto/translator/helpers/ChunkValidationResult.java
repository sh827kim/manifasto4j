package ai.manifesto.translator.helpers;

import java.util.List;

/**
 * KR: chunk validation 결과입니다.
 * EN: Validation result for chunks.
 */
public record ChunkValidationResult(
    boolean valid,
    List<String> diagnostics
) {}
