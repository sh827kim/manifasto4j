package ai.manifesto.compiler;

import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.Parser;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * KR: MEL source를 token stream으로 노출하는 compiler 보조 API입니다.
     * EN: Compiler helper API exposing MEL source as token stream.
     */
    default Lexer.LexResult tokenize(String melText) {
        return new Lexer(melText).tokenize();
    }

    /**
     * KR: MEL source를 parser AST로 노출하는 compiler 보조 API입니다.
     * EN: Compiler helper API exposing MEL source as parsed AST.
     */
    default ParseResult parseSource(String melText) {
        Lexer.LexResult lexResult = tokenize(melText);
        if (lexResult.diagnostics().stream().anyMatch(d -> d.severity() == ai.manifesto.compiler.diagnostics.DiagnosticSeverity.ERROR)) {
            return new ParseResult(null, List.copyOf(lexResult.diagnostics()));
        }
        ParseResult parsed = new Parser(lexResult.tokens()).parse();
        List<ai.manifesto.compiler.diagnostics.Diagnostic> diagnostics = new ArrayList<>(lexResult.diagnostics());
        diagnostics.addAll(parsed.diagnostics());
        return new ParseResult(parsed.program(), List.copyOf(diagnostics));
    }
}
