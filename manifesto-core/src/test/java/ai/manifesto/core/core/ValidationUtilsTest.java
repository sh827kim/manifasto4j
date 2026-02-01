package ai.manifesto.core.core;

import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.expr.string.Substring;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ValidationUtils 표현식 경로 수집 테스트")
class ValidationUtilsTest {

    @Test
    @DisplayName("substring 표현식의 get 경로 수집")
    void testCollectPathsFromSubstringExpr() {
        ExprNode substringExpr = Substring.of(new Get("name"), new Lit(0), new Lit(4));

        List<String> paths = ValidationUtils.collectGetPathsFromExpr(substringExpr);

        assertEquals(1, paths.size());
        assertEquals("name", paths.get(0));
    }
}
