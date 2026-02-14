package ai.manifesto.translator.strategies;

/**
 * KR: merge 전략 옵션입니다.
 * EN: Options for merge strategies.
 */
public record MergeOptions(
    boolean aggressiveMode,
    int maxMergeDepth
) {
    public static MergeOptions conservative() {
        return new MergeOptions(false, 1);
    }

    public static MergeOptions aggressive() {
        return new MergeOptions(true, 4);
    }
}
