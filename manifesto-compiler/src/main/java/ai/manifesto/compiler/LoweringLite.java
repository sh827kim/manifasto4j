package ai.manifesto.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LoweringLite - 벡터 기반 최소 lowering 구현
 *
 * 목적: TS 벡터(lowering/evaluation)와 동일 JSON 출력 검증.
 */
public final class LoweringLite {

    public Map<String, Object> lowerExprNode(Map<String, Object> node) {
        try {
            return lowerExprNodeStrict(node, LoweringContext.effectArgsContext());
        } catch (LoweringError e) {
            return mapOf("kind", "lit", "value", null);
        }
    }

    public Map<String, Object> lowerExprNodeStrict(Map<String, Object> node, LoweringContext ctx) {
        if (node == null) {
            throw LoweringError.invalidShape("Node is null");
        }
        String kind = String.valueOf(node.get("kind"));
        return switch (kind) {
            case "lit" -> mapOf("kind", "lit", "value", node.get("value"));
            case "get" -> mapOf("kind", "get", "path", lowerGetPath(node, ctx));
            case "sys" -> mapOf("kind", "get", "path", lowerSysPath(node.get("path"), ctx));
            case "var" -> lowerVar(node, ctx);
            case "obj" -> lowerObject(node, ctx);
            case "arr" -> lowerArray(node, ctx);
            case "call" -> lowerCall(node, ctx);
            default -> throw LoweringError.unknownNodeKind(kind);
        };
    }

    public List<Map<String, Object>> lowerRuntimePatches(List<Map<String, Object>> patches) {
        return lowerRuntimePatches(patches, LoweringContext.defaultContext());
    }

    public List<Map<String, Object>> lowerRuntimePatches(List<Map<String, Object>> patches, CompilePatchOptions options) {
        return lowerRuntimePatches(patches, LoweringContext.fromPatchOptions(options));
    }

    public List<Map<String, Object>> lowerRuntimePatches(List<Map<String, Object>> patches, LoweringContext ctx) {
        List<Map<String, Object>> lowered = new ArrayList<>();
        for (Map<String, Object> patch : patches) {
            Map<String, Object> out = new LinkedHashMap<>();
            if (patch.containsKey("condition")) {
                out.put("condition", lowerExprNodeStrict(castMap(patch.get("condition")), ctx));
            }
            out.put("op", patch.get("op"));
            out.put("path", patch.get("path"));
            if (patch.containsKey("value")) {
                out.put("value", lowerExprNodeStrict(castMap(patch.get("value")), ctx));
            }
            lowered.add(out);
        }
        return lowered;
    }

    public List<Map<String, Object>> lowerPatchFragments(List<Map<String, Object>> fragments) {
        return lowerPatchFragments(fragments, LoweringContext.defaultContext());
    }

    public List<Map<String, Object>> lowerPatchFragments(List<Map<String, Object>> fragments, CompilePatchOptions options) {
        return lowerPatchFragments(fragments, LoweringContext.fromPatchOptions(options));
    }

    public List<Map<String, Object>> lowerPatchFragments(List<Map<String, Object>> fragments, LoweringContext ctx) {
        List<Map<String, Object>> lowered = new ArrayList<>();
        for (Map<String, Object> fragment : fragments) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("fragmentId", fragment.get("fragmentId"));
            if (fragment.containsKey("condition")) {
                out.put("condition", lowerExprNodeStrict(castMap(fragment.get("condition")), ctx));
            }
            Map<String, Object> op = castMap(fragment.get("op"));
            Map<String, Object> loweredOp = new LinkedHashMap<>();
            if (op != null) {
                loweredOp.put("kind", op.get("kind"));
                loweredOp.put("name", op.get("name"));
                if (op.containsKey("expr")) {
                    loweredOp.put("expr", lowerExprNodeStrict(castMap(op.get("expr")), ctx));
                }
            }
            out.put("op", loweredOp);
            out.put("confidence", fragment.get("confidence"));
            lowered.add(out);
        }
        return lowered;
    }

    private Map<String, Object> lowerCall(Map<String, Object> node, LoweringContext ctx) {
        String fn = String.valueOf(node.get("fn"));
        List<Map<String, Object>> args = castList(node.get("args"));

        if (isBinary(fn)) {
            requireArgs(args, 2, fn);
            Map<String, Object> left = lowerExprNodeStrict(args.get(0), ctx);
            Map<String, Object> right = lowerExprNodeStrict(args.get(1), ctx);
            return mapOf("kind", fn, "left", left, "right", right);
        }
        if (isUnary(fn)) {
            requireArgs(args, 1, fn);
            Map<String, Object> arg = lowerExprNodeStrict(args.get(0), ctx);
            return mapOf("kind", fn, "arg", arg);
        }
        if ("and".equals(fn) || "or".equals(fn)) {
            List<Map<String, Object>> loweredArgs = new ArrayList<>();
            for (Map<String, Object> arg : args) {
                loweredArgs.add(lowerExprNodeStrict(arg, ctx));
            }
            return mapOf("kind", fn, "args", loweredArgs);
        }
        if ("if".equals(fn)) {
            requireArgs(args, 3, fn);
            return mapOf("kind", "if",
                "cond", lowerExprNodeStrict(args.get(0), ctx),
                "then", lowerExprNodeStrict(args.get(1), ctx),
                "else", lowerExprNodeStrict(args.get(2), ctx)
            );
        }
        if ("pow".equals(fn)) {
            requireArgs(args, 2, fn);
            return mapOf("kind", "pow",
                "base", lowerExprNodeStrict(args.get(0), ctx),
                "exponent", lowerExprNodeStrict(args.get(1), ctx)
            );
        }
        if ("sqrt".equals(fn)) {
            requireArgs(args, 1, fn);
            return mapOf("kind", "sqrt",
                "arg", lowerExprNodeStrict(args.get(0), ctx)
            );
        }
        if ("toLowerCase".equals(fn) || "lower".equals(fn)) {
            requireArgs(args, 1, fn);
            return mapOf("kind", "toLowerCase",
                "str", lowerExprNodeStrict(args.get(0), ctx)
            );
        }
        if ("toUpperCase".equals(fn) || "upper".equals(fn)) {
            requireArgs(args, 1, fn);
            return mapOf("kind", "toUpperCase",
                "str", lowerExprNodeStrict(args.get(0), ctx)
            );
        }
        if ("substring".equals(fn) || "substr".equals(fn)) {
            requireArgs(args, 2, fn);
            Map<String, Object> out = mapOf("kind", "substring",
                "str", lowerExprNodeStrict(args.get(0), ctx),
                "start", lowerExprNodeStrict(args.get(1), ctx)
            );
            if (args.size() > 2) {
                out.put("end", lowerExprNodeStrict(args.get(2), ctx));
            }
            return out;
        }
        if ("filter".equals(fn)) {
            requireArgs(args, 2, fn);
            return mapOf("kind", "filter",
                "array", lowerExprNodeStrict(args.get(0), ctx),
                "predicate", lowerExprNodeStrict(args.get(1), ctx)
            );
        }
        if ("map".equals(fn)) {
            requireArgs(args, 2, fn);
            return mapOf("kind", "map",
                "array", lowerExprNodeStrict(args.get(0), ctx),
                "mapper", lowerExprNodeStrict(args.get(1), ctx)
            );
        }
        if ("find".equals(fn) || "some".equals(fn) || "every".equals(fn)) {
            requireArgs(args, 2, fn);
            return mapOf("kind", fn,
                "array", lowerExprNodeStrict(args.get(0), ctx),
                "predicate", lowerExprNodeStrict(args.get(1), ctx)
            );
        }
        if ("includes".equals(fn)) {
            requireArgs(args, 2, fn);
            return mapOf("kind", "includes",
                "array", lowerExprNodeStrict(args.get(0), ctx),
                "item", lowerExprNodeStrict(args.get(1), ctx)
            );
        }
        if ("reduce".equals(fn)) {
            requireArgs(args, 2, fn);
            Map<String, Object> out = mapOf("kind", "reduce",
                "array", lowerExprNodeStrict(args.get(0), ctx),
                "reducer", lowerExprNodeStrict(args.get(1), ctx)
            );
            if (args.size() > 2) {
                out.put("initial", lowerExprNodeStrict(args.get(2), ctx));
            }
            return out;
        }
        if ("at".equals(fn)) {
            requireArgs(args, 2, fn);
            return mapOf("kind", "at",
                "array", lowerExprNodeStrict(args.get(0), ctx),
                "index", lowerExprNodeStrict(args.get(1), ctx)
            );
        }
        if ("slice".equals(fn)) {
            requireArgs(args, 1, fn);
            Map<String, Object> out = mapOf("kind", "slice",
                "array", lowerExprNodeStrict(args.get(0), ctx));
            if (args.size() > 1) {
                out.put("start", lowerExprNodeStrict(args.get(1), ctx));
            }
            if (args.size() > 2) {
                out.put("end", lowerExprNodeStrict(args.get(2), ctx));
            }
            return out;
        }
        if ("append".equals(fn)) {
            requireArgs(args, 1, fn);
            return mapOf("kind", "append",
                "array", lowerExprNodeStrict(args.get(0), ctx),
                "items", lowerArgs(args.subList(1, args.size()), ctx)
            );
        }
        if ("merge".equals(fn)) {
            requireArgs(args, 1, fn);
            return mapOf("kind", "merge",
                "objects", lowerArgs(args, ctx)
            );
        }
        if ("concat".equals(fn) || "coalesce".equals(fn) || "min".equals(fn) || "max".equals(fn)) {
            return mapOf("kind", fn, "args", lowerArgs(args, ctx));
        }
        throw LoweringError.unknownCallFn(fn);
    }

    private Map<String, Object> lowerObject(Map<String, Object> node, LoweringContext ctx) {
        Map<String, Object> fields = new LinkedHashMap<>();
        List<Map<String, Object>> rawFields = castList(node.get("fields"));
        for (Map<String, Object> field : rawFields) {
            String key = String.valueOf(field.get("key"));
            Map<String, Object> value = lowerExprNodeStrict(castMap(field.get("value")), ctx);
            fields.put(key, value);
        }
        return mapOf("kind", "object", "fields", fields);
    }

    private Map<String, Object> lowerArray(Map<String, Object> node, LoweringContext ctx) {
        List<Map<String, Object>> elements = castList(node.get("elements"));
        if (elements.isEmpty()) {
            return mapOf("kind", "lit", "value", List.of());
        }
        boolean allLiteral = true;
        List<Object> values = new ArrayList<>();
        for (Map<String, Object> element : elements) {
            if (!"lit".equals(String.valueOf(element.get("kind")))) {
                allLiteral = false;
                break;
            }
            values.add(element.get("value"));
        }
        if (allLiteral) {
            return mapOf("kind", "lit", "value", values);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> element : elements) {
            items.add(lowerExprNodeStrict(element, ctx));
        }
        return mapOf("kind", "append", "array", mapOf("kind", "lit", "value", List.of()), "items", items);
    }

    private String lowerSysPath(Object pathObj, LoweringContext ctx) {
        if (pathObj instanceof List<?> list) {
            List<String> parts = new ArrayList<>();
            for (Object part : list) {
                parts.add(String.valueOf(part));
            }
            validateSysPrefix(parts, ctx);
            return String.join(".", parts);
        }
        String path = String.valueOf(pathObj);
        validateSysPrefix(List.of(path.split("\\\\.")), ctx);
        return path;
    }

    private String lowerPath(Object pathObj) {
        if (pathObj instanceof List<?> list) {
            List<String> parts = new ArrayList<>();
            for (Object part : list) {
                if (part instanceof Map<?, ?> map && "prop".equals(String.valueOf(map.get("kind")))) {
                    parts.add(String.valueOf(map.get("name")));
                } else {
                    parts.add(String.valueOf(part));
                }
            }
            return String.join(".", parts);
        }
        return String.valueOf(pathObj);
    }

    private String lowerGetPath(Map<String, Object> node, LoweringContext ctx) {
        String path = lowerPath(node.get("path"));
        if (node.containsKey("base")) {
            Map<String, Object> base = castMap(node.get("base"));
            if (base != null && "var".equals(String.valueOf(base.get("kind")))) {
                String baseName = String.valueOf(base.get("name"));
                if (!ctx.isAllowItem()) {
                    throw LoweringError.invalidKindForContext("var", ctx.getMode());
                }
                if (path == null || path.isEmpty() || "null".equals(path)) {
                    return "$" + baseName;
                }
                return "$" + baseName + "." + path;
            }
            if (base != null) {
                throw LoweringError.unsupportedBase(String.valueOf(base.get("kind")));
            }
        }
        return path;
    }

    private Map<String, Object> lowerVar(Map<String, Object> node, LoweringContext ctx) {
        String name = String.valueOf(node.get("name"));
        if (!ctx.isAllowItem()) {
            throw LoweringError.invalidKindForContext("var", ctx.getMode());
        }
        if (!"item".equals(name)) {
            throw LoweringError.invalidShape("Only var(item) is supported");
        }
        return mapOf("kind", "get", "path", "$" + name);
    }

    private boolean isBinary(String fn) {
        return switch (fn) {
            case "eq", "neq", "gt", "gte", "lt", "lte", "add", "sub", "mul", "div", "mod", "startsWith", "endsWith" -> true;
            default -> false;
        };
    }

    private boolean isUnary(String fn) {
        return switch (fn) {
            case "not", "isNull", "len", "abs", "neg", "round", "floor", "ceil", "typeof", "trim", "keys", "values", "entries", "first", "last" -> true;
            default -> false;
        };
    }

    private List<Map<String, Object>> lowerArgs(List<Map<String, Object>> args, LoweringContext ctx) {
        List<Map<String, Object>> lowered = new ArrayList<>();
        for (Map<String, Object> arg : args) {
            lowered.add(lowerExprNodeStrict(arg, ctx));
        }
        return lowered;
    }

    private void validateSysPrefix(List<String> parts, LoweringContext ctx) {
        if (parts.isEmpty()) {
            throw LoweringError.invalidSysPath("");
        }
        String prefix = parts.get(0);
        if (!ctx.getAllowSysPrefixes().contains(prefix)) {
            throw LoweringError.invalidSysPath(String.join(".", parts));
        }
    }

    private void requireArgs(List<?> args, int count, String fn) {
        if (args == null || args.size() < count) {
            throw LoweringError.invalidShape("Function '" + fn + "' requires at least " + count + " args");
        }
    }

    private Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < entries.length; i += 2) {
            map.put(String.valueOf(entries[i]), entries[i + 1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object obj) {
        return (Map<String, Object>) obj;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object obj) {
        return obj == null ? List.of() : (List<Map<String, Object>>) obj;
    }
}
