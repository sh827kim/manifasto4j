package ai.manifesto.compiler;

import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ActionNode;
import ai.manifesto.compiler.parser.ArrayLiteralExprNode;
import ai.manifesto.compiler.parser.ArrayTypeNode;
import ai.manifesto.compiler.parser.BinaryExprNode;
import ai.manifesto.compiler.parser.ComputedNode;
import ai.manifesto.compiler.parser.DomainMember;
import ai.manifesto.compiler.parser.DomainNode;
import ai.manifesto.compiler.parser.ExprNode;
import ai.manifesto.compiler.parser.FailStmtNode;
import ai.manifesto.compiler.parser.FunctionCallExprNode;
import ai.manifesto.compiler.parser.IdentifierExprNode;
import ai.manifesto.compiler.parser.IndexAccessExprNode;
import ai.manifesto.compiler.parser.LiteralExprNode;
import ai.manifesto.compiler.parser.ObjectLiteralExprNode;
import ai.manifesto.compiler.parser.OnceStmtNode;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.parser.PropertyAccessExprNode;
import ai.manifesto.compiler.parser.RecordTypeNode;
import ai.manifesto.compiler.parser.StateNode;
import ai.manifesto.compiler.parser.StopStmtNode;
import ai.manifesto.compiler.parser.SystemIdentExprNode;
import ai.manifesto.compiler.parser.UnaryExprNode;
import ai.manifesto.compiler.parser.UnionTypeNode;
import ai.manifesto.compiler.parser.WhenStmtNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Parser 전체 시나리오 테스트")
class ParserFullTest {

    private ParseResult parseSource(String source) {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize().tokens());
        return parser.parse();
    }

    private ExprNode parseExpr(String exprStr) {
        ParseResult result = parseSource("domain T { computed x = " + exprStr + " }");
        ProgramNode program = result.program();
        DomainMember member = program.domain().members().get(0);
        return ((ComputedNode) member).expression();
    }

    @Test
    @DisplayName("도메인 구조 파싱")
    void parsesDomainStructure() {
        ParseResult result = parseSource("domain Empty {}");
        assertTrue(result.diagnostics().isEmpty());
        ProgramNode program = result.program();
        assertEquals("Empty", program.domain().name());
        assertEquals(0, program.domain().members().size());
    }

    @Test
    @DisplayName("state/computed/action 파싱")
    void parsesDomainMembers() {
        ParseResult result = parseSource("""
            domain Counter {
              state { count: number = 0 }
              computed doubled = mul(count, 2)
              action increment() {
                when gt(count, 0) {
                  patch count = add(count, 1)
                }
              }
            }
            """);
        assertTrue(result.diagnostics().isEmpty());
        DomainNode domain = result.program().domain();
        assertEquals(3, domain.members().size());
        assertInstanceOf(StateNode.class, domain.members().get(0));
        assertInstanceOf(ComputedNode.class, domain.members().get(1));
        assertInstanceOf(ActionNode.class, domain.members().get(2));
    }

    @Test
    @DisplayName("타입 표현식 파싱")
    void parsesTypeExpressions() {
        ParseResult result = parseSource("""
            domain Test {
              state {
                name: string = ""
                status: "idle" | "loading" | "done" = "idle"
                items: Array<string> = []
                tasks: Record<string, Task> = {}
              }
            }
            """);
        assertTrue(result.diagnostics().isEmpty());
        StateNode state = (StateNode) result.program().domain().members().get(0);
        assertInstanceOf(LiteralExprNode.class, state.fields().get(0).initializer());
        assertInstanceOf(UnionTypeNode.class, state.fields().get(1).typeExpr());
        assertInstanceOf(ArrayTypeNode.class, state.fields().get(2).typeExpr());
        assertInstanceOf(RecordTypeNode.class, state.fields().get(3).typeExpr());
    }

    @Test
    @DisplayName("기본 표현식 파싱")
    void parsesExpressions() {
        assertInstanceOf(LiteralExprNode.class, parseExpr("42"));
        assertInstanceOf(IdentifierExprNode.class, parseExpr("foo"));
        assertInstanceOf(FunctionCallExprNode.class, parseExpr("add(1, 2)"));
        assertInstanceOf(UnaryExprNode.class, parseExpr("!active"));
        assertInstanceOf(BinaryExprNode.class, parseExpr("a + b"));
        assertInstanceOf(BinaryExprNode.class, parseExpr("a ?? b"));
        assertInstanceOf(PropertyAccessExprNode.class, parseExpr("user.name"));
        assertInstanceOf(IndexAccessExprNode.class, parseExpr("items[0]"));
        assertInstanceOf(ObjectLiteralExprNode.class, parseExpr("{ a: 1, b: 2 }"));
        assertInstanceOf(ArrayLiteralExprNode.class, parseExpr("[1, 2, 3]"));
    }

    @Test
    @DisplayName("연산자 우선순위")
    void respectsOperatorPrecedence() {
        ExprNode expr = parseExpr("a + b * c");
        BinaryExprNode add = (BinaryExprNode) expr;
        assertEquals("+", add.operator());
        assertInstanceOf(BinaryExprNode.class, add.right());
        BinaryExprNode mul = (BinaryExprNode) add.right();
        assertEquals("*", mul.operator());
    }

    @Test
    @DisplayName("시스템 식별자 및 $item")
    void parsesSystemIdentifiers() {
        ExprNode system = parseExpr("$system.uuid");
        assertInstanceOf(SystemIdentExprNode.class, system);
        assertEquals(List.of("system", "uuid"), ((SystemIdentExprNode) system).path());

        ExprNode meta = parseExpr("$meta.intentId");
        assertInstanceOf(SystemIdentExprNode.class, meta);
        assertEquals(List.of("meta", "intentId"), ((SystemIdentExprNode) meta).path());

        ExprNode item = parseExpr("$item.name");
        assertInstanceOf(PropertyAccessExprNode.class, item);
    }

    @Test
    @DisplayName("when/once/patch/effect 파싱")
    void parsesStatements() {
        ParseResult result = parseSource("""
            domain T {
              action test(x: number) {
                when eq(x, 0) { patch x = 1 }
                once(marker) when gt(x, 0) { patch marker = $meta.intentId }
              }
            }
            """);
        assertTrue(result.diagnostics().isEmpty());
        ActionNode action = (ActionNode) result.program().domain().members().get(0);
        assertInstanceOf(WhenStmtNode.class, action.body().get(0));
        assertInstanceOf(OnceStmtNode.class, action.body().get(1));
    }

    @Test
    @DisplayName("fail/stop 파싱")
    void parsesFailAndStop() {
        ParseResult result = parseSource("""
            domain Test {
              state { x: number = 0 }
              action test() {
                when eq(x, 0) {
                  fail "INVALID_STATE" with "x must not be zero"
                  stop "already_done"
                }
              }
            }
            """);
        assertTrue(result.diagnostics().isEmpty());
        ActionNode action = (ActionNode) result.program().domain().members().get(1);
        WhenStmtNode whenStmt = (WhenStmtNode) action.body().get(0);
        assertInstanceOf(FailStmtNode.class, whenStmt.body().get(0));
        assertInstanceOf(StopStmtNode.class, whenStmt.body().get(1));
    }

    @Test
    @DisplayName("available 조건 파싱")
    void parsesAvailableCondition() {
        ParseResult result = parseSource("""
            domain Counter {
              state { count: number = 0 }
              action decrement() available when gt(count, 0) {
                when true { patch count = sub(count, 1) }
              }
            }
            """);
        assertTrue(result.diagnostics().isEmpty());
        ActionNode action = (ActionNode) result.program().domain().members().get(1);
        assertNotNull(action.available());
        assertInstanceOf(FunctionCallExprNode.class, action.available());
    }
}
