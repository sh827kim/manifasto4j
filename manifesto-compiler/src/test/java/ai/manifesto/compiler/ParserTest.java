package ai.manifesto.compiler;

import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ComputedNode;
import ai.manifesto.compiler.parser.DomainNode;
import ai.manifesto.compiler.parser.FunctionCallExprNode;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.parser.StateNode;
import ai.manifesto.compiler.parser.StateFieldNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Parser 테스트")
class ParserTest {

    @Test
    @DisplayName("도메인/표현식 파싱")
    void testParseDomain() {
        String source = """
            domain Test {
              state { count: number }
              computed total = add(1, 2)
            }
            """;

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize().tokens());
        ParseResult result = parser.parse();

        ProgramNode program = result.program();
        assertNotNull(program);
        DomainNode domain = program.domain();
        assertNotNull(domain);

        assertInstanceOf(StateNode.class, domain.members().get(0));
        StateNode state = (StateNode) domain.members().get(0);
        StateFieldNode field = state.fields().get(0);
        assertNotNull(field);

        assertInstanceOf(ComputedNode.class, domain.members().get(1));
        ComputedNode computed = (ComputedNode) domain.members().get(1);
        assertInstanceOf(FunctionCallExprNode.class, computed.expression());
    }
}
