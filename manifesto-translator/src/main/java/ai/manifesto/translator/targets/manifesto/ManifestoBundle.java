package ai.manifesto.translator.targets.manifesto;

import java.util.List;

/**
 * KR: Manifesto target exporter 출력 번들입니다.
 * EN: Output bundle of Manifesto target exporter.
 */
public record ManifestoBundle(
    InvocationPlan invocationPlan,
    List<ManifestoExtensionCandidate> extensionCandidates,
    ManifestoBundleMeta meta
) {}
