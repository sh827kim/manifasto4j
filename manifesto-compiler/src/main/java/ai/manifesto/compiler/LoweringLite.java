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
        if (node == null) {
            return mapOf("kind", "lit", "value", null);
        }
        String kind = String.valueOf(node.get("kind"));
        return switch (kind) {
            case "lit" -> mapOf("kind", "lit", "value", node.get("value"));
            case "get" -> mapOf("kind", "get", "path", lowerGetPath(node));
            case "sys" -> mapOf("kind", "get", "path", lowerSysPath(node.get("path")));
            case "var" -> mapOf("kind", "get", "path", "$" + node.get("name"));
            case "obj" -> lowerObject(node);
            case "arr" -> lowerArray(node);
            case "call" -> lowerCall(node);
            default -> mapOf("kind", "lit", "value", null);
        };
    }

    public List<Map<String, Object>> lowerRuntimePatches(List<Map<String, Object>> patches) {
        List<Map<String, Object>> lowered = new ArrayList<>();
        for (Map<String, Object> patch : patches) {
            Map<String, Object> out = new LinkedHashMap<>();
            if (patch.containsKey("condition")) {
                out.put("condition", lowerExprNode(castMap(patch.get("condition"))));
            }
            out.put("op", patch.get("op"));
            out.put("path", patch.get("path"));
            if (patch.containsKey("value")) {
                out.put("value", lowerExprNode(castMap(patch.get("value"))));
            }
            lowered.add(out);
        }
        return lowered;
    }

    public List<Map<String, Object>> lowerPatchFragments(List<Map<String, Object>> fragments) {
        List<Map<String, Object>> lowered = new ArrayList<>();
        for (Map<String, Object> fragment : fragments) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("fragmentId", fragment.get("fragmentId"));
            if (fragment.containsKey("condition")) {
                out.put("condition", lowerExprNode(castMap(fragment.get("condition"))));
            }
            Map<String, Object> op = castMap(fragment.get("op"));
            Map<String, Object> loweredOp = new LinkedHashMap<>();
            if (op != null) {
                loweredOp.put("kind", op.get("kind"));
                loweredOp.put("name", op.get("name"));
                if (op.containsKey("expr")) {
                    loweredOp.put("expr", lowerExprNode(castMap(op.get("expr"))));
                }
            }
            out.put("op", loweredOp);
            out.put("confidence", fragment.get("confidence"));
            lowered.add(out);
        }
        return lowered;
    }

    private Map<String, Object> lowerCall(Map<String, Object> node) {
        String fn = String.valueOf(node.get("fn"));
        List<Map<String, Object>> args = castList(node.get("args"));

        if (isBinary(fn)) {
            Map<String, Object> left = lowerExprNode(args.get(0));
            Map<String, Object> right = lowerExprNode(args.get(1));
            return mapOf("kind", fn, "left", left, "right", right);
        }
        if (isUnary(fn)) {
            Map<String, Object> arg = lowerExprNode(args.get(0));
            return mapOf("kind", fn, "arg", arg);
        }
        if ("and".equals(fn) || "or".equals(fn)) {
            List<Map<String, Object>> loweredArgs = new ArrayList<>();
            for (Map<String, Object> arg : args) {
                loweredArgs.add(lowerExprNode(arg));
            }
            return mapOf("kind", fn, "args", loweredArgs);
        }
        if ("if".equals(fn)) {
            return mapOf("kind", "if",
                "cond", lowerExprNode(args.get(0)),
                "then", lowerExprNode(args.get(1)),
                "else", lowerExprNode(args.get(2))
            );
        }
        if ("pow".equals(fn)) {
            return mapOf("kind", "pow",
                "base", lowerExprNode(args.get(0)),
                "exponent", lowerExprNode(args.get(1))
            );
        }
        if ("sqrt".equals(fn)) {
            return mapOf("kind", "sqrt",
                "arg", lowerExprNode(args.get(0))
            );
        }
        if ("toLowerCase".equals(fn) || "lower".equals(fn)) {
            return mapOf("kind", "toLowerCase",
                "str", lowerExprNode(args.get(0))
            );
        }
        if ("toUpperCase".equals(fn) || "upper".equals(fn)) {
            return mapOf("kind", "toUpperCase",
                "str", lowerExprNode(args.get(0))
            );
        }
        if ("substring".equals(fn) || "substr".equals(fn)) {
            Map<String, Object> out = mapOf("kind", "substring",
                "str", lowerExprNode(args.get(0)),
                "start", lowerExprNode(args.get(1))
            );
            if (args.size() > 2) {
                out.put("end", lowerExprNode(args.get(2)));
            }
            return out;
        }
        if ("filter".equals(fn)) {
            return mapOf("kind", "filter",
                "array", lowerExprNode(args.get(0)),
                "predicate", lowerExprNode(args.get(1))
            );
        }
        if ("map".equals(fn)) {
            return mapOf("kind", "map",
                "array", lowerExprNode(args.get(0)),
                "mapper", lowerExprNode(args.get(1))
            );
        }
        if ("find".equals(fn) || "some".equals(fn) || "every".equals(fn)) {
            return mapOf("kind", fn,
                "array", lowerExprNode(args.get(0)),
                "predicate", lowerExprNode(args.get(1))
            );
        }
        if ("includes".equals(fn)) {
            return mapOf("kind", "includes",
                "array", lowerExprNode(args.get(0)),
                "item", lowerExprNode(args.get(1))
            );
        }
        if ("reduce".equals(fn)) {
            Map<String, Object> out = mapOf("kind", "reduce",
                "array", lowerExprNode(args.get(0)),
                "reducer", lowerExprNode(args.get(1))
            );
            if (args.size() > 2) {
                out.put("initial", lowerExprNode(args.get(2)));
            }
            return out;
        }
        if ("at".equals(fn)) {
            return mapOf("kind", "at",
                "array", lowerExprNode(args.get(0)),
                "index", lowerExprNode(args.get(1))
            );
        }
        if ("slice".equals(fn)) {
            Map<String, Object> out = mapOf("kind", "slice",
                "array", lowerExprNode(args.get(0)));
            if (args.size() > 1) {
                out.put("start", lowerExprNode(args.get(1)));
            }
            if (args.size() > 2) {
                out.put("end", lowerExprNode(args.get(2)));
            }
            return out;
        }
        if ("append".equals(fn)) {
            return mapOf("kind", "append",
                "array", lowerExprNode(args.get(0)),
                "items", lowerArgs(args.subList(1, args.size()))
            );
        }
        if ("merge".equals(fn)) {
            return mapOf("kind", "merge",
                "objects", lowerArgs(args)
            );
        }
        if ("concat".equals(fn) || "coalesce".equals(fn) || "min".equals(fn) || "max".equals(fn)) {
            return mapOf("kind", fn, "args", lowerArgs(args));
        }
        return mapOf("kind", "lit", "value", null);
    }

    private Map<String, Object> lowerObject(Map<String, Object> node) {
        Map<String, Object> fields = new LinkedHashMap<>();
        List<Map<String, Object>> rawFields = castList(node.get("fields"));
        for (Map<String, Object> field : rawFields) {
            String key = String.valueOf(field.get("key"));
            Map<String, Object> value = lowerExprNode(castMap(field.get("value")));
            fields.put(key, value);
        }
        return mapOf("kind", "object", "fields", fields);
    }

    private Map<String, Object> lowerArray(Map<String, Object> node) {
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
            items.add(lowerExprNode(element));
        }
        return mapOf("kind", "append", "array", mapOf("kind", "lit", "value", List.of()), "items", items);
    }

    private String lowerSysPath(Object pathObj) {
        if (pathObj instanceof List<?> list) {
            List<String> parts = new ArrayList<>();
            for (Object part : list) {
                parts.add(String.valueOf(part));
            }
            return String.join(".", parts);
        }
        return String.valueOf(pathObj);
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

    private String lowerGetPath(Map<String, Object> node) {
        String path = lowerPath(node.get("path"));
        if (node.containsKey("base")) {
            Map<String, Object> base = castMap(node.get("base"));
            if (base != null && "var".equals(String.valueOf(base.get("kind")))) {
                String baseName = String.valueOf(base.get("name"));
                if (path == null || path.isEmpty() || "null".equals(path)) {
                    return "$" + baseName;
                }
                return "$" + baseName + "." + path;
            }
        }
        return path;
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

    private List<Map<String, Object>> lowerArgs(List<Map<String, Object>> args) {
        List<Map<String, Object>> lowered = new ArrayList<>();
        for (Map<String, Object> arg : args) {
            lowered.add(lowerExprNode(arg));
        }
        return lowered;
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
