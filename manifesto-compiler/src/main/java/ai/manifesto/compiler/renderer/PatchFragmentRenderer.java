package ai.manifesto.compiler.renderer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KR: PatchFragmentRenderer는 내부 표현을 문자열 또는 출력 포맷으로 렌더링하는 타입입니다.
 * EN: PatchFragmentRenderer is a renderer type that converts internal representation into textual output format.
 */
public final class PatchFragmentRenderer {

    private PatchFragmentRenderer() {
    }

    public record FragmentRenderOptions(
        String indent,
        boolean includeComments,
        String commentPrefix,
        boolean includeMetadata,
        boolean includeEvidence,
        boolean includeConfidence,
        boolean includeFragmentId
    ) {
        public static FragmentRenderOptions defaults() {
            return new FragmentRenderOptions(
                "  ",
                true,
                "// ",
                true,
                false,
                true,
                false
            );
        }
    }

    public static String renderFragment(Map<String, Object> fragment) {
        return renderFragment(fragment, FragmentRenderOptions.defaults());
    }

    public static String renderFragment(Map<String, Object> fragment, FragmentRenderOptions options) {
        if (fragment == null) {
            return options.commentPrefix() + "Fragment: null";
        }
        List<String> lines = new ArrayList<>();
        if (options.includeMetadata()) {
            if (options.includeFragmentId()) {
                lines.add(options.commentPrefix() + "Fragment: " + fragment.get("fragmentId"));
            }
            if (options.includeConfidence()) {
                Object confidence = fragment.get("confidence");
                if (confidence instanceof Number number) {
                    int percent = (int) Math.round(number.doubleValue() * 100.0);
                    lines.add(options.commentPrefix() + "Confidence: " + percent + "%");
                }
            }
            if (options.includeEvidence()) {
                List<String> evidence = castListOfString(fragment.get("evidence"));
                if (evidence != null && !evidence.isEmpty()) {
                    lines.add(options.commentPrefix() + "Evidence:");
                    for (String item : evidence) {
                        lines.add(options.commentPrefix() + "  - " + item);
                    }
                }
            }
        }

        Map<String, Object> op = castMap(fragment.get("op"));
        PatchOpRenderer.RenderOptions opOptions = new PatchOpRenderer.RenderOptions(
            options.indent(),
            options.includeComments(),
            options.commentPrefix()
        );
        lines.add(PatchOpRenderer.renderPatchOp(op, opOptions));
        return String.join("\n", lines);
    }

    public static String renderFragments(List<Map<String, Object>> fragments) {
        return renderFragments(fragments, FragmentRenderOptions.defaults());
    }

    public static String renderFragments(List<Map<String, Object>> fragments, FragmentRenderOptions options) {
        if (fragments == null || fragments.isEmpty()) {
            return "";
        }
        List<String> rendered = new ArrayList<>();
        for (Map<String, Object> fragment : fragments) {
            rendered.add(renderFragment(fragment, options));
        }
        return String.join("\n\n", rendered);
    }

    public static Map<String, String> renderFragmentsByKind(List<Map<String, Object>> fragments) {
        return renderFragmentsByKind(fragments, FragmentRenderOptions.defaults());
    }

    public static Map<String, String> renderFragmentsByKind(List<Map<String, Object>> fragments, FragmentRenderOptions options) {
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        if (fragments != null) {
            for (Map<String, Object> fragment : fragments) {
                Map<String, Object> op = castMap(fragment.get("op"));
                String kind = op == null ? "unknown" : String.valueOf(op.get("kind"));
                grouped.computeIfAbsent(kind, k -> new ArrayList<>()).add(fragment);
            }
        }
        Map<String, String> rendered = new LinkedHashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            rendered.put(entry.getKey(), renderFragments(entry.getValue(), options));
        }
        return rendered;
    }

    public static String renderAsDomain(String domainName, List<Map<String, Object>> fragments) {
        return renderAsDomain(domainName, fragments, FragmentRenderOptions.defaults());
    }

    public static String renderAsDomain(String domainName, List<Map<String, Object>> fragments, FragmentRenderOptions options) {
        String indent = options.indent();

        List<Map<String, Object>> types = new ArrayList<>();
        List<Map<String, Object>> fields = new ArrayList<>();
        List<Map<String, Object>> defaults = new ArrayList<>();
        List<Map<String, Object>> computed = new ArrayList<>();
        List<Map<String, Object>> constraints = new ArrayList<>();
        List<Map<String, Object>> actions = new ArrayList<>();

        if (fragments != null) {
            for (Map<String, Object> fragment : fragments) {
                Map<String, Object> op = castMap(fragment.get("op"));
                if (op == null) {
                    continue;
                }
                String kind = String.valueOf(op.get("kind"));
                switch (kind) {
                    case "addType" -> types.add(fragment);
                    case "addField", "setFieldType" -> fields.add(fragment);
                    case "setDefaultValue" -> defaults.add(fragment);
                    case "addComputed" -> computed.add(fragment);
                    case "addConstraint" -> constraints.add(fragment);
                    case "addActionAvailable" -> actions.add(fragment);
                    default -> {
                    }
                }
            }
        }

        List<String> lines = new ArrayList<>();
        lines.add("domain " + domainName + " {");

        if (!fields.isEmpty() || !defaults.isEmpty()) {
            lines.add(indent + "state {");
            List<Map<String, Object>> combined = new ArrayList<>(fields);
            combined.addAll(defaults);
            for (Map<String, Object> fragment : combined) {
                String rendered = PatchOpRenderer.renderPatchOp(
                    castMap(fragment.get("op")),
                    new PatchOpRenderer.RenderOptions(indent + indent, false, options.commentPrefix())
                );
                lines.add(indent + indent + rendered);
            }
            lines.add(indent + "}");
            lines.add("");
        }

        for (Map<String, Object> fragment : types) {
            String rendered = PatchOpRenderer.renderPatchOp(
                castMap(fragment.get("op")),
                new PatchOpRenderer.RenderOptions(indent, false, options.commentPrefix())
            );
            for (String line : rendered.split("\n", -1)) {
                lines.add(indent + line);
            }
            lines.add("");
        }

        for (Map<String, Object> fragment : computed) {
            String rendered = PatchOpRenderer.renderPatchOp(
                castMap(fragment.get("op")),
                new PatchOpRenderer.RenderOptions(indent, false, options.commentPrefix())
            );
            lines.add(indent + rendered);
        }
        if (!computed.isEmpty()) {
            lines.add("");
        }

        for (Map<String, Object> fragment : constraints) {
            String rendered = PatchOpRenderer.renderPatchOp(
                castMap(fragment.get("op")),
                new PatchOpRenderer.RenderOptions(indent, true, options.commentPrefix())
            );
            lines.add(indent + rendered);
        }
        if (!constraints.isEmpty()) {
            lines.add("");
        }

        for (Map<String, Object> fragment : actions) {
            String rendered = PatchOpRenderer.renderPatchOp(
                castMap(fragment.get("op")),
                new PatchOpRenderer.RenderOptions(indent, false, options.commentPrefix())
            );
            for (String line : rendered.split("\n", -1)) {
                lines.add(indent + line);
            }
            lines.add("");
        }

        lines.add("}");
        return String.join("\n", lines);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> castListOfString(Object value) {
        if (value instanceof List<?> list) {
            if (list.isEmpty() || list.get(0) instanceof String) {
                return (List<String>) list;
            }
        }
        return null;
    }
}
