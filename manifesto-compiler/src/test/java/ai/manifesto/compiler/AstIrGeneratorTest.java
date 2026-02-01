package ai.manifesto.compiler;

import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("AST IR Generator 테스트")
class AstIrGeneratorTest {

    private ProgramNode parseProgram(String source) {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize().tokens());
        ParseResult result = parser.parse();
        return result.program();
    }

    @Test
    @DisplayName("nullable union은 required=false")
    void nullableUnionSetsRequiredFalse() {
        ProgramNode program = parseProgram("""
            domain Test {
              state { name: string | null = null }
            }
            """);
        AstIrGenerator generator = new AstIrGenerator();
        GenerateResult result = generator.generate(program);
        DomainSchema schema = result.schema();
        assertNotNull(schema);
        FieldSpec spec = schema.getDataField("name");
        assertNotNull(spec);
        assertFalse(spec.isRequired());
    }
}
