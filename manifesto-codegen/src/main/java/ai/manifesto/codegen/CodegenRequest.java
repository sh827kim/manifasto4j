package ai.manifesto.codegen;

import java.util.Map;

/**
 * KR: DomainSchema 기반 코드 생성 요청 모델입니다.
 * EN: Code generation request model based on a DomainSchema-like payload.
 */
public record CodegenRequest(
    Map<String, Object> schema,
    String basePackage,
    CodegenTarget target
) {}
