package ai.manifesto.compiler;

import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.ParseResult;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.expr.comparison.Neq;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.object.ObjectExpr;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.flow.PatchOp;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.utils.HashUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

    @Test
    @DisplayName("onceIntent는 intent guard merge로 lowering 된다")
    void lowersOnceIntentToGuardMerge() {
        ProgramNode program = parseProgram("""
            domain Test {
              state { count: number = 0 }
              action inc() {
                onceIntent { patch count = add(count, 1) }
              }
            }
            """);
        AstIrGenerator generator = new AstIrGenerator();
        GenerateResult result = generator.generate(program);
        DomainSchema schema = result.schema();
        assertNotNull(schema);

        FlowNode flow = schema.getAction("inc").getFlow();
        FlowNode.If ifNode = assertInstanceOf(FlowNode.If.class, flow);
        ExprNode cond = ifNode.getCond();
        Neq neq = assertInstanceOf(Neq.class, cond);

        String guardId = HashUtils.sha256Sync("inc:0:intent");
        assertEquals("$mel.guards.intent." + guardId, assertInstanceOf(Get.class, neq.left()).path());
        assertEquals("meta.intentId", assertInstanceOf(Get.class, neq.right()).path());

        FlowNode.Seq seq = assertInstanceOf(FlowNode.Seq.class, ifNode.getThenBranch());
        FlowNode.Patch first = assertInstanceOf(FlowNode.Patch.class, seq.getSteps().get(0));
        assertEquals(PatchOp.MERGE, first.getOp());
        assertEquals("$mel.guards.intent", first.getPath());

        ObjectExpr objectExpr = assertInstanceOf(ObjectExpr.class, first.getValue());
        ExprNode markerValue = objectExpr.fields().get(guardId);
        assertEquals("meta.intentId", assertInstanceOf(Get.class, markerValue).path());
    }
}
