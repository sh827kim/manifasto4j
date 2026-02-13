package ai.manifesto.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: 파서/AST 단계에서 생성된 표현식을 코어 런타임 IR 형태로 정규화합니다.
 * EN: Normalizes parser/AST expressions into core runtime IR structures.
 */
public final class Lowering {

    public Map<String, Object> lowerExprNode(Map<String, Object> node) {
        return lowerExprNodeStrict(node, LoweringContext.effectArgsContext());
    }

    public Map<String, Object> lowerExprNodeStrict(Map<String, Object> node, LoweringContext ctx) {
        if (node == null) {
            throw LoweringError.invalidShape("Node is null");
        }
        Object kindObj = node.get("kind");
        if (!(kindObj instanceof String)) {
            throw LoweringError.invalidShape("Missing or invalid 'kind' field");
        }
        String kind = (String) kindObj;
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
        return lowerRuntimePatches(patches, LoweringContext.defaultActionContext());
    }

    public List<Map<String, Object>> lowerRuntimePatchesStrict(List<Map<String, Object>> patches) {
        return lowerRuntimePatchesStrict(patches, LoweringContext.defaultActionContext());
    }

    public List<Map<String, Object>> lowerRuntimePatchesStrict(List<Map<String, Object>> patches, CompilePatchOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        return lowerRuntimePatchesStrict(patches, LoweringContext.fromPatchOptions(options));
    }

    public List<Map<String, Object>> lowerRuntimePatchesStrict(List<Map<String, Object>> patches, LoweringContext ctx) {
        Objects.requireNonNull(ctx, "ctx must not be null");
        List<Map<String, Object>> lowered = lowerRuntimePatches(patches, ctx);
        for (int i = 0; i < lowered.size(); i++) {
            Map<String, Object> patch = lowered.get(i);
            String op = requireString(patch.get("op"), "Runtime patch missing 'op'");
            String path = requireString(patch.get("path"), "Runtime patch missing 'path'");
            if (path.isBlank()) {
                throw LoweringError.invalidShape("Runtime patch 'path' must not be blank");
            }
            if (!"set".equals(op) && !"unset".equals(op) && !"merge".equals(op)) {
                throw LoweringError.invalidShape("Runtime patch op is not supported: " + op);
            }
            if (("set".equals(op) || "merge".equals(op)) && !patch.containsKey("value")) {
                throw LoweringError.invalidShape("Runtime patch '" + op + "' requires 'value'");
            }
            if ("unset".equals(op) && patch.containsKey("value")) {
                throw LoweringError.invalidShape("Runtime patch 'unset' must not include 'value'");
            }
        }
        return lowered;
    }

    public List<Map<String, Object>> lowerRuntimePatches(List<Map<String, Object>> patches, CompilePatchOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        return lowerRuntimePatches(patches, LoweringContext.fromPatchOptions(options));
    }

    public List<Map<String, Object>> lowerRuntimePatches(List<Map<String, Object>> patches, LoweringContext ctx) {
        Objects.requireNonNull(ctx, "ctx must not be null");
        List<Map<String, Object>> lowered = new ArrayList<>();
        if (patches == null) {
            throw LoweringError.invalidShape("Runtime patches are null");
        }
        for (Map<String, Object> patch : patches) {
            if (patch == null) {
                throw LoweringError.invalidShape("Runtime patch is null");
            }
            Map<String, Object> out = new LinkedHashMap<>();
            if (patch.containsKey("condition")) {
                out.put("condition", lowerExprNodeStrict(requireMap(patch.get("condition"), "Runtime patch condition is not a node"), ctx));
            }
            Object op = patch.get("op");
            Object path = patch.get("path");
            if (!(op instanceof String)) {
                throw LoweringError.invalidShape("Runtime patch missing 'op'");
            }
            if (!(path instanceof String)) {
                throw LoweringError.invalidShape("Runtime patch missing 'path'");
            }
            out.put("op", op);
            out.put("path", path);
            if (patch.containsKey("value")) {
                out.put("value", lowerExprNodeStrict(requireMap(patch.get("value"), "Runtime patch value is not a node"), ctx));
            }
            lowered.add(out);
        }
        return lowered;
    }

    public List<Map<String, Object>> lowerPatchFragments(List<Map<String, Object>> fragments) {
        return lowerPatchFragments(fragments, LoweringContext.defaultActionContext());
    }

    public List<Map<String, Object>> lowerPatchFragments(List<Map<String, Object>> fragments, CompilePatchOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        return lowerPatchFragments(fragments, LoweringContext.fromPatchOptions(options));
    }

    public List<Map<String, Object>> lowerPatchFragments(List<Map<String, Object>> fragments, LoweringContext ctx) {
        Objects.requireNonNull(ctx, "ctx must not be null");
        List<Map<String, Object>> lowered = new ArrayList<>();
        if (fragments == null) {
            throw LoweringError.invalidShape("Patch fragments are null");
        }
        LoweringContext actionCtx = ctx.withModeAndAllowItem("action", false);
        LoweringContext schemaCtx = ctx.withModeAndAllowItem("schema", false);
        for (Map<String, Object> fragment : fragments) {
            if (fragment == null) {
                throw LoweringError.invalidShape("Patch fragment is null");
            }
            Map<String, Object> out = new LinkedHashMap<>();
            Object fragmentId = fragment.get("fragmentId");
            if (!(fragmentId instanceof String)) {
                throw LoweringError.invalidShape("Patch fragment missing 'fragmentId'");
            }
            out.put("fragmentId", fragmentId);
            if (fragment.containsKey("condition")) {
                out.put("condition", lowerExprNodeStrict(requireMap(fragment.get("condition"), "Patch fragment condition is not a node"), actionCtx));
            }
            Map<String, Object> op = requireMap(fragment.get("op"), "Patch fragment op is not an object");
            if (op == null) {
                throw LoweringError.invalidShape("Patch fragment missing 'op'");
            }
            out.put("op", lowerPatchOp(op, schemaCtx));
            out.put("confidence", fragment.get("confidence"));
            lowered.add(out);
        }
        return lowered;
    }

    private Map<String, Object> lowerPatchOp(Map<String, Object> op, LoweringContext ctx) {
        String kind = requireString(op.get("kind"), "Patch op missing 'kind'");
        return switch (kind) {
            case "addType" -> mapOf(
                "kind", "addType",
                "typeName", requireString(op.get("typeName"), "addType missing 'typeName'"),
                "typeExpr", lowerTypeExpr(requireMap(op.get("typeExpr"), "addType missing 'typeExpr'"))
            );
            case "addField" -> {
                String typeName = requireString(op.get("typeName"), "addField missing 'typeName'");
                Map<String, Object> field = requireMap(op.get("field"), "addField missing 'field'");
                Map<String, Object> loweredField = new LinkedHashMap<>();
                loweredField.put("name", requireString(field.get("name"), "addField.field missing 'name'"));
                loweredField.put("type", lowerTypeExpr(requireMap(field.get("type"), "addField.field missing 'type'")));
                if (field.containsKey("optional")) {
                    loweredField.put("optional", field.get("optional"));
                }
                if (field.containsKey("defaultValue")) {
                    loweredField.put("defaultValue", field.get("defaultValue"));
                }
                yield mapOf(
                    "kind", "addField",
                    "typeName", typeName,
                    "field", loweredField
                );
            }
            case "setFieldType" -> mapOf(
                "kind", "setFieldType",
                "path", requireString(op.get("path"), "setFieldType missing 'path'"),
                "typeExpr", lowerTypeExpr(requireMap(op.get("typeExpr"), "setFieldType missing 'typeExpr'"))
            );
            case "setDefaultValue" -> mapOf(
                "kind", "setDefaultValue",
                "path", requireString(op.get("path"), "setDefaultValue missing 'path'"),
                "value", op.get("value")
            );
            case "addConstraint" -> {
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("kind", "addConstraint");
                out.put("targetPath", requireString(op.get("targetPath"), "addConstraint missing 'targetPath'"));
                out.put("rule", lowerExprNodeStrict(requireMap(op.get("rule"), "addConstraint missing 'rule'"), ctx));
                if (op.containsKey("message")) {
                    out.put("message", op.get("message"));
                }
                yield out;
            }
            case "addComputed" -> {
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("kind", "addComputed");
                out.put("name", requireString(op.get("name"), "addComputed missing 'name'"));
                out.put("expr", lowerExprNodeStrict(requireMap(op.get("expr"), "addComputed missing 'expr'"), ctx));
                if (op.containsKey("deps")) {
                    out.put("deps", op.get("deps"));
                }
                yield out;
            }
            case "addActionAvailable" -> mapOf(
                "kind", "addActionAvailable",
                "actionName", requireString(op.get("actionName"), "addActionAvailable missing 'actionName'"),
                "expr", lowerExprNodeStrict(requireMap(op.get("expr"), "addActionAvailable missing 'expr'"), ctx)
            );
            default -> throw LoweringError.invalidShape("Unknown patch op kind: " + kind);
        };
    }

    private Map<String, Object> lowerTypeExpr(Map<String, Object> typeExpr) {
        String kind = requireString(typeExpr.get("kind"), "TypeExpr missing 'kind'");
        return switch (kind) {
            case "primitive" -> mapOf(
                "kind", "primitive",
                "name", requireString(typeExpr.get("name"), "primitive TypeExpr missing 'name'")
            );
            case "array" -> mapOf(
                "kind", "array",
                "element", lowerTypeExpr(requireMap(typeExpr.get("element"), "array TypeExpr missing 'element'"))
            );
            case "object" -> {
                List<Map<String, Object>> fields = requireList(typeExpr.get("fields"), "object TypeExpr missing 'fields'");
                List<Map<String, Object>> loweredFields = new ArrayList<>();
                for (Map<String, Object> field : fields) {
                    if (field == null) {
                        throw LoweringError.invalidShape("object TypeExpr field is null");
                    }
                    Map<String, Object> outField = new LinkedHashMap<>();
                    outField.put("name", requireString(field.get("name"), "object field missing 'name'"));
                    outField.put("type", lowerTypeExpr(requireMap(field.get("type"), "object field missing 'type'")));
                    if (field.containsKey("optional")) {
                        outField.put("optional", field.get("optional"));
                    }
                    loweredFields.add(outField);
                }
                yield mapOf("kind", "object", "fields", loweredFields);
            }
            case "union" -> {
                List<Map<String, Object>> members = requireList(typeExpr.get("members"), "union TypeExpr missing 'members'");
                List<Map<String, Object>> loweredMembers = new ArrayList<>();
                for (Map<String, Object> member : members) {
                    loweredMembers.add(lowerTypeExpr(requireMap(member, "union member is not a TypeExpr")));
                }
                yield mapOf("kind", "union", "members", loweredMembers);
            }
            case "literal" -> mapOf(
                "kind", "literal",
                "value", typeExpr.get("value")
            );
            case "ref" -> mapOf(
                "kind", "ref",
                "name", requireString(typeExpr.get("name"), "ref TypeExpr missing 'name'")
            );
            default -> throw LoweringError.invalidShape("Unknown TypeExpr kind: " + kind);
        };
    }

    private Map<String, Object> lowerCall(Map<String, Object> node, LoweringContext ctx) {
        String fn = requireString(node.get("fn"), "Call missing 'fn'");
        List<Map<String, Object>> args = requireList(node.get("args"), "Call missing 'args'");

        if (isBinary(fn)) {
            if (args.size() != 2) {
                throw LoweringError.unknownCallFn(fn);
            }
            Map<String, Object> left = lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx);
            Map<String, Object> right = lowerExprNodeStrict(requireMap(args.get(1), "Call arg[1] is not a node"), ctx);
            return mapOf("kind", fn, "left", left, "right", right);
        }
        if (isUnaryArgOp(fn)) {
            if (args.size() != 1) {
                throw LoweringError.unknownCallFn(fn);
            }
            Map<String, Object> arg = lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx);
            return mapOf("kind", fn, "arg", arg);
        }
        if ("trim".equals(fn)) {
            if (args.size() != 1) {
                throw LoweringError.unknownCallFn(fn);
            }
            return mapOf("kind", "trim",
                "str", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx)
            );
        }
        if (isArgsOp(fn)) {
            return mapOf("kind", fn, "args", lowerArgs(args, ctx));
        }
        if ("if".equals(fn)) {
            if (args.size() != 3) {
                throw LoweringError.unknownCallFn(fn);
            }
            return mapOf("kind", "if",
                "cond", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx),
                "then", lowerExprNodeStrict(requireMap(args.get(1), "Call arg[1] is not a node"), ctx),
                "else", lowerExprNodeStrict(requireMap(args.get(2), "Call arg[2] is not a node"), ctx)
            );
        }
        if (isArrayArgOp(fn)) {
            if (args.size() != 1) {
                throw LoweringError.unknownCallFn(fn);
            }
            return mapOf("kind", fn,
                "array", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx)
            );
        }
        if (isObjArgOp(fn)) {
            if (args.size() != 1) {
                throw LoweringError.unknownCallFn(fn);
            }
            return mapOf("kind", fn,
                "obj", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx)
            );
        }
        if ("at".equals(fn)) {
            if (args.size() != 2) {
                throw LoweringError.unknownCallFn(fn);
            }
            return mapOf("kind", "at",
                "array", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx),
                "index", lowerExprNodeStrict(requireMap(args.get(1), "Call arg[1] is not a node"), ctx)
            );
        }
        if ("includes".equals(fn)) {
            if (args.size() != 2) {
                throw LoweringError.unknownCallFn(fn);
            }
            return mapOf("kind", "includes",
                "array", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx),
                "item", lowerExprNodeStrict(requireMap(args.get(1), "Call arg[1] is not a node"), ctx)
            );
        }
        if (isPredicateOp(fn)) {
            if (args.size() != 2) {
                throw LoweringError.unknownCallFn(fn);
            }
            LoweringContext predicateCtx = ctx.withModeAndAllowItem(ctx.getMode(), true);
            if ("map".equals(fn)) {
                return mapOf("kind", "map",
                    "array", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx),
                    "mapper", lowerExprNodeStrict(requireMap(args.get(1), "Call arg[1] is not a node"), predicateCtx)
                );
            }
            return mapOf("kind", fn,
                "array", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx),
                "predicate", lowerExprNodeStrict(requireMap(args.get(1), "Call arg[1] is not a node"), predicateCtx)
            );
        }
        if ("slice".equals(fn)) {
            if (args.size() < 2 || args.size() > 3) {
                throw LoweringError.unknownCallFn(fn);
            }
            Map<String, Object> out = mapOf("kind", "slice",
                "array", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx),
                "start", lowerExprNodeStrict(requireMap(args.get(1), "Call arg[1] is not a node"), ctx)
            );
            if (args.size() == 3) {
                out.put("end", lowerExprNodeStrict(requireMap(args.get(2), "Call arg[2] is not a node"), ctx));
            }
            return out;
        }
        if ("substring".equals(fn)) {
            if (args.size() < 2 || args.size() > 3) {
                throw LoweringError.unknownCallFn(fn);
            }
            Map<String, Object> out = mapOf("kind", "substring",
                "str", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx),
                "start", lowerExprNodeStrict(requireMap(args.get(1), "Call arg[1] is not a node"), ctx)
            );
            if (args.size() == 3) {
                out.put("end", lowerExprNodeStrict(requireMap(args.get(2), "Call arg[2] is not a node"), ctx));
            }
            return out;
        }
        if ("append".equals(fn)) {
            if (args.isEmpty()) {
                throw LoweringError.unknownCallFn(fn);
            }
            return mapOf("kind", "append",
                "array", lowerExprNodeStrict(requireMap(args.get(0), "Call arg[0] is not a node"), ctx),
                "items", lowerArgs(args.subList(1, args.size()), ctx)
            );
        }
        if ("merge".equals(fn)) {
            return mapOf("kind", "merge",
                "objects", lowerArgs(args, ctx)
            );
        }
        throw LoweringError.unknownCallFn(fn);
    }

    private Map<String, Object> lowerObject(Map<String, Object> node, LoweringContext ctx) {
        Map<String, Object> fields = new LinkedHashMap<>();
        List<Map<String, Object>> rawFields = requireList(node.get("fields"), "Object missing 'fields'");
        for (Map<String, Object> field : rawFields) {
            if (field == null) {
                throw LoweringError.invalidShape("Object field is null");
            }
            String key = requireString(field.get("key"), "Object field missing 'key'");
            Map<String, Object> value = lowerExprNodeStrict(requireMap(field.get("value"), "Object field missing 'value'"), ctx);
            fields.put(key, value);
        }
        return mapOf("kind", "object", "fields", fields);
    }

    private Map<String, Object> lowerArray(Map<String, Object> node, LoweringContext ctx) {
        List<Map<String, Object>> elements = requireList(node.get("elements"), "Array missing 'elements'");
        if (elements.isEmpty()) {
            return mapOf("kind", "lit", "value", List.of());
        }
        boolean allLiteral = true;
        List<Object> values = new ArrayList<>();
        for (Map<String, Object> element : elements) {
            if (element == null) {
                throw LoweringError.invalidShape("Array element is null");
            }
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
        if (!(pathObj instanceof List<?> list)) {
            throw LoweringError.invalidShape("Sys path must be a string array");
        }
        List<String> parts = new ArrayList<>();
        for (Object part : list) {
            if (!(part instanceof String)) {
                throw LoweringError.invalidShape("Sys path segment must be a string");
            }
            parts.add((String) part);
        }
        validateSysPrefix(parts, ctx);
        return String.join(".", parts);
    }

    private String lowerPath(Object pathObj) {
        if (!(pathObj instanceof List<?> list)) {
            throw LoweringError.invalidShape("Get path must be an array of path segments");
        }
        List<String> parts = new ArrayList<>();
        for (Object part : list) {
            if (!(part instanceof Map<?, ?> map)) {
                throw LoweringError.invalidShape("Get path segment must be an object");
            }
            Object kind = map.get("kind");
            Object name = map.get("name");
            if (!"prop".equals(kind) || !(name instanceof String)) {
                throw LoweringError.invalidShape("Get path segment must be { kind: 'prop', name: string }");
            }
            parts.add((String) name);
        }
        return String.join(".", parts);
    }

    private String lowerGetPath(Map<String, Object> node, LoweringContext ctx) {
        String path = lowerPath(node.get("path"));
        if (node.containsKey("base")) {
            Map<String, Object> base = optionalMap(node.get("base"), "Get base must be an object");
            if (base != null && "var".equals(String.valueOf(base.get("kind")))) {
                String baseName = requireString(base.get("name"), "Get base var missing 'name'");
                if (!"item".equals(baseName)) {
                    throw LoweringError.invalidShape("Only var(item) is supported");
                }
                if (!ctx.isAllowItem()) {
                    throw LoweringError.invalidKindForContext("var", ctx.getMode());
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
        String name = requireString(node.get("name"), "Var missing 'name'");
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
            case "eq", "neq", "gt", "gte", "lt", "lte", "add", "sub", "mul", "div", "mod" -> true;
            default -> false;
        };
    }

    private boolean isUnaryArgOp(String fn) {
        return switch (fn) {
            case "not", "len", "typeof", "isNull" -> true;
            default -> false;
        };
    }

    private boolean isArgsOp(String fn) {
        return switch (fn) {
            case "and", "or", "concat", "coalesce" -> true;
            default -> false;
        };
    }

    private boolean isArrayArgOp(String fn) {
        return switch (fn) {
            case "first", "last" -> true;
            default -> false;
        };
    }

    private boolean isObjArgOp(String fn) {
        return switch (fn) {
            case "keys", "values", "entries" -> true;
            default -> false;
        };
    }

    private boolean isPredicateOp(String fn) {
        return switch (fn) {
            case "filter", "find", "every", "some", "map" -> true;
            default -> false;
        };
    }

    private List<Map<String, Object>> lowerArgs(List<Map<String, Object>> args, LoweringContext ctx) {
        List<Map<String, Object>> lowered = new ArrayList<>();
        for (Map<String, Object> arg : args) {
            lowered.add(lowerExprNodeStrict(requireMap(arg, "Call arg is not a node"), ctx));
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

    private Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < entries.length; i += 2) {
            map.put(String.valueOf(entries[i]), entries[i + 1]);
        }
        return map;
    }

    private String requireString(Object value, String message) {
        if (!(value instanceof String)) {
            throw LoweringError.invalidShape(message);
        }
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> requireMap(Object obj, String message) {
        if (!(obj instanceof Map<?, ?>)) {
            throw LoweringError.invalidShape(message);
        }
        return (Map<String, Object>) obj;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> optionalMap(Object obj, String message) {
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof Map<?, ?>)) {
            throw LoweringError.invalidShape(message);
        }
        return (Map<String, Object>) obj;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> requireList(Object obj, String message) {
        if (!(obj instanceof List<?>)) {
            throw LoweringError.invalidShape(message);
        }
        return (List<T>) obj;
    }
}
