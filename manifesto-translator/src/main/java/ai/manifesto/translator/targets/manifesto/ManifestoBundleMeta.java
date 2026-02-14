package ai.manifesto.translator.targets.manifesto;

/**
 * KR: Manifesto bundle 요약 메타 정보입니다.
 * EN: Summary metadata for Manifesto bundle.
 */
public record ManifestoBundleMeta(
    int nodeCount,
    int readyCount,
    int deferredCount,
    int failedCount
) {}
