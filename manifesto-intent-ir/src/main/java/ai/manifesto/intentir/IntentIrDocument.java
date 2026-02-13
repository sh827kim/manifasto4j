package ai.manifesto.intentir;

import java.util.Map;

/**
 * KR: Translator 결과를 Host/Core 경계로 넘기기 위한 정규 Intent IR 문서입니다.
 * EN: Normalized Intent IR document used to pass translator output into the Host/Core boundary.
 */
public record IntentIrDocument(
    String schemaVersion,
    String domain,
    String action,
    Map<String, Object> input,
    Map<String, Object> meta
) {}
