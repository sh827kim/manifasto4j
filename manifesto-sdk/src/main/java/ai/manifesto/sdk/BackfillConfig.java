package ai.manifesto.sdk;

/**
 * KR: SDK memory backfill 옵션입니다.
 * EN: SDK memory backfill options.
 */
public record BackfillConfig(boolean overwriteExisting) {
    public static BackfillConfig defaults() {
        return new BackfillConfig(false);
    }
}
