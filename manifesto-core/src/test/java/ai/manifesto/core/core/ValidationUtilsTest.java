package ai.manifesto.core.core;

import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.expr.arithmetic.Add;
import ai.manifesto.core.expr.collection.Reduce;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.expr.string.StartsWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ValidationUtils 표현식 경로 수집 테스트")
class ValidationUtilsTest {

    @Test
    @DisplayName("reduce 표현식의 get 경로 수집")
    void testCollectPathsFromReduceExpr() {
        ExprNode reduceExpr = Reduce.of(
            new Get("data.values"),
            Add.of(new Get("$acc"), new Get("$item.value")),
            new Get("data.initial")
        );

        List<String> paths = ValidationUtils.collectGetPathsFromExpr(reduceExpr);

        assertTrue(paths.contains("data.values"));
        assertTrue(paths.contains("$acc"));
        assertTrue(paths.contains("$item.value"));
        assertTrue(paths.contains("data.initial"));
    }

    @Test
    @DisplayName("startsWith 표현식의 get 경로 수집")
    void testCollectPathsFromStartsWithExpr() {
        ExprNode startsWithExpr = StartsWith.of(new Get("data.name"), new Lit("user_"));

        List<String> paths = ValidationUtils.collectGetPathsFromExpr(startsWithExpr);

        assertEquals(1, paths.size());
        assertEquals("data.name", paths.get(0));
    }
}
