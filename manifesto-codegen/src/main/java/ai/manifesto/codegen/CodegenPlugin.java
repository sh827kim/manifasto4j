package ai.manifesto.codegen;

/**
 * KR: 특정 target을 처리할 수 있는 코드 생성 플러그인 계약입니다.
 * EN: Code generation plugin contract capable of handling specific targets.
 */
public interface CodegenPlugin extends CodeGenerator {
    String pluginId();

    boolean supports(CodegenTarget target);
}
