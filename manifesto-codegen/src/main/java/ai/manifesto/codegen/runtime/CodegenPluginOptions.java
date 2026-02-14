package ai.manifesto.codegen.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: plugin 실행 옵션 계약입니다.
 * EN: Plugin execution option contract.
 */
public record CodegenPluginOptions(
    NamingConvention naming,
    NullabilityMode nullability,
    StyleProfile style
) {
    public static CodegenPluginOptions defaults() {
        return new CodegenPluginOptions(NamingConvention.CAMEL_CASE, NullabilityMode.STRICT, StyleProfile.STANDARD);
    }

    public List<CodegenDiagnostic> validate() {
        List<CodegenDiagnostic> diagnostics = new ArrayList<>();
        if (naming == null) {
            diagnostics.add(CodegenDiagnostic.error("runner", "Plugin option naming must not be null"));
        }
        if (nullability == null) {
            diagnostics.add(CodegenDiagnostic.error("runner", "Plugin option nullability must not be null"));
        }
        if (style == null) {
            diagnostics.add(CodegenDiagnostic.error("runner", "Plugin option style must not be null"));
        }
        return List.copyOf(diagnostics);
    }
}
