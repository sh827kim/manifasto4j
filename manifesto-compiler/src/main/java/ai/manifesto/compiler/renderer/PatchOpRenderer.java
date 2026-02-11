package ai.manifesto.compiler.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * KR: PatchOpRenderer는 내부 표현을 문자열 또는 출력 포맷으로 렌더링하는 타입입니다.
 * EN: PatchOpRenderer is a renderer type that converts internal representation into textual output format.
 */
public final class PatchOpRenderer {

    private PatchOpRenderer() {
    }

    public record RenderOptions(String indent, boolean includeComments, String commentPrefix) {
        public static RenderOptions defaults() {
            return new RenderOptions("  ", false, "// ");
        }
    }

    public static String renderPatchOp(Map<String, Object> op) {
        return renderPatchOp(op, RenderOptions.defaults());
    }

    public static String renderPatchOp(Map<String, Object> op, RenderOptions options) {
        if (op == null) {
            return options.commentPrefix() + "Unknown operation: null";
        }
        String kind = asString(op.get("kind"));
        if (kind == null) {
            return options.commentPrefix() + "Unknown operation: " + op;
        }
        return switch (kind) {
            case "addType" -> renderAddType(op, options);
            case "addField" -> renderAddField(op, options);
            case "setFieldType" -> renderSetFieldType(op, options);
            case "setDefaultValue" -> renderSetDefaultValue(op, options);
            case "addConstraint" -> renderAddConstraint(op, options);
            case "addComputed" -> renderAddComputed(op, options);
            case "addActionAvailable" -> renderAddActionAvailable(op, options);
            default -> options.commentPrefix() + "Unknown operation: " + op;
        };
    }

    private static String renderAddType(Map<String, Object> op, RenderOptions options) {
        String typeName = asString(op.get("typeName"));
        Map<String, Object> typeExpr = castMap(op.get("typeExpr"));
        if (typeExpr != null && "object".equals(asString(typeExpr.get("kind")))) {
            List<Map<String, Object>> fields = castList(typeExpr.get("fields"));
            List<String> lines = new ArrayList<>();
            lines.add("type " + typeName + " {");
            if (fields != null) {
                for (Map<String, Object> field : fields) {
                    lines.add(options.indent() + renderTypeField(field, null));
                }
            }
            lines.add("}");
            return String.join("\n", lines);
        }
        return "type " + typeName + " = " + renderTypeExpr(typeExpr);
    }

    private static String renderAddField(Map<String, Object> op, RenderOptions options) {
        Map<String, Object> field = castMap(op.get("field"));
        if (field == null) {
            return options.commentPrefix() + "Unknown operation: " + op;
        }
        Object defaultValue = null;
        boolean hasDefault = false;
        if (field.containsKey("defaultValue")) {
            defaultValue = field.get("defaultValue");
            hasDefault = true;
        }
        return renderTypeField(field, hasDefault ? defaultValue : null);
    }

    private static String renderSetFieldType(Map<String, Object> op, RenderOptions options) {
        String path = asString(op.get("path"));
        Map<String, Object> typeExpr = castMap(op.get("typeExpr"));
        String typeStr = renderTypeExpr(typeExpr);
        String fieldName = extractFieldName(path);

        List<String> lines = new ArrayList<>();
        if (options.includeComments()) {
            lines.add(options.commentPrefix() + "Change " + path + " type to: " + typeStr);
        }
        lines.add(fieldName + ": " + typeStr);
        return String.join("\n", lines);
    }

    private static String renderSetDefaultValue(Map<String, Object> op, RenderOptions options) {
        String path = asString(op.get("path"));
        String fieldName = extractFieldName(path);
        String valueStr = renderValue(op.get("value"));

        List<String> lines = new ArrayList<>();
        if (options.includeComments()) {
            lines.add(options.commentPrefix() + "Set default value for " + path);
        }
        lines.add(fieldName + " = " + valueStr);
        return String.join("\n", lines);
    }

    private static String renderAddConstraint(Map<String, Object> op, RenderOptions options) {
        String targetPath = asString(op.get("targetPath"));
        Map<String, Object> rule = castMap(op.get("rule"));
        String exprStr = renderExprNode(rule);
        String messageStr = op.containsKey("message") ? " - " + renderValue(op.get("message")) : "";
        return options.commentPrefix() + "Constraint on " + targetPath + ": " + exprStr + messageStr;
    }

    private static String renderAddComputed(Map<String, Object> op, RenderOptions options) {
        String name = asString(op.get("name"));
        Map<String, Object> expr = castMap(op.get("expr"));
        String exprStr = renderExprNode(expr);

        List<String> lines = new ArrayList<>();
        List<String> deps = castListOfString(op.get("deps"));
        if (options.includeComments() && deps != null && !deps.isEmpty()) {
            lines.add(options.commentPrefix() + "Dependencies: " + String.join(", ", deps));
        }
        lines.add("computed " + name + " = " + exprStr);
        return String.join("\n", lines);
    }

    private static String renderAddActionAvailable(Map<String, Object> op, RenderOptions options) {
        String actionName = asString(op.get("actionName"));
        Map<String, Object> expr = castMap(op.get("expr"));
        String exprStr = renderExprNode(expr);

        List<String> lines = new ArrayList<>();
        if (options.includeComments()) {
            lines.add(options.commentPrefix() + "Add availability condition to " + actionName);
        }
        lines.add("action " + actionName + "() available when " + exprStr + " {");
        lines.add(options.indent() + "// action body...");
        lines.add("}");
        return String.join("\n", lines);
    }

    public static String renderExprNode(Map<String, Object> expr) {
        if (expr == null) {
            return "null";
        }
        String kind = asString(expr.get("kind"));
        if (kind == null) {
            return "/* unknown: " + expr + " */";
        }
        return switch (kind) {
            case "lit" -> renderValue(expr.get("value"));
            case "get" -> renderPath(asString(expr.get("path")));
            case "eq", "neq", "gt", "gte", "lt", "lte" -> {
                Map<String, Object> left = castMap(expr.get("left"));
                Map<String, Object> right = castMap(expr.get("right"));
                yield kind + "(" + renderExprNode(left) + ", " + renderExprNode(right) + ")";
            }
            case "and", "or" -> {
                List<Map<String, Object>> args = castList(expr.get("args"));
                if (args == null) {
                    yield kind + "(/* malformed: args undefined */)";
                }
                StringJoiner joiner = new StringJoiner(", ");
                for (Map<String, Object> arg : args) {
                    joiner.add(renderExprNode(arg));
                }
                yield kind + "(" + joiner + ")";
            }
            case "not" -> {
                Map<String, Object> arg = castMap(expr.get("arg"));
                if (arg == null) {
                    yield "not(/* malformed: arg undefined */)";
                }
                yield "not(" + renderExprNode(arg) + ")";
            }
            case "if" -> {
                Map<String, Object> cond = castMap(expr.get("cond"));
                Map<String, Object> thenExpr = castMap(expr.get("then"));
                Map<String, Object> elseExpr = castMap(expr.get("else"));
                yield "if(" + renderExprNode(cond) + ", " + renderExprNode(thenExpr) + ", " + renderExprNode(elseExpr) + ")";
            }
            case "add", "sub", "mul", "div", "mod" -> {
                Map<String, Object> left = castMap(expr.get("left"));
                Map<String, Object> right = castMap(expr.get("right"));
                yield kind + "(" + renderExprNode(left) + ", " + renderExprNode(right) + ")";
            }
            case "concat" -> {
                List<Map<String, Object>> args = castList(expr.get("args"));
                if (args == null) {
                    yield "concat(/* malformed: args undefined */)";
                }
                StringJoiner joiner = new StringJoiner(", ");
                for (Map<String, Object> arg : args) {
                    joiner.add(renderExprNode(arg));
                }
                yield "concat(" + joiner + ")";
            }
            case "substring" -> {
                Map<String, Object> str = castMap(expr.get("str"));
                Map<String, Object> start = castMap(expr.get("start"));
                Map<String, Object> end = castMap(expr.get("end"));
                if (end != null) {
                    yield "substring(" + renderExprNode(str) + ", " + renderExprNode(start) + ", " + renderExprNode(end) + ")";
                }
                yield "substring(" + renderExprNode(str) + ", " + renderExprNode(start) + ")";
            }
            case "trim" -> "trim(" + renderExprNode(castMap(expr.get("str"))) + ")";
            case "len" -> "len(" + renderExprNode(castMap(expr.get("arg"))) + ")";
            case "at" -> "at(" + renderExprNode(castMap(expr.get("array"))) + ", " + renderExprNode(castMap(expr.get("index"))) + ")";
            case "first" -> "first(" + renderExprNode(castMap(expr.get("array"))) + ")";
            case "last" -> "last(" + renderExprNode(castMap(expr.get("array"))) + ")";
            case "slice" -> {
                Map<String, Object> array = castMap(expr.get("array"));
                Map<String, Object> start = castMap(expr.get("start"));
                Map<String, Object> end = castMap(expr.get("end"));
                if (end != null) {
                    yield "slice(" + renderExprNode(array) + ", " + renderExprNode(start) + ", " + renderExprNode(end) + ")";
                }
                yield "slice(" + renderExprNode(array) + ", " + renderExprNode(start) + ")";
            }
            case "includes" -> "includes(" + renderExprNode(castMap(expr.get("array"))) + ", " + renderExprNode(castMap(expr.get("item"))) + ")";
            case "filter" -> "filter(" + renderExprNode(castMap(expr.get("array"))) + ", " + renderExprNode(castMap(expr.get("predicate"))) + ")";
            case "map" -> "map(" + renderExprNode(castMap(expr.get("array"))) + ", " + renderExprNode(castMap(expr.get("mapper"))) + ")";
            case "find" -> "find(" + renderExprNode(castMap(expr.get("array"))) + ", " + renderExprNode(castMap(expr.get("predicate"))) + ")";
            case "every" -> "every(" + renderExprNode(castMap(expr.get("array"))) + ", " + renderExprNode(castMap(expr.get("predicate"))) + ")";
            case "some" -> "some(" + renderExprNode(castMap(expr.get("array"))) + ", " + renderExprNode(castMap(expr.get("predicate"))) + ")";
            case "append" -> {
                List<Map<String, Object>> items = castList(expr.get("items"));
                if (items == null) {
                    yield "append(" + renderExprNode(castMap(expr.get("array"))) + ", /* malformed: items undefined */)";
                }
                StringJoiner joiner = new StringJoiner(", ");
                for (Map<String, Object> item : items) {
                    joiner.add(renderExprNode(item));
                }
                yield "append(" + renderExprNode(castMap(expr.get("array"))) + ", " + joiner + ")";
            }
            case "object" -> renderObjectExpr(castMap(expr.get("fields")));
            case "keys" -> "keys(" + renderExprNode(castMap(expr.get("obj"))) + ")";
            case "values" -> "values(" + renderExprNode(castMap(expr.get("obj"))) + ")";
            case "entries" -> "entries(" + renderExprNode(castMap(expr.get("obj"))) + ")";
            case "merge" -> {
                List<Map<String, Object>> objects = castList(expr.get("objects"));
                if (objects == null) {
                    yield "merge(/* malformed: objects undefined */)";
                }
                StringJoiner joiner = new StringJoiner(", ");
                for (Map<String, Object> object : objects) {
                    joiner.add(renderExprNode(object));
                }
                yield "merge(" + joiner + ")";
            }
            case "typeof" -> "typeof(" + renderExprNode(castMap(expr.get("arg"))) + ")";
            case "isNull" -> "isNull(" + renderExprNode(castMap(expr.get("arg"))) + ")";
            case "coalesce" -> {
                List<Map<String, Object>> args = castList(expr.get("args"));
                if (args == null) {
                    yield "coalesce(/* malformed: args undefined */)";
                }
                StringJoiner joiner = new StringJoiner(", ");
                for (Map<String, Object> arg : args) {
                    joiner.add(renderExprNode(arg));
                }
                yield "coalesce(" + joiner + ")";
            }
            default -> "/* unknown: " + expr + " */";
        };
    }

    public static String renderTypeExpr(Map<String, Object> typeExpr) {
        if (typeExpr == null) {
            return "any";
        }
        String kind = asString(typeExpr.get("kind"));
        if (kind == null) {
            return "any";
        }
        return switch (kind) {
            case "primitive" -> asString(typeExpr.get("name"));
            case "literal" -> renderValue(typeExpr.get("value"));
            case "ref" -> asString(typeExpr.get("name"));
            case "array" -> "Array<" + renderTypeExpr(castMap(typeExpr.get("element"))) + ">";
            case "record" -> "Record<" + renderTypeExpr(castMap(typeExpr.get("key"))) + ", " + renderTypeExpr(castMap(typeExpr.get("value"))) + ">";
            case "union" -> renderUnion(castList(typeExpr.get("members")));
            case "object" -> renderObjectType(castList(typeExpr.get("fields")));
            default -> "any";
        };
    }

    public static String renderTypeField(Map<String, Object> field, Object defaultValue) {
        String name = asString(field.get("name"));
        boolean optional = Boolean.TRUE.equals(field.get("optional"));
        String typeStr = renderTypeExpr(castMap(field.get("type")));
        if (defaultValue != null || field.containsKey("defaultValue")) {
            return name + ": " + typeStr + " = " + renderValue(defaultValue);
        }
        if (optional) {
            return name + "?: " + typeStr;
        }
        return name + ": " + typeStr;
    }

    public static String renderValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String s) {
            return "\"" + escapeString(s) + "\"";
        }
        if (value instanceof Boolean b) {
            return b ? "true" : "false";
        }
        if (value instanceof Number n) {
            return String.valueOf(n);
        }
        if (value instanceof List<?> list) {
            StringJoiner joiner = new StringJoiner(", ");
            for (Object item : list) {
                joiner.add(renderValue(item));
            }
            return "[" + joiner + "]";
        }
        if (value instanceof Map<?, ?> map) {
            StringJoiner joiner = new StringJoiner(", ");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                joiner.add(key + ": " + renderValue(entry.getValue()));
            }
            return "{ " + joiner + " }";
        }
        return String.valueOf(value);
    }

    private static String renderUnion(List<Map<String, Object>> members) {
        if (members == null || members.isEmpty()) {
            return "never";
        }
        StringJoiner joiner = new StringJoiner(" | ");
        for (Map<String, Object> member : members) {
            joiner.add(renderTypeExpr(member));
        }
        return joiner.toString();
    }

    private static String renderObjectType(List<Map<String, Object>> fields) {
        if (fields == null || fields.isEmpty()) {
            return "{}";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (Map<String, Object> field : fields) {
            joiner.add(renderTypeField(field, null));
        }
        return "{ " + joiner + " }";
    }

    private static String renderObjectExpr(Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return "{ }";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            Object value = entry.getValue();
            String rendered = value instanceof Map<?, ?> map
                ? renderExprNode(castMap(map))
                : renderValue(value);
            joiner.add(entry.getKey() + ": " + rendered);
        }
        return "{ " + joiner + " }";
    }

    private static String renderPath(String path) {
        if (path == null) {
            return "null";
        }
        if (path.startsWith("$meta.") || path.startsWith("$system.") || path.startsWith("$input.")) {
            return path;
        }
        if (path.startsWith("data.")) {
            return path.substring(5);
        }
        if (path.startsWith("computed.")) {
            return path.substring(9);
        }
        return path;
    }

    private static String extractFieldName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        int lastDot = path.lastIndexOf('.');
        if (lastDot >= 0 && lastDot + 1 < path.length()) {
            return path.substring(lastDot + 1);
        }
        return path;
    }

    private static String escapeString(String str) {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object value) {
        if (value instanceof List<?> list) {
            if (list.isEmpty() || list.get(0) instanceof Map<?, ?>) {
                return (List<Map<String, Object>>) list;
            }
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

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
