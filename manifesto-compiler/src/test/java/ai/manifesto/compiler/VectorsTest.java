package ai.manifesto.compiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Compiler Vectors 로딩 테스트")
class VectorsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("JSON 벡터 파일 로딩")
    void testLoadVectors() throws Exception {
        assertVectorNotEmpty("vectors/lowering.json");
        assertVectorNotEmpty("vectors/lowering-patch-fragment.json");
        assertVectorNotEmpty("vectors/lowering-runtime-patch.json");
        assertVectorNotEmpty("vectors/evaluation.json");
        assertVectorNotEmpty("vectors/evaluation-runtime-patch.json");
        assertVectorNotEmpty("vectors/ir-generator.json");
    }

    private void assertVectorNotEmpty(String resourcePath) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(input, "Missing resource: " + resourcePath);
            List<Map<String, Object>> data = mapper.readValue(input, new TypeReference<>() {});
            assertFalse(data.isEmpty(), "Empty vector list: " + resourcePath);
        }
    }
}
