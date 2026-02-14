package ai.manifesto.translator.strategies;

/**
 * KR: translate 전략 옵션입니다.
 * EN: Options for translate strategies.
 */
public record TranslateOptions(
    boolean preserveAttributes
) {
    public static TranslateOptions defaults() {
        return new TranslateOptions(true);
    }
}
