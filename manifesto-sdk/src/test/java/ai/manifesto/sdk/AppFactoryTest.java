package ai.manifesto.sdk;

import ai.manifesto.app.App;
import ai.manifesto.core.Intent;
import ai.manifesto.core.Patch;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.host.EffectResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppFactoryTest {

    @Test
    void createAppSupportsInitialDataAndEffects() throws Exception {
        DomainSchema schema = buildSchema();
        App app = AppFactory.createApp(
            schema,
            Map.of("status", "seed"),
            Map.of("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))))
        );

        app.ready();
        app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));

        assertEquals("ok", app.getSnapshot().getData().get("status"));
    }

    @Test
    void createAppWithConfigDelegatesToRuntimeConfig() throws Exception {
        DomainSchema schema = buildSchema();
        App app = AppFactory.createApp(
            AppConfig.sdk(
                schema,
                Map.of("status", "seed"),
                Map.of("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))))
            )
        );

        app.ready();
        app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));

        assertEquals("ok", app.getSnapshot().getData().get("status"));
    }

    private DomainSchema buildSchema() {
        FlowNode flow = FlowNode.Seq.of(
            FlowNode.Effect.of("host.notify", Map.of()),
            FlowNode.Patch.set("status", new Lit("ok")),
            FlowNode.Halt.of("done")
        );
        ActionSpec action = new ActionSpec.Builder("notify").flow(flow).build();

        DomainSchema.Builder tempBuilder = new DomainSchema.Builder("urn:test:sdk:app-factory", "1.0.0")
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""));
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        DomainSchema.Builder builder = new DomainSchema.Builder("urn:test:sdk:app-factory", "1.0.0")
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""));
        return builder.hash(hash).build();
    }
}
