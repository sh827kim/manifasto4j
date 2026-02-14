package ai.manifesto.translator.targets.manifesto;

import ai.manifesto.intentir.DefaultIntentIrLexicon;
import ai.manifesto.intentir.DefaultIntentIrLowerer;
import ai.manifesto.intentir.DefaultIntentIrResolver;
import ai.manifesto.intentir.IntentIrLexicon;
import ai.manifesto.intentir.IntentIrLowerer;
import ai.manifesto.intentir.IntentIrResolver;

import java.util.Map;
import java.util.Set;

/**
 * KR: Manifesto exporter 실행 컨텍스트입니다.
 * EN: Execution context for Manifesto exporter.
 */
public record ManifestoExportContext(
    IntentIrLexicon lexicon,
    IntentIrResolver resolver,
    IntentIrLowerer lowerer,
    String domain,
    boolean strictValidation
) {
    public static ManifestoExportContext defaults() {
        Map<String, Set<String>> allowByDomain = Map.of("default", Set.of());
        return new ManifestoExportContext(
            new DefaultIntentIrLexicon(allowByDomain),
            new DefaultIntentIrResolver(allowByDomain),
            new DefaultIntentIrLowerer(),
            "default",
            true
        );
    }
}
