package ai.manifesto.compiler;

import ai.manifesto.core.schema.DomainSchema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SimpleCompiler 테스트")
class SimpleCompilerTest {

    @Test
    @DisplayName("기본 MEL-lite 컴파일")
    void testCompile() {
        String mel = String.join("\n",
            "schema test 1.0.0",
            "field title string required",
            "action noop halt",
            "computed total 0"
        );

        SimpleCompiler compiler = new SimpleCompiler();
        CompilationResult result = compiler.compileDomain(mel);

        assertTrue(result.isOk());
        DomainSchema schema = result.getSchema();
        assertEquals("test", schema.getId());
        assertEquals("1.0.0", schema.getVersion());
        assertEquals(1, schema.getActions().size());
        assertEquals(1, schema.getDataFields().size());
        assertEquals(1, schema.getComputedFields().size());
    }

    @Test
    @DisplayName("nullable/enum 타입 파싱")
    void testNullableEnumField() {
        String mel = String.join("\n",
            "schema test 1.0.0",
            "field status enum(\"a\",\"b\",null)",
            "field note string|null",
            "action noop halt"
        );

        SimpleCompiler compiler = new SimpleCompiler();
        CompilationResult result = compiler.compileDomain(mel);

        assertTrue(result.isOk());
        DomainSchema schema = result.getSchema();
        assertFalse(schema.getDataField("status").isRequired());
        assertNotNull(schema.getDataField("status").getEnumValues());
        assertFalse(schema.getDataField("note").isRequired());
        assertEquals("string", schema.getDataField("note").getType());
    }

    @Test
    @DisplayName("action input 객체/배열 타입 파싱")
    void testActionInputObjectArray() {
        String mel = String.join("\n",
            "schema test 1.0.0",
            "field title string",
            "action add halt input=payload:object{name:string,items:array<string>}",
            "computed total 0"
        );

        SimpleCompiler compiler = new SimpleCompiler();
        CompilationResult result = compiler.compileDomain(mel);

        assertTrue(result.isOk());
        DomainSchema schema = result.getSchema();
        assertTrue(schema.getActions().containsKey("add"));
        assertTrue(schema.getActions().get("add").getInputFields().containsKey("payload"));
        var payload = schema.getActions().get("add").getInputFields().get("payload");
        assertEquals("object", payload.getType());
        assertNotNull(payload.getFields());
        assertTrue(payload.getFields().containsKey("name"));
        assertTrue(payload.getFields().containsKey("items"));
        assertEquals("array", payload.getFields().get("items").getType());
        assertNotNull(payload.getFields().get("items").getItems());
        assertEquals("string", payload.getFields().get("items").getItems().getType());
    }
}
