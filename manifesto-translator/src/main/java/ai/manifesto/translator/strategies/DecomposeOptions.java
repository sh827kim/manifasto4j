package ai.manifesto.translator.strategies;

/**
 * KR: decompose 전략 옵션입니다.
 * EN: Options for decompose strategies.
 */
public record DecomposeOptions(
    int maxChunkLength,
    int maxChunks
) {
    public static DecomposeOptions defaults() {
        return new DecomposeOptions(200, 32);
    }
}
