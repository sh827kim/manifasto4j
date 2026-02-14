package ai.manifesto.translator;

import ai.manifesto.intentir.IntentIrDocument;
import ai.manifesto.intentir.IntentIrLexicon;
import ai.manifesto.intentir.IntentIrLexiconCheckResult;
import ai.manifesto.intentir.IntentIrLowerResult;
import ai.manifesto.intentir.IntentIrLowerer;
import ai.manifesto.intentir.IntentIrResolveResult;
import ai.manifesto.intentir.IntentIrResolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: verify 이후 Intent-IR resolver/lexicon 검사를 적용해 draft를 보정하는 파이프라인 플러그인입니다.
 * EN: Pipeline plugin that applies Intent-IR resolver/lexicon checks after verify and repairs the draft.
 */
public final class IntentIrResolutionPlugin implements TranslatorPipelinePlugin {
    private final IntentIrResolver resolver;
    private final IntentIrLexicon lexicon;
    private final IntentIrLowerer lowerer;

    public IntentIrResolutionPlugin(IntentIrResolver resolver, IntentIrLexicon lexicon) {
        this(resolver, lexicon, null);
    }

    public IntentIrResolutionPlugin(IntentIrResolver resolver, IntentIrLexicon lexicon, IntentIrLowerer lowerer) {
        this.resolver = Objects.requireNonNull(resolver, "resolver must not be null");
        this.lexicon = Objects.requireNonNull(lexicon, "lexicon must not be null");
        this.lowerer = lowerer;
    }

    @Override
    public TranslationDraft afterVerify(
        TranslationRequest request,
        TranslationDraft verifiedDraft,
        List<String> diagnostics
    ) {
        IntentIrDocument source = new IntentIrDocument(
            "1.0.0",
            verifiedDraft.domainName(),
            verifiedDraft.actionName(),
            verifiedDraft.input() == null ? Map.of() : verifiedDraft.input(),
            verifiedDraft.meta() == null ? Map.of() : verifiedDraft.meta()
        );

        IntentIrResolveResult resolved = resolver.resolve(source);
        diagnostics.addAll(resolved.diagnostics());

        IntentIrLexiconCheckResult lexiconResult = lexicon.check(resolved.document());
        diagnostics.addAll(lexiconResult.diagnostics());

        IntentIrLowerResult lowered = null;
        if (lowerer != null) {
            lowered = lowerer.lower(resolved.document());
            diagnostics.addAll(lowered.diagnostics());
        }

        Map<String, Object> mergedMeta = new LinkedHashMap<>();
        if (verifiedDraft.meta() != null) {
            mergedMeta.putAll(verifiedDraft.meta());
        }
        if (resolved.document().meta() != null) {
            mergedMeta.putAll(resolved.document().meta());
        }
        mergedMeta.put("lexiconValid", lexiconResult.valid());
        if (lowered != null) {
            mergedMeta.putAll(lowered.meta());
        }

        return new TranslationDraft(
            resolved.document().domain(),
            lowered == null ? resolved.document().action() : lowered.action(),
            lowered == null ? resolved.document().input() : lowered.input(),
            mergedMeta,
            verifiedDraft.diagnostics()
        );
    }
}
