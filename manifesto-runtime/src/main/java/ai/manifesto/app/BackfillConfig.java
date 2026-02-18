package ai.manifesto.app;

/**
 * KR: 과거 memory 레코드 역주입(backfill) 옵션입니다.
 * EN: Backfill options for injecting historical memory records.
 */
public record BackfillConfig(boolean overwriteExisting) {
    public static BackfillConfig defaults() {
        return new BackfillConfig(false);
    }
}
