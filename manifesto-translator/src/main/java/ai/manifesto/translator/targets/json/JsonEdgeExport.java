package ai.manifesto.translator.targets.json;

/**
 * KR: JSON target의 엣지 출력 모델입니다.
 * EN: Edge output model for JSON target.
 */
public record JsonEdgeExport(
    String from,
    String to
) {}
