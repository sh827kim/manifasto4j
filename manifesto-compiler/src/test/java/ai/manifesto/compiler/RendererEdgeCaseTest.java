package ai.manifesto.compiler;

import ai.manifesto.compiler.lexer.Lexer;
import ai.manifesto.compiler.parser.Parser;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.renderer.MelRenderer;
import ai.manifesto.compiler.renderer.PatchFragmentRenderer;
import ai.manifesto.compiler.renderer.PatchOpRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Renderer edge-case tests")
class RendererEdgeCaseTest {

    @Test
    void patchOpRendererHandlesMalformedAndUnknownOps() {
        String nullOp = PatchOpRenderer.renderPatchOp(null);
        assertTrue(nullOp.contains("Unknown operation"));

        String unknown = PatchOpRenderer.renderPatchOp(Map.of("kind", "not-supported", "x", 1));
        assertTrue(unknown.contains("Unknown operation"));

        String malformedAnd = PatchOpRenderer.renderExprNode(Map.of("kind", "and"));
        assertTrue(malformedAnd.contains("malformed"));

        String malformedNot = PatchOpRenderer.renderExprNode(Map.of("kind", "not"));
        assertTrue(malformedNot.contains("malformed"));
    }

    @Test
    void patchFragmentRendererSupportsGroupingAndDomainRendering() {
        Map<String, Object> addField = Map.of(
            "fragmentId", "f1",
            "confidence", 0.9,
            "op", Map.of(
                "kind", "addField",
                "field", Map.of(
                    "name", "count",
                    "optional", false,
                    "type", Map.of("kind", "primitive", "name", "number")
                )
            )
        );
        Map<String, Object> computed = Map.of(
            "fragmentId", "f2",
            "op", Map.of(
                "kind", "addComputed",
                "name", "computed.total",
                "expr", Map.of(
                    "kind", "add",
                    "left", Map.of("kind", "get", "path", "count"),
                    "right", Map.of("kind", "lit", "value", 1)
                )
            )
        );

        String rendered = PatchFragmentRenderer.renderFragments(List.of(addField, computed));
        assertTrue(rendered.contains("count: number"));
        assertTrue(rendered.contains("computed computed.total"));

        Map<String, String> grouped = PatchFragmentRenderer.renderFragmentsByKind(List.of(addField, computed));
        assertTrue(grouped.containsKey("addField"));
        assertTrue(grouped.containsKey("addComputed"));

        String domainText = PatchFragmentRenderer.renderAsDomain("Counter", List.of(addField, computed));
        assertTrue(domainText.contains("domain Counter"));
        assertTrue(domainText.contains("state {"));
        assertTrue(domainText.contains("computed computed.total"));
    }

    @Test
    void melRendererAppliesCustomIndentAndNewline() {
        String source = """
            domain Counter {
              state { count: number = 0 }
              action increment() { stop \"done\" }
            }
            """;

        ProgramNode program = new Parser(new Lexer(source).tokenize().tokens()).parse().program();
        assertNotNull(program);

        MelRenderer.RenderOptions options = new MelRenderer.RenderOptions("    ", "\r\n");
        String rendered = MelRenderer.renderProgram(program, options);

        assertTrue(rendered.contains("\r\n"));
        assertTrue(rendered.contains("    state"));
        assertFalse(rendered.isBlank());
    }
}
