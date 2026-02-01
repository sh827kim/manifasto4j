package ai.manifesto.compiler;

import ai.manifesto.core.schema.DomainSchema;

/**
 * CompilerFacade - MEL 컴파일 진입점
 */
public interface CompilerFacade {
    CompilationResult compileDomain(String melText);

    default CompilationResult compileDomain(String melText, CompileDomainOptions options) {
        return compileDomain(melText);
    }

    default DomainSchema compileDomainOrThrow(String melText) {
        CompilationResult result = compileDomain(melText);
        if (!result.isOk()) {
            throw new IllegalArgumentException("Compilation failed: " + result.getError());
        }
        return result.getSchema();
    }
}
