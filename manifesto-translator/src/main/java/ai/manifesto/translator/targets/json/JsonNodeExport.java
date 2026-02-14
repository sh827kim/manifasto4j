package ai.manifesto.translator.targets.json;

import java.util.List;

/**
 * KR: JSON target의 노드 출력 모델입니다.
 * EN: Node output model for JSON target.
 */
public record JsonNodeExport(
    String id,
    String action,
    String resolution,
    List<String> dependencies
) {}
