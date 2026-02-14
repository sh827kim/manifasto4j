package ai.manifesto.translator.targets.openapi;

import java.util.Map;

/**
 * KR: OpenAPI target exporter 출력 스펙 모델입니다.
 * EN: Output specification model for OpenAPI target exporter.
 */
public record OpenApiSpec(
    String openapi,
    Info info,
    Map<String, PathItem> paths
) {
    public record Info(String title, String version) {}

    public record PathItem(Operation post) {}

    public record Operation(String operationId, String summary) {}
}
