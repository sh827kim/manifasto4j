package ai.manifesto.translator.strategies;

import ai.manifesto.translator.TranslationRequest;
import ai.manifesto.translator.core.Chunk;
import ai.manifesto.translator.core.ExecutionPlan;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.helpers.ExecutionPlanBuilder;

import java.util.List;
import java.util.Objects;

/**
 * KR: decompose/translate/merge 전략을 조합해 execution plan까지 산출하는 실행기입니다.
 * EN: Composer that combines decompose/translate/merge strategies and produces execution plans.
 */
public final class StrategyComposer {
    private final DecomposeStrategy decomposeStrategy;
    private final TranslateStrategy translateStrategy;
    private final MergeStrategy mergeStrategy;

    public StrategyComposer(
        DecomposeStrategy decomposeStrategy,
        TranslateStrategy translateStrategy,
        MergeStrategy mergeStrategy
    ) {
        this.decomposeStrategy = Objects.requireNonNull(decomposeStrategy, "decomposeStrategy is required");
        this.translateStrategy = Objects.requireNonNull(translateStrategy, "translateStrategy is required");
        this.mergeStrategy = Objects.requireNonNull(mergeStrategy, "mergeStrategy is required");
    }

    public ExecutionPlan compose(
        TranslationRequest request,
        DecomposeOptions decomposeOptions,
        TranslateOptions translateOptions,
        MergeOptions mergeOptions
    ) {
        return composeResult(request, decomposeOptions, translateOptions, mergeOptions).executionPlan();
    }

    public StrategyCompositionResult composeResult(
        TranslationRequest request,
        DecomposeOptions decomposeOptions,
        TranslateOptions translateOptions,
        MergeOptions mergeOptions
    ) {
        List<Chunk> chunks = decomposeStrategy.decompose(request, decomposeOptions);
        IntentGraph baseGraph = translateStrategy.translate(chunks, translateOptions);
        IntentGraph merged = mergeStrategy.merge(List.of(baseGraph), mergeOptions);
        ExecutionPlan executionPlan = new ExecutionPlanBuilder().build(merged);
        return new StrategyCompositionResult(merged, executionPlan);
    }
}
