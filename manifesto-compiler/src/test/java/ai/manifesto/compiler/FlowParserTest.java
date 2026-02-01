package ai.manifesto.compiler;

import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ActionNode;
import ai.manifesto.compiler.parser.DomainNode;
import ai.manifesto.compiler.parser.GuardedStmtNode;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.parser.PatchStmtNode;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.parser.WhenStmtNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Flow 파서 테스트")
class FlowParserTest {

    @Test
    @DisplayName("action/when/patch 파싱")
    void testFlowLine() {
        String source = """
            domain Test {
              state { count: number }
              action inc() {
                when count == 0 {
                  patch count = 1
                }
              }
            }
            """;

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize().tokens());
        ParseResult result = parser.parse();

        ProgramNode program = result.program();
        assertNotNull(program);
        DomainNode domain = program.domain();
        assertNotNull(domain);

        ActionNode action = (ActionNode) domain.members().get(1);
        GuardedStmtNode stmt = action.body().get(0);
        assertInstanceOf(WhenStmtNode.class, stmt);
        WhenStmtNode whenStmt = (WhenStmtNode) stmt;
        assertInstanceOf(PatchStmtNode.class, whenStmt.body().get(0));
    }
}
