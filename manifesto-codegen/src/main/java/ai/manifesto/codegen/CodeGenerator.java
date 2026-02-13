package ai.manifesto.codegen;

import java.util.List;

/**
 * KR: 스키마를 특정 타깃 코드로 변환하는 코드 생성기 계약입니다.
 * EN: Code generator contract that transforms schema input into target-specific source artifacts.
 */
public interface CodeGenerator {
    List<GeneratedArtifact> generate(CodegenRequest request);
}
