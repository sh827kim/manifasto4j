package ai.manifesto.host;

import ai.manifesto.core.Requirement;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HostErrorTaxonomyTest {

    @Test
    void hostErrorFactoryCreatesStableContracts() {
        HostError invalid = HostError.of(HostErrorCode.INVALID_ARGUMENT, "bad arg", Map.of("field", "intentId"));
        assertEquals(HostErrorCode.INVALID_ARGUMENT, invalid.code());
        assertEquals("bad arg", invalid.message());
        assertEquals("intentId", invalid.details().get("field"));
    }

    @Test
    void hostErrorsUtilityMapsKnownCases() {
        HostError missingIntentId = HostErrors.intentIdMissing();
        assertEquals(HostErrorCode.INTENT_ID_MISSING, missingIntentId.code());

        Requirement req = Requirement.builder()
            .id("req-1")
            .type("host.notify")
            .params(Map.of("k", "v"))
            .actionId("notify")
            .flowPosition(new Requirement.FlowPosition("flow.notify", 1L))
            .createdAt(1L)
            .build();
        HostError missingHandler = HostErrors.handlerNotFound(req);
        assertEquals(HostErrorCode.HANDLER_NOT_FOUND, missingHandler.code());
        assertEquals("host.notify", missingHandler.details().get("requirementType"));

        EffectExecutionError error = new EffectExecutionError(EffectExecutionErrorCode.TIMEOUT, "too slow", 2, true);
        HostError effectFailed = HostErrors.effectFailure(req, error);
        assertEquals(HostErrorCode.EFFECT_EXECUTION_FAILED, effectFailed.code());
        assertEquals("TIMEOUT", effectFailed.details().get("effectErrorCode"));

        HostError limit = HostErrors.iterationLimitExceeded(10);
        assertEquals(HostErrorCode.ITERATION_LIMIT_EXCEEDED, limit.code());
        assertEquals(10, limit.details().get("maxIterations"));
    }
}
