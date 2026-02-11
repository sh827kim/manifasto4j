package ai.manifesto.compiler;

import ai.manifesto.core.schema.FieldSpec;

import java.util.Map;

/**
 * KR: IrGeneratorLite는 컴파일러 모듈에서 ir generator lite 역할을 수행하는 구현 타입입니다.
 * EN: IrGeneratorLite is an implementation type performing ir generator lite roles in the compiler module.
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
