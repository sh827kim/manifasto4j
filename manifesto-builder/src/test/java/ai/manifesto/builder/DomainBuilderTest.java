package ai.manifesto.builder;

import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DomainBuilder 최소 구현 테스트")
class DomainBuilderTest {

    @Test
    @DisplayName("기본 스키마 생성")
    void testBuildSchema() {
        ActionSpec action = new ActionSpec.Builder("noop")
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainBuilder builder = new DomainBuilder("test", "1.0.0")
            .addAction(action)
            .addDataField(new FieldSpec("name", "string", false, ""))
            .addComputedField(ComputedFieldDef.simple("count", new Lit(0)));

        DomainSchema schema = builder.build("hash");

        assertEquals("test", schema.getId());
        assertEquals("1.0.0", schema.getVersion());
        assertEquals(1, schema.getActions().size());
        assertEquals(1, schema.getDataFields().size());
        assertEquals(1, schema.getComputedFields().size());
    }
}
