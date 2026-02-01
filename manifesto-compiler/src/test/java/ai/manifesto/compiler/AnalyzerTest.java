package ai.manifesto.compiler;

import ai.manifesto.compiler.analyzer.ScopeAnalyzer;
import ai.manifesto.compiler.analyzer.ScopeAnalysisResult;
import ai.manifesto.compiler.analyzer.SemanticValidator;
import ai.manifesto.compiler.analyzer.ValidationResult;
import ai.manifesto.compiler.diagnostics.DiagnosticSeverity;
import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.parser.ProgramNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Analyzer 테스트")
class AnalyzerTest {

    private ProgramNode parseProgram(String source) {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize().tokens());
        ParseResult result = parser.parse();
        return result.program();
    }

    @Test
    @DisplayName("미정의 식별자 감지")
    void detectsUndefinedIdentifiers() {
        ProgramNode program = parseProgram("""
            domain Test {
              state { x: number = 0 }
              computed y = add(x, undefined_var)
            }
            """);
        ScopeAnalyzer analyzer = new ScopeAnalyzer();
        ScopeAnalysisResult result = analyzer.analyze(program);
        assertTrue(result.diagnostics().stream().anyMatch(d -> "E_UNDEFINED".equals(d.code().code())));
    }

    @Test
    @DisplayName("정상 스코프 해석")
    void resolvesIdentifiers() {
        ProgramNode program = parseProgram("""
            domain Test {
              state { x: number = 0 }
              computed doubled = mul(x, 2)
              action add(amount: number) {
                when true { patch x = add(x, amount) }
              }
            }
            """);
        ScopeAnalyzer analyzer = new ScopeAnalyzer();
        ScopeAnalysisResult result = analyzer.analyze(program);
        assertFalse(result.diagnostics().stream().anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR));
    }

    @Test
    @DisplayName("$system.* computed 금지 (E001)")
    void detectsSystemInComputed() {
        ProgramNode program = parseProgram("""
            domain Test {
              computed id = $system.uuid
            }
            """);
        SemanticValidator validator = new SemanticValidator();
        ValidationResult result = validator.validate(program);
        assertTrue(result.diagnostics().stream().anyMatch(d -> "E001".equals(d.code().code())));
    }

    @Test
    @DisplayName("$system.* action 허용")
    void allowsSystemInActions() {
        ProgramNode program = parseProgram("""
            domain Test {
              state { id: string | null = null }
              action generateId() {
                when true { patch id = $system.uuid }
              }
            }
            """);
        SemanticValidator validator = new SemanticValidator();
        ValidationResult result = validator.validate(program);
        assertFalse(result.diagnostics().stream().anyMatch(d -> "E001".equals(d.code().code())));
    }

    @Test
    @DisplayName("available에서 $system/$input 금지 (E005)")
    void detectsSystemInAvailable() {
        ProgramNode program = parseProgram("""
            domain Test {
              action noop() available when eq($system.uuid, $input.id) {
                when true { patch x = 1 }
              }
            }
            """);
        SemanticValidator validator = new SemanticValidator();
        ValidationResult result = validator.validate(program);
        assertTrue(result.diagnostics().stream().anyMatch(d -> "E005".equals(d.code().code())));
    }
}
