package ai.manifesto.compiler;

import java.util.List;

/**
 * KR: CompilePatchOptions는 실행 동작을 제어하는 옵션 값을 묶는 설정 객체입니다.
 * EN: CompilePatchOptions is a configuration object bundling options that control runtime behavior.
 */
public record CompilePatchOptions(
    String actionName,
    List<String> allowSysPrefixes,
    String fnTableVersion
) {}
