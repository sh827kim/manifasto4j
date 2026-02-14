package ai.manifesto.translator.targets.manifesto;

import java.util.Map;

/**
 * KR: Manifesto exporter 확장 후보(MEL 등) 모델입니다.
 * EN: Extension candidate model (e.g., MEL) from Manifesto exporter.
 */
public record ManifestoExtensionCandidate(
    String nodeId,
    String kind,
    String reason,
    Map<String, Object> payload
) {}
