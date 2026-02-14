package ai.manifesto.host;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * KR: Host golden 벡터 로딩/JSON 동등성 검증을 표준화하는 테스트 하네스입니다.
 * EN: Test harness standardizing Host golden vector loading and JSON equality checks.
 */
final class HostGoldenVectorHarness {
    private final ObjectMapper mapper = new ObjectMapper();

    List<Map<String, Object>> loadVectors(String resourcePath) throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertNotNull(input, "Golden resource not found: " + resourcePath);
        return mapper.readValue(input, new TypeReference<List<Map<String, Object>>>() {});
    }

    List<Map<String, Object>> loadVectors(List<String> resourcePaths) throws Exception {
        List<Map<String, Object>> merged = new ArrayList<>();
        for (String resourcePath : resourcePaths) {
            merged.addAll(loadVectors(resourcePath));
        }
        return List.copyOf(merged);
    }

    void assertJsonEquals(Map<String, Object> expected, Map<String, Object> actual, String message) throws Exception {
        JsonNode expectedNode = mapper.valueToTree(expected);
        JsonNode actualNode = mapper.valueToTree(actual);
        assertEquals(expectedNode, actualNode, message);
    }
}
