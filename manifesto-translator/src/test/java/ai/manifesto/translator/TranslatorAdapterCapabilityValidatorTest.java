package ai.manifesto.translator;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslatorAdapterCapabilityValidatorTest {

    @Test
    void validatePassesForRoundTripPreservingAdapter() {
        TranslatorAdapterCapabilityValidator validator = new TranslatorAdapterCapabilityValidator();
        TranslatorMessageAdapter<ExternalMessage> adapter = new PreservingAdapter();

        List<TranslatorMessage> translatorSamples = List.of(
            new TranslatorMessage("user", "hello", Map.of("id", "m1")),
            new TranslatorMessage("assistant", "ok", Map.of())
        );
        List<ExternalMessage> externalSamples = List.of(
            new ExternalMessage("system", "boot", Map.of("trace", "t1"))
        );

        TranslatorAdapterCapabilityReport report = validator.validate(adapter, translatorSamples, externalSamples);

        assertTrue(report.translatorRoundTripPreserved());
        assertTrue(report.externalRoundTripPreserved());
        assertTrue(report.diagnostics().isEmpty());
        assertTrue(report.isCompatible());
    }

    @Test
    void validateFailsWhenRoundTripLosesRoleOrContent() {
        TranslatorAdapterCapabilityValidator validator = new TranslatorAdapterCapabilityValidator();
        TranslatorMessageAdapter<ExternalMessage> adapter = new LossyAdapter();

        List<TranslatorMessage> translatorSamples = List.of(
            new TranslatorMessage("user", "hello", Map.of("id", "m1"))
        );
        List<ExternalMessage> externalSamples = List.of(
            new ExternalMessage("user", "hello", Map.of())
        );

        TranslatorAdapterCapabilityReport report = validator.validate(adapter, translatorSamples, externalSamples);

        assertFalse(report.translatorRoundTripPreserved());
        assertFalse(report.isCompatible());
        assertFalse(report.diagnostics().isEmpty());
    }

    private record ExternalMessage(String role, String content, Map<String, Object> attrs) {
    }

    private static final class PreservingAdapter implements TranslatorMessageAdapter<ExternalMessage> {
        @Override
        public List<TranslatorMessage> toTranslatorMessages(List<ExternalMessage> externalMessages) {
            return externalMessages.stream()
                .map(msg -> new TranslatorMessage(msg.role(), msg.content(), msg.attrs()))
                .toList();
        }

        @Override
        public List<ExternalMessage> toExternalMessages(List<TranslatorMessage> translatorMessages) {
            return translatorMessages.stream()
                .map(msg -> new ExternalMessage(msg.role(), msg.content(), msg.attributes()))
                .toList();
        }
    }

    private static final class LossyAdapter implements TranslatorMessageAdapter<ExternalMessage> {
        @Override
        public List<TranslatorMessage> toTranslatorMessages(List<ExternalMessage> externalMessages) {
            return externalMessages.stream()
                .map(msg -> new TranslatorMessage("assistant", "", Map.of()))
                .toList();
        }

        @Override
        public List<ExternalMessage> toExternalMessages(List<TranslatorMessage> translatorMessages) {
            return translatorMessages.stream()
                .map(msg -> new ExternalMessage("assistant", "", new LinkedHashMap<>()))
                .toList();
        }
    }
}
