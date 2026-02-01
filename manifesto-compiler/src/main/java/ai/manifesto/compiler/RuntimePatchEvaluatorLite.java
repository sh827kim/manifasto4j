package ai.manifesto.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RuntimePatchEvaluatorLite - 벡터 기반 최소 evaluator
 */
public final class RuntimePatchEvaluatorLite {
    public EvaluationResult evaluate(List<Map<String, Object>> patches, SnapshotContext snapshot) {
        List<Map<String, Object>> outPatches = new ArrayList<>();
        List<Map<String, Object>> skipped = new ArrayList<>();

        for (Map<String, Object> patch : patches) {
            if (patch.containsKey("condition")) {
                Object cond = evaluateExpr(castMap(patch.get("condition")), snapshot);
                if (!(cond instanceof Boolean) || !((Boolean) cond)) {
                    skipped.add(patch);
                    continue;
                }
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("op", patch.get("op"));
            out.put("path", patch.get("path"));
            if (patch.containsKey("value")) {
                Object value = evaluateExpr(castMap(patch.get("value")), snapshot);
                out.put("value", value);
            }
            outPatches.add(out);
        }

        SnapshotContext finalSnapshot = apply(outPatches, snapshot);
        return new EvaluationResult(outPatches, skipped, finalSnapshot);
    }

    public Object evaluateExpr(Map<String, Object> expr, SnapshotContext snapshot) {
        String kind = String.valueOf(expr.get("kind"));
        return switch (kind) {
            case "lit" -> expr.get("value");
            case "get" -> getByPath(snapshot, String.valueOf(expr.get("path")));
            case "eq" -> eq(evaluateExpr(castMap(expr.get("left")), snapshot),
                            evaluateExpr(castMap(expr.get("right")), snapshot));
            case "neq" -> !eq(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "gt" -> gt(evaluateExpr(castMap(expr.get("left")), snapshot),
                            evaluateExpr(castMap(expr.get("right")), snapshot));
            case "gte" -> gte(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "lt" -> lt(evaluateExpr(castMap(expr.get("left")), snapshot),
                            evaluateExpr(castMap(expr.get("right")), snapshot));
            case "lte" -> lte(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "add" -> add(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "sub" -> sub(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "mul" -> mul(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "div" -> div(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "mod" -> mod(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "and" -> and(expr, snapshot);
            case "or" -> or(expr, snapshot);
            case "len" -> len(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "not" -> !truthy(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "isNull" -> evaluateExpr(castMap(expr.get("arg")), snapshot) == null;
            case "abs" -> abs(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "neg" -> neg(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "round" -> round(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "floor" -> floor(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "ceil" -> ceil(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "if" -> truthy(evaluateExpr(castMap(expr.get("cond")), snapshot))
                ? evaluateExpr(castMap(expr.get("then")), snapshot)
                : evaluateExpr(castMap(expr.get("else")), snapshot);
            case "concat" -> concat(castList(expr.get("args")), snapshot);
            case "coalesce" -> coalesce(castList(expr.get("args")), snapshot);
            case "min" -> min(castList(expr.get("args")), snapshot);
            case "max" -> max(castList(expr.get("args")), snapshot);
            case "typeof" -> typeOf(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "startsWith" -> startsWith(evaluateExpr(castMap(expr.get("left")), snapshot),
                                            evaluateExpr(castMap(expr.get("right")), snapshot));
            case "endsWith" -> endsWith(evaluateExpr(castMap(expr.get("left")), snapshot),
                                        evaluateExpr(castMap(expr.get("right")), snapshot));
            case "trim" -> trim(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "at" -> at(evaluateExpr(castMap(expr.get("array")), snapshot),
                            evaluateExpr(castMap(expr.get("index")), snapshot));
            case "first" -> first(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "last" -> last(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "slice" -> slice(expr, snapshot);
            case "append" -> append(expr, snapshot);
            case "filter" -> filter(expr, snapshot);
            case "map" -> map(expr, snapshot);
            case "find" -> find(expr, snapshot);
            case "some" -> some(expr, snapshot);
            case "every" -> every(expr, snapshot);
            case "includes" -> includes(expr, snapshot);
            case "reduce" -> reduce(expr, snapshot);
            case "object" -> object(expr, snapshot);
            case "merge" -> merge(expr, snapshot);
            default -> null;
        };
    }

    private boolean and(Map<String, Object> expr, SnapshotContext snapshot) {
        List<Map<String, Object>> args = castList(expr.get("args"));
        for (Map<String, Object> arg : args) {
            if (!truthy(evaluateExpr(arg, snapshot))) {
                return false;
            }
        }
        return true;
    }

    private boolean or(Map<String, Object> expr, SnapshotContext snapshot) {
        List<Map<String, Object>> args = castList(expr.get("args"));
        for (Map<String, Object> arg : args) {
            if (truthy(evaluateExpr(arg, snapshot))) {
                return true;
            }
        }
        return false;
    }

    private Object getByPath(SnapshotContext snapshot, String path) {
        if (path.startsWith("meta.")) {
            return getMapValue(snapshot.meta, path.substring(5));
        }
        if (path.startsWith("input.")) {
            return getMapValue(snapshot.input, path.substring(6));
        }
        if (path.startsWith("computed.")) {
            return getMapValue(snapshot.computed, path.substring(9));
        }
        return getMapValue(snapshot.data, path);
    }

    private Object getMapValue(Map<String, Object> map, String path) {
        if (path == null || path.isEmpty()) {
            return map;
        }
        String[] parts = path.split("\\.");
        Object current = map;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                return null;
            }
            current = currentMap.get(part);
        }
        return current;
    }

    private SnapshotContext apply(List<Map<String, Object>> patches, SnapshotContext snapshot) {
        Map<String, Object> data = new LinkedHashMap<>(snapshot.data);
        for (Map<String, Object> patch : patches) {
            String op = String.valueOf(patch.get("op"));
            String path = String.valueOf(patch.get("path"));
            if (!"set".equals(op)) {
                continue;
            }
            setByPath(data, path, patch.get("value"));
        }
        return new SnapshotContext(data, snapshot.computed, snapshot.meta, snapshot.input);
    }

    private void setByPath(Map<String, Object> data, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map<?, ?>)) {
                Map<String, Object> created = new LinkedHashMap<>();
                current.put(parts[i], created);
                current = created;
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> cast = (Map<String, Object>) next;
                current = cast;
            }
        }
        current.put(parts[parts.length - 1], value);
    }

    private boolean truthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        return true;
    }

    private boolean eq(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.equals(right);
    }

    private boolean gt(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            return ln.doubleValue() > rn.doubleValue();
        }
        return false;
    }

    private boolean gte(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            return ln.doubleValue() >= rn.doubleValue();
        }
        return false;
    }

    private boolean lt(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            return ln.doubleValue() < rn.doubleValue();
        }
        return false;
    }

    private boolean lte(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            return ln.doubleValue() <= rn.doubleValue();
        }
        return false;
    }

    private Object add(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            if (left instanceof Integer && right instanceof Integer) {
                return ln.intValue() + rn.intValue();
            }
            return ln.doubleValue() + rn.doubleValue();
        }
        return null;
    }

    private Object sub(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            if (left instanceof Integer && right instanceof Integer) {
                return ln.intValue() - rn.intValue();
            }
            return ln.doubleValue() - rn.doubleValue();
        }
        return null;
    }

    private Object mul(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            if (left instanceof Integer && right instanceof Integer) {
                return ln.intValue() * rn.intValue();
            }
            return ln.doubleValue() * rn.doubleValue();
        }
        return null;
    }

    private Object div(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            return ln.doubleValue() / rn.doubleValue();
        }
        return null;
    }

    private Object mod(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            return ln.doubleValue() % rn.doubleValue();
        }
        return null;
    }

    private Object abs(Object value) {
        if (value instanceof Number n) {
            return Math.abs(n.doubleValue());
        }
        return null;
    }

    private Object neg(Object value) {
        if (value instanceof Number n) {
            if (value instanceof Integer) {
                return -n.intValue();
            }
            return -n.doubleValue();
        }
        return null;
    }

    private Object round(Object value) {
        if (value instanceof Number n) {
            return Math.round(n.doubleValue());
        }
        return null;
    }

    private Object floor(Object value) {
        if (value instanceof Number n) {
            return Math.floor(n.doubleValue());
        }
        return null;
    }

    private Object ceil(Object value) {
        if (value instanceof Number n) {
            return Math.ceil(n.doubleValue());
        }
        return null;
    }

    private Object concat(List<Map<String, Object>> args, SnapshotContext snapshot) {
        StringBuilder builder = new StringBuilder();
        for (Map<String, Object> arg : args) {
            Object value = evaluateExpr(arg, snapshot);
            builder.append(value == null ? "null" : value.toString());
        }
        return builder.toString();
    }

    private Object coalesce(List<Map<String, Object>> args, SnapshotContext snapshot) {
        for (Map<String, Object> arg : args) {
            Object value = evaluateExpr(arg, snapshot);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Object min(List<Map<String, Object>> args, SnapshotContext snapshot) {
        Double min = null;
        for (Map<String, Object> arg : args) {
            Object value = evaluateExpr(arg, snapshot);
            if (value instanceof Number n) {
                double num = n.doubleValue();
                if (min == null || num < min) {
                    min = num;
                }
            }
        }
        return min;
    }

    private Object max(List<Map<String, Object>> args, SnapshotContext snapshot) {
        Double max = null;
        for (Map<String, Object> arg : args) {
            Object value = evaluateExpr(arg, snapshot);
            if (value instanceof Number n) {
                double num = n.doubleValue();
                if (max == null || num > max) {
                    max = num;
                }
            }
        }
        return max;
    }

    private String typeOf(Object value) {
        if (value == null) return "null";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Number) return "number";
        if (value instanceof String) return "string";
        if (value instanceof Map<?, ?>) return "object";
        if (value instanceof List<?>) return "array";
        return "unknown";
    }

    private boolean startsWith(Object left, Object right) {
        if (left instanceof String l && right != null) {
            return l.startsWith(String.valueOf(right));
        }
        return false;
    }

    private boolean endsWith(Object left, Object right) {
        if (left instanceof String l && right != null) {
            return l.endsWith(String.valueOf(right));
        }
        return false;
    }

    private Object trim(Object value) {
        if (value instanceof String s) {
            return s.trim();
        }
        return value;
    }

    private Object at(Object array, Object index) {
        if (array instanceof List<?> list && index instanceof Number n) {
            int idx = n.intValue();
            if (idx >= 0 && idx < list.size()) {
                return list.get(idx);
            }
        }
        return null;
    }

    private Object first(Object array) {
        if (array instanceof List<?> list && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    private Object last(Object array) {
        if (array instanceof List<?> list && !list.isEmpty()) {
            return list.get(list.size() - 1);
        }
        return null;
    }

    private Object slice(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return List.of();
        }
        int start = 0;
        int end = list.size();
        if (expr.containsKey("start")) {
            Object s = evaluateExpr(castMap(expr.get("start")), snapshot);
            if (s instanceof Number n) start = n.intValue();
        }
        if (expr.containsKey("end")) {
            Object e = evaluateExpr(castMap(expr.get("end")), snapshot);
            if (e instanceof Number n) end = n.intValue();
        }
        start = Math.max(0, start);
        end = Math.min(end, list.size());
        return list.subList(start, end);
    }

    private Object append(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        List<Object> result = new ArrayList<>();
        if (arrayValue instanceof List<?> list) {
            result.addAll(list);
        }
        List<Map<String, Object>> items = castList(expr.get("items"));
        for (Map<String, Object> item : items) {
            result.add(evaluateExpr(item, snapshot));
        }
        return result;
    }

    private Object filter(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return List.of();
        }
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withCollection(item, i, list);
            Object pred = evaluateExpr(castMap(expr.get("predicate")), ctx);
            if (truthy(pred)) {
                result.add(item);
            }
        }
        return result;
    }

    private Object map(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return List.of();
        }
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withCollection(item, i, list);
            result.add(evaluateExpr(castMap(expr.get("transform")), ctx));
        }
        return result;
    }

    private Object find(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withCollection(item, i, list);
            Object pred = evaluateExpr(castMap(expr.get("predicate")), ctx);
            if (truthy(pred)) {
                return item;
            }
        }
        return null;
    }

    private Object some(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withCollection(item, i, list);
            if (truthy(evaluateExpr(castMap(expr.get("predicate")), ctx))) {
                return true;
            }
        }
        return false;
    }

    private Object every(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withCollection(item, i, list);
            if (!truthy(evaluateExpr(castMap(expr.get("predicate")), ctx))) {
                return false;
            }
        }
        return true;
    }

    private Object includes(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        Object value = evaluateExpr(castMap(expr.get("value")), snapshot);
        if (arrayValue instanceof List<?> list) {
            return list.contains(value);
        }
        return false;
    }

    private Object reduce(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return null;
        }
        Object acc = expr.containsKey("initial")
            ? evaluateExpr(castMap(expr.get("initial")), snapshot)
            : (list.isEmpty() ? null : list.get(0));
        int start = expr.containsKey("initial") ? 0 : 1;
        for (int i = start; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withReducer(acc, item, i, list);
            acc = evaluateExpr(castMap(expr.get("reducer")), ctx);
        }
        return acc;
    }

    private Object object(Map<String, Object> expr, SnapshotContext snapshot) {
        Map<String, Object> fields = castMap(expr.get("fields"));
        Map<String, Object> out = new LinkedHashMap<>();
        if (fields != null) {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                out.put(entry.getKey(), evaluateExpr(castMap(entry.getValue()), snapshot));
            }
        }
        return out;
    }

    private Object merge(Map<String, Object> expr, SnapshotContext snapshot) {
        List<Map<String, Object>> objects = castList(expr.get("objects"));
        Map<String, Object> merged = new LinkedHashMap<>();
        for (Map<String, Object> object : objects) {
            Object value = evaluateExpr(object, snapshot);
            if (value instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    merged.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        }
        return merged;
    }
    private int len(Object value) {
        if (value instanceof List<?> list) {
            return list.size();
        }
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        if (value instanceof String s) {
            return s.length();
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object obj) {
        return (Map<String, Object>) obj;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object obj) {
        return obj == null ? List.of() : (List<Map<String, Object>>) obj;
    }

    public record SnapshotContext(Map<String, Object> data,
                                  Map<String, Object> computed,
                                  Map<String, Object> meta,
                                  Map<String, Object> input) {
        public SnapshotContext withCollection(Object item, int index, List<?> array) {
            Map<String, Object> nextData = new LinkedHashMap<>(data);
            nextData.put("$item", item);
            nextData.put("$index", index);
            nextData.put("$array", array);
            return new SnapshotContext(nextData, computed, meta, input);
        }

        public SnapshotContext withReducer(Object acc, Object item, int index, List<?> array) {
            Map<String, Object> nextData = new LinkedHashMap<>(data);
            nextData.put("$acc", acc);
            nextData.put("$item", item);
            nextData.put("$index", index);
            nextData.put("$array", array);
            return new SnapshotContext(nextData, computed, meta, input);
        }
    }

    public record EvaluationResult(List<Map<String, Object>> patches,
                                   List<Map<String, Object>> skipped,
                                   SnapshotContext finalSnapshot) {}
}
