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
        SnapshotContext workingSnapshot = snapshot;

        for (int i = 0; i < patches.size(); i++) {
            Map<String, Object> patch = patches.get(i);
            if (patch.containsKey("condition")) {
                Object cond = evaluateExpr(castMap(patch.get("condition")), workingSnapshot);
                if (!(cond instanceof Boolean) || !((Boolean) cond)) {
                    String reason = cond == null ? "null" : (cond instanceof Boolean ? "false" : "non-boolean");
                    Map<String, Object> skipInfo = new LinkedHashMap<>();
                    skipInfo.put("index", i);
                    skipInfo.put("path", patch.get("path"));
                    skipInfo.put("reason", reason);
                    skipped.add(skipInfo);
                    continue;
                }
            }
            String op = String.valueOf(patch.get("op"));
            String path = String.valueOf(patch.get("path"));
            Object concreteValue = null;
            if (patch.containsKey("value")) {
                concreteValue = evaluateExpr(castMap(patch.get("value")), workingSnapshot);
            }
            Map<String, Object> out = buildConcretePatch(op, path, concreteValue);
            if (out != null) {
                outPatches.add(out);
                workingSnapshot = applyToWorkingSnapshot(out, workingSnapshot);
            }
        }

        return new EvaluationResult(outPatches, skipped, workingSnapshot);
    }

    public Object evaluateExpr(Map<String, Object> expr, SnapshotContext snapshot) {
        try {
            return evaluateNode(expr, snapshot);
        } catch (Exception e) {
            return null;
        }
    }

    private Object evaluateNode(Map<String, Object> expr, SnapshotContext snapshot) {
        String kind = String.valueOf(expr.get("kind"));
        return switch (kind) {
            case "lit" -> expr.get("value");
            case "get" -> getByPath(snapshot, String.valueOf(expr.get("path")));
            case "eq" -> deepEqual(evaluateExpr(castMap(expr.get("left")), snapshot),
                                   evaluateExpr(castMap(expr.get("right")), snapshot));
            case "neq" -> {
                Object result = deepEqual(evaluateExpr(castMap(expr.get("left")), snapshot),
                                          evaluateExpr(castMap(expr.get("right")), snapshot));
                yield result instanceof Boolean b ? !b : null;
            }
            case "gt" -> compareNumber(expr, snapshot, (l, r) -> l > r);
            case "gte" -> compareNumber(expr, snapshot, (l, r) -> l >= r);
            case "lt" -> compareNumber(expr, snapshot, (l, r) -> l < r);
            case "lte" -> compareNumber(expr, snapshot, (l, r) -> l <= r);
            case "add" -> arithmetic(expr, snapshot, (l, r) -> l + r);
            case "sub" -> arithmetic(expr, snapshot, (l, r) -> l - r);
            case "mul" -> arithmetic(expr, snapshot, (l, r) -> l * r);
            case "div" -> div(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "mod" -> mod(evaluateExpr(castMap(expr.get("left")), snapshot),
                              evaluateExpr(castMap(expr.get("right")), snapshot));
            case "and" -> and(expr, snapshot);
            case "or" -> or(expr, snapshot);
            case "len" -> len(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "strLen" -> strLen(evaluateExpr(castMap(expr.get("str")), snapshot));
            case "toString" -> toStringValue(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "not" -> not(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "isNull" -> evaluateExpr(castMap(expr.get("arg")), snapshot) == null;
            case "abs" -> abs(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "neg" -> neg(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "round" -> round(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "floor" -> floor(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "ceil" -> ceil(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "pow" -> pow(evaluateExpr(castMap(expr.get("base")), snapshot),
                              evaluateExpr(castMap(expr.get("exponent")), snapshot));
            case "sqrt" -> sqrt(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "if" -> ifExpr(expr, snapshot);
            case "concat" -> concat(castList(expr.get("args")), snapshot);
            case "coalesce" -> coalesce(castList(expr.get("args")), snapshot);
            case "min" -> min(castList(expr.get("args")), snapshot);
            case "max" -> max(castList(expr.get("args")), snapshot);
            case "sumArray" -> sumArray(evaluateExpr(castMap(expr.get("array")), snapshot));
            case "minArray" -> minArray(evaluateExpr(castMap(expr.get("array")), snapshot));
            case "maxArray" -> maxArray(evaluateExpr(castMap(expr.get("array")), snapshot));
            case "typeof" -> typeOf(evaluateExpr(castMap(expr.get("arg")), snapshot));
            case "startsWith" -> startsWith(evaluateExpr(castMap(expr.get("left")), snapshot),
                                            evaluateExpr(castMap(expr.get("right")), snapshot));
            case "endsWith" -> endsWith(evaluateExpr(castMap(expr.get("left")), snapshot),
                                        evaluateExpr(castMap(expr.get("right")), snapshot));
            case "trim" -> trim(evaluateExpr(castMap(expr.get("str")), snapshot));
            case "toLowerCase" -> toLowerCase(evaluateExpr(castMap(expr.get("str")), snapshot));
            case "toUpperCase" -> toUpperCase(evaluateExpr(castMap(expr.get("str")), snapshot));
            case "at" -> at(evaluateExpr(castMap(expr.get("array")), snapshot),
                            evaluateExpr(castMap(expr.get("index")), snapshot));
            case "first" -> first(evaluateExpr(castMap(expr.get("array")), snapshot));
            case "last" -> last(evaluateExpr(castMap(expr.get("array")), snapshot));
            case "slice" -> slice(expr, snapshot);
            case "append" -> append(expr, snapshot);
            case "filter" -> filter(expr, snapshot);
            case "map" -> map(expr, snapshot);
            case "find" -> find(expr, snapshot);
            case "some" -> some(expr, snapshot);
            case "every" -> every(expr, snapshot);
            case "includes" -> includes(expr, snapshot);
            case "object" -> object(expr, snapshot);
            case "merge" -> merge(expr, snapshot);
            default -> null;
        };
    }

    private Object and(Map<String, Object> expr, SnapshotContext snapshot) {
        List<Map<String, Object>> args = castList(expr.get("args"));
        for (Map<String, Object> arg : args) {
            Object result = evaluateExpr(arg, snapshot);
            if (Boolean.TRUE.equals(result)) {
                continue;
            }
            if (Boolean.FALSE.equals(result)) {
                return false;
            }
            return null;
        }
        return true;
    }

    private Object or(Map<String, Object> expr, SnapshotContext snapshot) {
        List<Map<String, Object>> args = castList(expr.get("args"));
        for (Map<String, Object> arg : args) {
            Object result = evaluateExpr(arg, snapshot);
            if (Boolean.TRUE.equals(result)) {
                return true;
            }
            if (Boolean.FALSE.equals(result)) {
                continue;
            }
            return null;
        }
        return false;
    }

    private Object getByPath(SnapshotContext snapshot, String path) {
        if ("meta".equals(path)) {
            return snapshot.meta;
        }
        if (path.startsWith("meta.")) {
            return getMapValue(snapshot.meta, path.substring(5));
        }
        if ("input".equals(path)) {
            return snapshot.input;
        }
        if (path.startsWith("input.")) {
            return getMapValue(snapshot.input, path.substring(6));
        }
        if ("computed".equals(path)) {
            return snapshot.computed;
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

    private SnapshotContext applyToWorkingSnapshot(Map<String, Object> patch, SnapshotContext snapshot) {
        Map<String, Object> data = deepCopyMap(snapshot.data);
        String op = String.valueOf(patch.get("op"));
        String path = String.valueOf(patch.get("path"));
        if ("unset".equals(op)) {
            unsetByPath(data, path);
            return new SnapshotContext(data, snapshot.computed, snapshot.meta, snapshot.input);
        }
        setByPath(data, path, patch.get("value"));
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

    private void unsetByPath(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map<?, ?>)) {
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) next;
            current = cast;
        }
        current.remove(parts[parts.length - 1]);
    }

    private Object div(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            double divisor = rn.doubleValue();
            if (divisor == 0) {
                return null;
            }
            double result = ln.doubleValue() / divisor;
            if (!Double.isFinite(result)) {
                return null;
            }
            return coerceWholeNumber(left, right, result);
        }
        return null;
    }

    private Object mod(Object left, Object right) {
        if (left instanceof Number ln && right instanceof Number rn) {
            double divisor = rn.doubleValue();
            if (divisor == 0) {
                return null;
            }
            double result = ln.doubleValue() % divisor;
            return coerceWholeNumber(left, right, result);
        }
        return null;
    }

    private Object abs(Object value) {
        if (value instanceof Number n) {
            double result = Math.abs(n.doubleValue());
            return Double.isFinite(result) ? result : null;
        }
        return null;
    }

    private Object neg(Object value) {
        if (value instanceof Number n) {
            double result = -n.doubleValue();
            return Double.isFinite(result) ? result : null;
        }
        return null;
    }

    private Object round(Object value) {
        if (value instanceof Number n) {
            return (double) Math.round(n.doubleValue());
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
            if (!(value instanceof String)) {
                return null;
            }
            builder.append(value);
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
            if (!(value instanceof Number n)) {
                return null;
            }
            double num = n.doubleValue();
            if (min == null || num < min) {
                min = num;
            }
        }
        return min;
    }

    private Object max(List<Map<String, Object>> args, SnapshotContext snapshot) {
        Double max = null;
        for (Map<String, Object> arg : args) {
            Object value = evaluateExpr(arg, snapshot);
            if (!(value instanceof Number n)) {
                return null;
            }
            double num = n.doubleValue();
            if (max == null || num > max) {
                max = num;
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
        return null;
    }

    private Object toLowerCase(Object value) {
        if (value instanceof String s) {
            return s.toLowerCase();
        }
        return null;
    }

    private Object toUpperCase(Object value) {
        if (value instanceof String s) {
            return s.toUpperCase();
        }
        return null;
    }

    private Object pow(Object base, Object exponent) {
        if (base instanceof Number bn && exponent instanceof Number en) {
            return Math.pow(bn.doubleValue(), en.doubleValue());
        }
        return null;
    }

    private Object sqrt(Object value) {
        if (value instanceof Number n) {
            double result = Math.sqrt(n.doubleValue());
            return Double.isFinite(result) ? result : null;
        }
        return null;
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
            return null;
        }
        Object s = evaluateExpr(castMap(expr.get("start")), snapshot);
        if (!(s instanceof Number n)) {
            return null;
        }
        int start = n.intValue();
        if (!expr.containsKey("end")) {
            return list.subList(start, list.size());
        }
        Object e = evaluateExpr(castMap(expr.get("end")), snapshot);
        if (!(e instanceof Number en)) {
            return null;
        }
        int end = en.intValue();
        return list.subList(start, end);
    }

    private Object append(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        List<Object> result = new ArrayList<>();
        if (!(arrayValue instanceof List<?> list)) {
            return null;
        }
        result.addAll(list);
        List<Map<String, Object>> items = castList(expr.get("items"));
        for (Map<String, Object> item : items) {
            result.add(evaluateExpr(item, snapshot));
        }
        return result;
    }

    private Object filter(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return null;
        }
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withCollection(item, i, list);
            Object pred = evaluateExpr(castMap(expr.get("predicate")), ctx);
            if (Boolean.TRUE.equals(pred)) {
                result.add(item);
                continue;
            }
            if (Boolean.FALSE.equals(pred)) {
                continue;
            }
            return null;
        }
        return result;
    }

    private Object map(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return null;
        }
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withCollection(item, i, list);
            result.add(evaluateExpr(castMap(expr.get("mapper")), ctx));
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
            if (Boolean.TRUE.equals(pred)) {
                return item;
            }
            if (Boolean.FALSE.equals(pred)) {
                continue;
            }
            return null;
        }
        return null;
    }

    private Object some(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withCollection(item, i, list);
            Object result = evaluateExpr(castMap(expr.get("predicate")), ctx);
            if (Boolean.TRUE.equals(result)) {
                return true;
            }
            if (Boolean.FALSE.equals(result)) {
                continue;
            }
            return null;
        }
        return false;
    }

    private Object every(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        if (!(arrayValue instanceof List<?> list)) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            SnapshotContext ctx = snapshot.withCollection(item, i, list);
            Object result = evaluateExpr(castMap(expr.get("predicate")), ctx);
            if (Boolean.FALSE.equals(result)) {
                return false;
            }
            if (!Boolean.TRUE.equals(result)) {
                return null;
            }
        }
        return true;
    }

    private Object includes(Map<String, Object> expr, SnapshotContext snapshot) {
        Object arrayValue = evaluateExpr(castMap(expr.get("array")), snapshot);
        Object value = evaluateExpr(castMap(expr.get("item")), snapshot);
        if (arrayValue instanceof List<?> list) {
            for (Object item : list) {
                if (Boolean.TRUE.equals(deepEqual(item, value))) {
                    return true;
                }
            }
            return false;
        }
        return null;
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
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                merged.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return merged;
    }

    private double toNumber(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof Boolean b) return b ? 1.0 : 0.0;
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private Object len(Object value) {
        if (value instanceof List<?> list) {
            return list.size();
        }
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        if (value instanceof String s) {
            return s.length();
        }
        return null;
    }

    private Object strLen(Object value) {
        if (value instanceof String s) {
            return s.length();
        }
        return null;
    }

    private String toStringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Object sumArray(Object value) {
        if (!(value instanceof List<?> list)) {
            return null;
        }
        double sum = 0.0;
        for (Object item : list) {
            if (!(item instanceof Number n)) {
                return null;
            }
            sum += n.doubleValue();
        }
        return sum;
    }

    private Object minArray(Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return null;
        }
        double min = Double.POSITIVE_INFINITY;
        for (Object item : list) {
            if (!(item instanceof Number n)) {
                return null;
            }
            min = Math.min(min, n.doubleValue());
        }
        return min == Double.POSITIVE_INFINITY ? null : min;
    }

    private Object maxArray(Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return null;
        }
        double max = Double.NEGATIVE_INFINITY;
        for (Object item : list) {
            if (!(item instanceof Number n)) {
                return null;
            }
            max = Math.max(max, n.doubleValue());
        }
        return max == Double.NEGATIVE_INFINITY ? null : max;
    }

    private Object not(Object value) {
        if (value instanceof Boolean b) {
            return !b;
        }
        return null;
    }

    private Object ifExpr(Map<String, Object> expr, SnapshotContext snapshot) {
        Object cond = evaluateExpr(castMap(expr.get("cond")), snapshot);
        if (Boolean.TRUE.equals(cond)) {
            return evaluateExpr(castMap(expr.get("then")), snapshot);
        }
        if (Boolean.FALSE.equals(cond)) {
            return evaluateExpr(castMap(expr.get("else")), snapshot);
        }
        return null;
    }

    private Object compareNumber(Map<String, Object> expr, SnapshotContext snapshot, java.util.function.BiFunction<Double, Double, Boolean> op) {
        Object left = evaluateExpr(castMap(expr.get("left")), snapshot);
        Object right = evaluateExpr(castMap(expr.get("right")), snapshot);
        if (!(left instanceof Number ln) || !(right instanceof Number rn)) {
            return null;
        }
        return op.apply(ln.doubleValue(), rn.doubleValue());
    }

    private Object arithmetic(Map<String, Object> expr, SnapshotContext snapshot, java.util.function.BiFunction<Double, Double, Double> op) {
        Object left = evaluateExpr(castMap(expr.get("left")), snapshot);
        Object right = evaluateExpr(castMap(expr.get("right")), snapshot);
        if (!(left instanceof Number ln) || !(right instanceof Number rn)) {
            return null;
        }
        double result = op.apply(ln.doubleValue(), rn.doubleValue());
        if (!Double.isFinite(result)) {
            return null;
        }
        return coerceWholeNumber(left, right, result);
    }

    private Object coerceWholeNumber(Object left, Object right, double result) {
        if (left instanceof Integer && right instanceof Integer && result == Math.floor(result)) {
            return (int) result;
        }
        return result;
    }

    private Object deepEqual(Object left, Object right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return left == right;
        }
        if (left instanceof List<?> lList && right instanceof List<?> rList) {
            if (lList.size() != rList.size()) {
                return false;
            }
            for (int i = 0; i < lList.size(); i++) {
                Object eq = deepEqual(lList.get(i), rList.get(i));
                if (!(eq instanceof Boolean b) || !b) {
                    return false;
                }
            }
            return true;
        }
        if (left instanceof Map<?, ?> lMap && right instanceof Map<?, ?> rMap) {
            if (lMap.size() != rMap.size()) {
                return false;
            }
            for (Map.Entry<?, ?> entry : lMap.entrySet()) {
                Object key = entry.getKey();
                if (!rMap.containsKey(key)) {
                    return false;
                }
                Object eq = deepEqual(entry.getValue(), rMap.get(key));
                if (!(eq instanceof Boolean b) || !b) {
                    return false;
                }
            }
            return true;
        }
        return left.equals(right);
    }

    private Map<String, Object> buildConcretePatch(String op, String path, Object value) {
        Map<String, Object> patch = new LinkedHashMap<>();
        if ("set".equals(op)) {
            patch.put("op", "set");
            patch.put("path", path);
            patch.put("value", value);
            return patch;
        }
        if ("unset".equals(op)) {
            patch.put("op", "unset");
            patch.put("path", path);
            return patch;
        }
        if ("merge".equals(op)) {
            if (value instanceof Map<?, ?> mapValue && !(value instanceof List<?>)) {
                patch.put("op", "merge");
                patch.put("path", path);
                patch.put("value", mapValue);
                return patch;
            }
            patch.put("op", "set");
            patch.put("path", path);
            patch.put("value", null);
            return patch;
        }
        return null;
    }

    private Map<String, Object> deepCopyMap(Map<String, Object> input) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            copy.put(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return copy;
    }

    private Object deepCopyValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put(String.valueOf(entry.getKey()), deepCopyValue(entry.getValue()));
            }
            return copy;
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            for (Object item : list) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        return value;
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
