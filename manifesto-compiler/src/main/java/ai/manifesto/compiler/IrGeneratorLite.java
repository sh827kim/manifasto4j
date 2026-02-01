package ai.manifesto.compiler;

import ai.manifesto.core.schema.FieldSpec;

import java.util.Map;

/**
 * IrGeneratorLite - 최소 IR 타입 변환기
 *
 * 현재는 TypeExpr -> FieldSpec 변환에 집중한다.
 */
public final class IrGeneratorLite {
    private final TypeExprParser typeExprParser = new TypeExprParser();

    public FieldSpec generateFieldSpec(String name, String typeExpr) {
        return typeExprParser.parseFieldSpec(name, typeExpr, true, null);
    }

    public Map<String, FieldSpec> generateInputFields(String inputSpec) {
        return typeExprParser.parseInputFields(inputSpec);
    }
}
