package ai.manifesto.compiler;

import ai.manifesto.core.schema.FieldSpec;

import java.util.Map;
import java.util.Objects;

/**
 * KR: IR 생성 유틸리티로, 타입 표현식을 FieldSpec 기반 런타임 스키마로 변환합니다.
 * EN: IR generation utility that converts type expressions into runtime FieldSpec schemas.
 */
public final class IrGenerator {
    private final TypeExprParser typeExprParser = new TypeExprParser();

    public FieldSpec generateFieldSpec(String name, String typeExpr) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(typeExpr, "typeExpr must not be null");
        return typeExprParser.parseFieldSpec(name, typeExpr, true, null);
    }

    public Map<String, FieldSpec> generateInputFields(String inputSpec) {
        Objects.requireNonNull(inputSpec, "inputSpec must not be null");
        return typeExprParser.parseInputFields(inputSpec);
    }
}
