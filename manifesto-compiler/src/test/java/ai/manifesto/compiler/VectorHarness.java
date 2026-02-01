package ai.manifesto.compiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * VectorHarness - JSON 벡터 로더/비교 유틸
 */
public final class VectorHarness {
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, Object>> load(String resourcePath) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing resource: " + resourcePath);
            }
            return mapper.readValue(input, new TypeReference<>() {});
        }
    }

    public void assertJsonEquals(Object expected, Object actual) throws Exception {
        JsonNode expectedNode = mapper.valueToTree(expected);
        JsonNode actualNode = mapper.valueToTree(actual);
        if (!expectedNode.equals(actualNode)) {
            throw new AssertionError("JSON mismatch\nExpected: " + expectedNode + "\nActual: " + actualNode);
        }
    }
}
