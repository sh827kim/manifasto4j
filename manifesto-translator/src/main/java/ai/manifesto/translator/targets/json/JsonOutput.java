package ai.manifesto.translator.targets.json;

import java.util.List;

/**
 * KR: JSON target exporter 출력 루트 모델입니다.
 * EN: Root output model for JSON target exporter.
 */
public record JsonOutput(
    List<JsonNodeExport> nodes,
    List<JsonEdgeExport> edges
) {}
