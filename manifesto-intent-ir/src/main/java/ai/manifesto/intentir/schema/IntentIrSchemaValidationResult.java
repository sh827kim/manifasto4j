package ai.manifesto.intentir.schema;

import java.util.List;

/**
 * KR: Intent-IR schema validation 결과입니다.
 * EN: Validation result for Intent-IR schema shape.
 */
public record IntentIrSchemaValidationResult(
    boolean valid,
    List<String> diagnostics
) {}
