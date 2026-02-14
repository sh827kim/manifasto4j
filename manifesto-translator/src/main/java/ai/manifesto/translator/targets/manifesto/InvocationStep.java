package ai.manifesto.translator.targets.manifesto;

import ai.manifesto.intentir.IntentIrDocument;
import ai.manifesto.translator.core.ResolutionStatus;

/**
 * KR: Manifesto invocation plan 단일 스텝입니다.
 * EN: Single step in Manifesto invocation plan.
 */
public record InvocationStep(
    String nodeId,
    IntentIrDocument ir,
    ResolutionStatus resolution,
    LoweringResult lowering
) {}
