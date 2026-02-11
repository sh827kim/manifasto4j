package ai.manifesto.compiler;

import ai.manifesto.core.schema.DomainSchema;

/**
 * KR: CompilerFacade는 컴파일러 모듈에서 compiler facade 계약을 정의하는 인터페이스입니다.
 * EN: CompilerFacade is an interface defining the compiler facade contract in the compiler module.
 */
public interface CompilerFacade {
    CompilationResult compileDomain(String melText);

    default CompilationResult compileDomain(String melText, CompileDomainOptions options) {
        return compileDomain(melText);
    }

    default DomainSchema compileDomainOrThrow(String melText, CompileDomainOptions options) {
        CompilationResult result = compileDomain(melText, options);
        if (!result.isOk()) {
            throw new IllegalArgumentException("Compilation failed: " + result.getError());
        }
        return result.getSchema();
    }

    default DomainSchema compileDomainOrThrow(String melText) {
        CompilationResult result = compileDomain(melText);
        if (!result.isOk()) {
            throw new IllegalArgumentException("Compilation failed: " + result.getError());
        }
        return result.getSchema();
    }
}
