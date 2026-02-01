package ai.manifesto.core.core;

import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.expr.string.StartsWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ValidationUtils 표현식 경로 수집 테스트")
class ValidationUtilsTest {

    @Test
    @DisplayName("startsWith 표현식의 get 경로 수집")
    void testCollectPathsFromStartsWithExpr() {
        ExprNode startsWithExpr = StartsWith.of(new Get("data.name"), new Lit("user_"));

        List<String> paths = ValidationUtils.collectGetPathsFromExpr(startsWithExpr);

        assertEquals(1, paths.size());
        assertEquals("data.name", paths.get(0));
    }
}
