package ai.manifesto.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: TranslatorMessageAdapter 구현이 최소 계약을 만족하는지 검증하는 유틸리티입니다.
 * EN: Utility that validates whether a TranslatorMessageAdapter implementation satisfies minimum contract rules.
 */
public final class TranslatorAdapterCapabilityValidator {

    public <TExternalMessage> TranslatorAdapterCapabilityReport validate(
        TranslatorMessageAdapter<TExternalMessage> adapter,
        List<TranslatorMessage> translatorSamples,
        List<TExternalMessage> externalSamples
    ) {
        Objects.requireNonNull(adapter, "adapter must not be null");
        List<String> diagnostics = new ArrayList<>();

        boolean translatorRoundTrip = validateTranslatorRoundTrip(adapter, translatorSamples, diagnostics);
        boolean externalRoundTrip = validateExternalRoundTrip(adapter, externalSamples, diagnostics);

        return new TranslatorAdapterCapabilityReport(
            translatorRoundTrip,
            externalRoundTrip,
            List.copyOf(diagnostics)
        );
    }

    private <TExternalMessage> boolean validateTranslatorRoundTrip(
        TranslatorMessageAdapter<TExternalMessage> adapter,
        List<TranslatorMessage> samples,
        List<String> diagnostics
    ) {
        if (samples == null || samples.isEmpty()) {
            diagnostics.add("TAC001: translator sample messages are required");
            return false;
        }
        List<TExternalMessage> external = adapter.toExternalMessages(samples);
        if (external == null) {
            diagnostics.add("TAC002: adapter.toExternalMessages returned null");
            return false;
        }
        List<TranslatorMessage> roundTrip = adapter.toTranslatorMessages(external);
        if (roundTrip == null) {
            diagnostics.add("TAC003: adapter.toTranslatorMessages returned null");
            return false;
        }
        if (roundTrip.size() != samples.size()) {
            diagnostics.add("TAC004: translator round-trip size mismatch");
            return false;
        }
        for (int i = 0; i < samples.size(); i++) {
            if (!equivalent(samples.get(i), roundTrip.get(i))) {
                diagnostics.add("TAC005: translator round-trip content mismatch at index " + i);
                return false;
            }
        }
        return true;
    }

    private <TExternalMessage> boolean validateExternalRoundTrip(
        TranslatorMessageAdapter<TExternalMessage> adapter,
        List<TExternalMessage> samples,
        List<String> diagnostics
    ) {
        if (samples == null || samples.isEmpty()) {
            diagnostics.add("TAC006: external sample messages are required");
            return false;
        }
        List<TranslatorMessage> translated = adapter.toTranslatorMessages(samples);
        if (translated == null) {
            diagnostics.add("TAC007: adapter.toTranslatorMessages returned null for external samples");
            return false;
        }
        List<TExternalMessage> roundTrip = adapter.toExternalMessages(translated);
        if (roundTrip == null) {
            diagnostics.add("TAC008: adapter.toExternalMessages returned null for translated messages");
            return false;
        }
        if (roundTrip.size() != samples.size()) {
            diagnostics.add("TAC009: external round-trip size mismatch");
            return false;
        }
        return true;
    }

    private boolean equivalent(TranslatorMessage expected, TranslatorMessage actual) {
        if (expected == actual) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }
        if (!Objects.equals(safe(expected.role()), safe(actual.role()))) {
            return false;
        }
        if (!Objects.equals(safe(expected.content()), safe(actual.content()))) {
            return false;
        }
        Map<String, Object> expectedAttributes = expected.attributes() == null ? Map.of() : expected.attributes();
        Map<String, Object> actualAttributes = actual.attributes() == null ? Map.of() : actual.attributes();
        return actualAttributes.entrySet().containsAll(expectedAttributes.entrySet());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
