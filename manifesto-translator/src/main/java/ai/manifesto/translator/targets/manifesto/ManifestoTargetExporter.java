package ai.manifesto.translator.targets.manifesto;

import ai.manifesto.intentir.IntentIrDocument;
import ai.manifesto.intentir.IntentIrLexiconCheckResult;
import ai.manifesto.intentir.IntentIrLowerResult;
import ai.manifesto.intentir.IntentIrResolveResult;
import ai.manifesto.translator.core.DependencyEdge;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;
import ai.manifesto.translator.core.ResolutionStatus;
import ai.manifesto.translator.targets.ExportInput;
import ai.manifesto.translator.targets.TargetExporter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: IntentGraph를 Manifesto invocation bundle로 내보내는 exporter입니다.
 * EN: Exporter that converts IntentGraph into Manifesto invocation bundle.
 */
public final class ManifestoTargetExporter implements TargetExporter<ManifestoBundle, ManifestoExportContext> {

    @Override
    public String id() {
        return "manifesto";
    }

    @Override
    public ManifestoBundle export(ExportInput input, ManifestoExportContext context) {
        ManifestoExportContext safeContext = context == null ? ManifestoExportContext.defaults() : context;
        IntentGraph graph = input.graph();
        if (graph == null || graph.nodes() == null || graph.nodes().isEmpty()) {
            return new ManifestoBundle(
                new InvocationPlan(List.of(), List.of(), List.of()),
                List.of(),
                new ManifestoBundleMeta(0, 0, 0, 0)
            );
        }

        List<IntentGraphNode> nodes = graph.nodes().stream()
            .sorted(Comparator.comparing(IntentGraphNode::nodeId))
            .toList();
        List<DependencyEdge> edges = graph.edges() == null ? List.of() : List.copyOf(graph.edges());

        Map<String, InvocationStep> stepByNodeId = new HashMap<>();
        List<ManifestoExtensionCandidate> extensionCandidates = new ArrayList<>();
        List<String> abstractNodes = new ArrayList<>();

        for (IntentGraphNode node : nodes) {
            InvocationStep lowered = lowerNode(node, safeContext, extensionCandidates);
            stepByNodeId.put(node.nodeId(), lowered);
            if (node.resolutionStatus() == ResolutionStatus.ABSTRACT) {
                abstractNodes.add(node.nodeId());
            }
        }

        List<InvocationStep> gatedSteps = applyDependencyGating(nodes, edges, stepByNodeId);

        int ready = 0;
        int deferred = 0;
        int failed = 0;
        for (InvocationStep step : gatedSteps) {
            switch (step.lowering().status()) {
                case "ready" -> ready++;
                case "deferred" -> deferred++;
                case "failed" -> failed++;
                default -> deferred++;
            }
        }

        return new ManifestoBundle(
            new InvocationPlan(List.copyOf(gatedSteps), edges, List.copyOf(abstractNodes)),
            List.copyOf(extensionCandidates),
            new ManifestoBundleMeta(gatedSteps.size(), ready, deferred, failed)
        );
    }

    private InvocationStep lowerNode(
        IntentGraphNode node,
        ManifestoExportContext context,
        List<ManifestoExtensionCandidate> extensionCandidates
    ) {
        IntentIrDocument document = toIntentIr(node, context.domain());

        if (node.resolutionStatus() != ResolutionStatus.RESOLVED) {
            return new InvocationStep(
                node.nodeId(),
                document,
                node.resolutionStatus(),
                LoweringResult.deferred("Resolution status is " + node.resolutionStatus())
            );
        }

        if (context.strictValidation()) {
            if (document.action() == null || document.action().isBlank()) {
                LoweringFailure failure = new LoweringFailure(LoweringFailureKind.SCHEMA_MISMATCH, "Action is required");
                extensionCandidates.add(melCandidate(node, failure));
                return new InvocationStep(node.nodeId(), document, node.resolutionStatus(), LoweringResult.failed(failure));
            }
            if (document.input() == null) {
                LoweringFailure failure = new LoweringFailure(LoweringFailureKind.INVALID_ARGS, "Input map is required");
                extensionCandidates.add(melCandidate(node, failure));
                return new InvocationStep(node.nodeId(), document, node.resolutionStatus(), LoweringResult.failed(failure));
            }
        }

        IntentIrLexiconCheckResult lexiconCheck = context.lexicon().check(document);
        if (!lexiconCheck.valid()) {
            String reason = lexiconCheck.diagnostics() == null || lexiconCheck.diagnostics().isEmpty()
                ? "Lexicon check failed"
                : lexiconCheck.diagnostics().get(0);
            LoweringFailure failure = new LoweringFailure(LoweringFailureKind.UNSUPPORTED_EVENT, reason);
            extensionCandidates.add(melCandidate(node, failure));
            return new InvocationStep(node.nodeId(), document, node.resolutionStatus(), LoweringResult.failed(failure));
        }

        IntentIrDocument resolvedDocument;
        try {
            IntentIrResolveResult resolved = context.resolver().resolve(document);
            resolvedDocument = resolved == null || resolved.document() == null ? document : resolved.document();
        } catch (RuntimeException error) {
            LoweringFailure failure = new LoweringFailure(LoweringFailureKind.INTERNAL_ERROR, error.getMessage());
            extensionCandidates.add(melCandidate(node, failure));
            return new InvocationStep(node.nodeId(), document, node.resolutionStatus(), LoweringResult.failed(failure));
        }

        try {
            IntentIrLowerResult lowered = context.lowerer().lower(resolvedDocument);
            if (lowered == null) {
                LoweringFailure failure = new LoweringFailure(LoweringFailureKind.INTERNAL_ERROR, "Lowerer returned null");
                extensionCandidates.add(melCandidate(node, failure));
                return new InvocationStep(node.nodeId(), resolvedDocument, node.resolutionStatus(), LoweringResult.failed(failure));
            }
            return new InvocationStep(node.nodeId(), resolvedDocument, node.resolutionStatus(), LoweringResult.ready(lowered));
        } catch (RuntimeException error) {
            LoweringFailure failure = new LoweringFailure(LoweringFailureKind.INTERNAL_ERROR, error.getMessage());
            extensionCandidates.add(melCandidate(node, failure));
            return new InvocationStep(node.nodeId(), resolvedDocument, node.resolutionStatus(), LoweringResult.failed(failure));
        }
    }

    private List<InvocationStep> applyDependencyGating(
        List<IntentGraphNode> orderedNodes,
        List<DependencyEdge> edges,
        Map<String, InvocationStep> stepByNodeId
    ) {
        Map<String, List<String>> incoming = new HashMap<>();
        for (DependencyEdge edge : edges) {
            incoming.computeIfAbsent(edge.toNodeId(), key -> new ArrayList<>()).add(edge.fromNodeId());
        }

        List<InvocationStep> steps = new ArrayList<>();
        Map<String, InvocationStep> gatedByNode = new HashMap<>();
        for (IntentGraphNode node : orderedNodes) {
            InvocationStep current = stepByNodeId.get(node.nodeId());
            InvocationStep gated = gateByDependencies(current, incoming.getOrDefault(node.nodeId(), List.of()), gatedByNode);
            steps.add(gated);
            gatedByNode.put(node.nodeId(), gated);
        }
        return steps;
    }

    private InvocationStep gateByDependencies(InvocationStep step, List<String> dependencyNodeIds, Map<String, InvocationStep> gatedByNode) {
        Objects.requireNonNull(step, "step must not be null");
        if (!step.lowering().isReady()) {
            return step;
        }

        for (String dependencyNodeId : dependencyNodeIds) {
            InvocationStep dependency = gatedByNode.get(dependencyNodeId);
            if (dependency == null || !dependency.lowering().isReady()) {
                return new InvocationStep(
                    step.nodeId(),
                    step.ir(),
                    step.resolution(),
                    LoweringResult.deferred("Blocked by dependency " + dependencyNodeId)
                );
            }
        }
        return step;
    }

    private ManifestoExtensionCandidate melCandidate(IntentGraphNode node, LoweringFailure failure) {
        return new ManifestoExtensionCandidate(
            node.nodeId(),
            "MEL_CANDIDATE",
            failure.details(),
            Map.of(
                "action", node.action() == null ? "" : node.action(),
                "failureKind", failure.kind().name()
            )
        );
    }

    private IntentIrDocument toIntentIr(IntentGraphNode node, String domain) {
        String safeDomain = domain == null || domain.isBlank() ? "default" : domain;
        Map<String, Object> meta = new HashMap<>();
        meta.put("nodeId", node.nodeId());
        meta.put("resolutionStatus", node.resolutionStatus() == null ? "UNKNOWN" : node.resolutionStatus().name());

        return new IntentIrDocument(
            "1.0",
            safeDomain,
            node.action() == null ? "" : node.action(),
            node.input() == null ? Map.of() : node.input(),
            Map.copyOf(meta)
        );
    }
}
