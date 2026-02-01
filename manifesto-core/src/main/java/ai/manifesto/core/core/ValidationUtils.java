package ai.manifesto.core.core;

import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.expr.arithmetic.Add;
import ai.manifesto.core.expr.arithmetic.Abs;
import ai.manifesto.core.expr.arithmetic.Div;
import ai.manifesto.core.expr.arithmetic.Ceil;
import ai.manifesto.core.expr.arithmetic.Floor;
import ai.manifesto.core.expr.arithmetic.Max;
import ai.manifesto.core.expr.arithmetic.Min;
import ai.manifesto.core.expr.arithmetic.Mod;
import ai.manifesto.core.expr.arithmetic.Mul;
import ai.manifesto.core.expr.arithmetic.Neg;
import ai.manifesto.core.expr.arithmetic.Round;
import ai.manifesto.core.expr.arithmetic.Sub;
import ai.manifesto.core.expr.collection.Append;
import ai.manifesto.core.expr.collection.At;
import ai.manifesto.core.expr.collection.Every;
import ai.manifesto.core.expr.collection.Filter;
import ai.manifesto.core.expr.collection.Find;
import ai.manifesto.core.expr.collection.First;
import ai.manifesto.core.expr.collection.Includes;
import ai.manifesto.core.expr.collection.Last;
import ai.manifesto.core.expr.collection.Len;
import ai.manifesto.core.expr.collection.Reduce;
import ai.manifesto.core.expr.collection.Some;
import ai.manifesto.core.expr.collection.Slice;
import ai.manifesto.core.expr.comparison.Eq;
import ai.manifesto.core.expr.comparison.Gt;
import ai.manifesto.core.expr.comparison.Gte;
import ai.manifesto.core.expr.comparison.Lt;
import ai.manifesto.core.expr.comparison.Lte;
import ai.manifesto.core.expr.comparison.Neq;
import ai.manifesto.core.expr.conditional.If;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.expr.logical.And;
import ai.manifesto.core.expr.logical.Not;
import ai.manifesto.core.expr.logical.Or;
import ai.manifesto.core.expr.object.Entries;
import ai.manifesto.core.expr.object.Keys;
import ai.manifesto.core.expr.object.Merge;
import ai.manifesto.core.expr.object.ObjectExpr;
import ai.manifesto.core.expr.object.Values;
import ai.manifesto.core.expr.string.Concat;
import ai.manifesto.core.expr.string.EndsWith;
import ai.manifesto.core.expr.string.Split;
import ai.manifesto.core.expr.string.StartsWith;
import ai.manifesto.core.expr.string.Substring;
import ai.manifesto.core.expr.string.Trim;
import ai.manifesto.core.expr.type.Coalesce;
import ai.manifesto.core.expr.type.IsNull;
import ai.manifesto.core.expr.type.Typeof;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * ValidationUtils - 스키마 검증 보조 유틸리티
 */
public final class ValidationUtils {
    private static final Pattern SEMVER_REGEX = Pattern.compile(
        "^\\d+\\.\\d+\\.\\d+(?:-[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$"
    );
    private static final Pattern URI_SCHEME_REGEX = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:");
    private static final Pattern UUID_REGEX = Pattern.compile(
        "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
        Pattern.CASE_INSENSITIVE
    );

    private ValidationUtils() {
        // 유틸 클래스
    }

    public static boolean isValidSchemaId(String id) {
        if (id == null) {
            return false;
        }
        return URI_SCHEME_REGEX.matcher(id).find() || UUID_REGEX.matcher(id).matches();
    }

    public static boolean isValidSemver(String version) {
        if (version == null) {
            return false;
        }
        return SEMVER_REGEX.matcher(version).matches();
    }

    public static List<String> collectGetPathsFromExpr(ExprNode expr) {
        List<String> paths = new ArrayList<>();
        collectGetPathsFromExpr(expr, paths);
        return paths;
    }

    private static void collectGetPathsFromExpr(ExprNode expr, List<String> paths) {
        if (expr == null) {
            return;
        }
        if (expr instanceof Lit) {
            return;
        }
        if (expr instanceof Get get) {
            paths.add(get.path());
            return;
        }

        if (expr instanceof Eq eq) {
            collectGetPathsFromExpr(eq.left(), paths);
            collectGetPathsFromExpr(eq.right(), paths);
            return;
        }
        if (expr instanceof Neq neq) {
            collectGetPathsFromExpr(neq.left(), paths);
            collectGetPathsFromExpr(neq.right(), paths);
            return;
        }
        if (expr instanceof Gt gt) {
            collectGetPathsFromExpr(gt.left(), paths);
            collectGetPathsFromExpr(gt.right(), paths);
            return;
        }
        if (expr instanceof Gte gte) {
            collectGetPathsFromExpr(gte.left(), paths);
            collectGetPathsFromExpr(gte.right(), paths);
            return;
        }
        if (expr instanceof Lt lt) {
            collectGetPathsFromExpr(lt.left(), paths);
            collectGetPathsFromExpr(lt.right(), paths);
            return;
        }
        if (expr instanceof Lte lte) {
            collectGetPathsFromExpr(lte.left(), paths);
            collectGetPathsFromExpr(lte.right(), paths);
            return;
        }

        if (expr instanceof Add add) {
            collectGetPathsFromExpr(add.left(), paths);
            collectGetPathsFromExpr(add.right(), paths);
            return;
        }
        if (expr instanceof Sub sub) {
            collectGetPathsFromExpr(sub.left(), paths);
            collectGetPathsFromExpr(sub.right(), paths);
            return;
        }
        if (expr instanceof Mul mul) {
            collectGetPathsFromExpr(mul.left(), paths);
            collectGetPathsFromExpr(mul.right(), paths);
            return;
        }
        if (expr instanceof Div div) {
            collectGetPathsFromExpr(div.left(), paths);
            collectGetPathsFromExpr(div.right(), paths);
            return;
        }
        if (expr instanceof Mod mod) {
            collectGetPathsFromExpr(mod.left(), paths);
            collectGetPathsFromExpr(mod.right(), paths);
            return;
        }
        if (expr instanceof Min min) {
            for (ExprNode arg : min.args()) {
                collectGetPathsFromExpr(arg, paths);
            }
            return;
        }
        if (expr instanceof Max max) {
            for (ExprNode arg : max.args()) {
                collectGetPathsFromExpr(arg, paths);
            }
            return;
        }
        if (expr instanceof Abs abs) {
            collectGetPathsFromExpr(abs.arg(), paths);
            return;
        }
        if (expr instanceof Neg neg) {
            collectGetPathsFromExpr(neg.arg(), paths);
            return;
        }
        if (expr instanceof Round round) {
            collectGetPathsFromExpr(round.arg(), paths);
            return;
        }
        if (expr instanceof Floor floor) {
            collectGetPathsFromExpr(floor.arg(), paths);
            return;
        }
        if (expr instanceof Ceil ceil) {
            collectGetPathsFromExpr(ceil.arg(), paths);
            return;
        }

        if (expr instanceof And and) {
            for (ExprNode arg : and.args()) {
                collectGetPathsFromExpr(arg, paths);
            }
            return;
        }
        if (expr instanceof Or or) {
            for (ExprNode arg : or.args()) {
                collectGetPathsFromExpr(arg, paths);
            }
            return;
        }
        if (expr instanceof Concat concat) {
            for (ExprNode arg : concat.args()) {
                collectGetPathsFromExpr(arg, paths);
            }
            return;
        }
        if (expr instanceof Coalesce coalesce) {
            for (ExprNode arg : coalesce.args()) {
                collectGetPathsFromExpr(arg, paths);
            }
            return;
        }

        if (expr instanceof Not not) {
            collectGetPathsFromExpr(not.arg(), paths);
            return;
        }
        if (expr instanceof Typeof typeofExpr) {
            collectGetPathsFromExpr(typeofExpr.arg(), paths);
            return;
        }
        if (expr instanceof IsNull isNull) {
            collectGetPathsFromExpr(isNull.arg(), paths);
            return;
        }
        if (expr instanceof Len len) {
            collectGetPathsFromExpr(len.arg(), paths);
            return;
        }

        if (expr instanceof First first) {
            collectGetPathsFromExpr(first.array(), paths);
            return;
        }
        if (expr instanceof Last last) {
            collectGetPathsFromExpr(last.array(), paths);
            return;
        }
        if (expr instanceof At at) {
            collectGetPathsFromExpr(at.array(), paths);
            collectGetPathsFromExpr(at.index(), paths);
            return;
        }
        if (expr instanceof Slice slice) {
            collectGetPathsFromExpr(slice.array(), paths);
            collectGetPathsFromExpr(slice.start(), paths);
            collectGetPathsFromExpr(slice.end(), paths);
            return;
        }
        if (expr instanceof Includes includes) {
            collectGetPathsFromExpr(includes.array(), paths);
            collectGetPathsFromExpr(includes.item(), paths);
            return;
        }
        if (expr instanceof Filter filter) {
            collectGetPathsFromExpr(filter.array(), paths);
            collectGetPathsFromExpr(filter.predicate(), paths);
            return;
        }
        if (expr instanceof ai.manifesto.core.expr.collection.Map map) {
            collectGetPathsFromExpr(map.array(), paths);
            collectGetPathsFromExpr(map.mapper(), paths);
            return;
        }
        if (expr instanceof Find find) {
            collectGetPathsFromExpr(find.array(), paths);
            collectGetPathsFromExpr(find.predicate(), paths);
            return;
        }
        if (expr instanceof Every every) {
            collectGetPathsFromExpr(every.array(), paths);
            collectGetPathsFromExpr(every.predicate(), paths);
            return;
        }
        if (expr instanceof Some some) {
            collectGetPathsFromExpr(some.array(), paths);
            collectGetPathsFromExpr(some.predicate(), paths);
            return;
        }
        if (expr instanceof Append append) {
            collectGetPathsFromExpr(append.array(), paths);
            for (ExprNode item : append.items()) {
                collectGetPathsFromExpr(item, paths);
            }
            return;
        }
        if (expr instanceof Reduce reduce) {
            collectGetPathsFromExpr(reduce.array(), paths);
            collectGetPathsFromExpr(reduce.reducer(), paths);
            collectGetPathsFromExpr(reduce.initial(), paths);
            return;
        }

        if (expr instanceof ObjectExpr objectExpr) {
            for (ExprNode value : objectExpr.fields().values()) {
                collectGetPathsFromExpr(value, paths);
            }
            return;
        }
        if (expr instanceof Keys keys) {
            collectGetPathsFromExpr(keys.obj(), paths);
            return;
        }
        if (expr instanceof Values values) {
            collectGetPathsFromExpr(values.obj(), paths);
            return;
        }
        if (expr instanceof Entries entries) {
            collectGetPathsFromExpr(entries.obj(), paths);
            return;
        }
        if (expr instanceof Merge merge) {
            for (ExprNode obj : merge.objects()) {
                collectGetPathsFromExpr(obj, paths);
            }
            return;
        }

        if (expr instanceof If ifExpr) {
            collectGetPathsFromExpr(ifExpr.cond(), paths);
            collectGetPathsFromExpr(ifExpr.thenExpr(), paths);
            collectGetPathsFromExpr(ifExpr.elseExpr(), paths);
            return;
        }
        if (expr instanceof Substring substring) {
            collectGetPathsFromExpr(substring.str(), paths);
            collectGetPathsFromExpr(substring.start(), paths);
            collectGetPathsFromExpr(substring.end(), paths);
            return;
        }
        if (expr instanceof Trim trim) {
            collectGetPathsFromExpr(trim.str(), paths);
            return;
        }
        if (expr instanceof StartsWith startsWith) {
            collectGetPathsFromExpr(startsWith.str(), paths);
            collectGetPathsFromExpr(startsWith.prefix(), paths);
            return;
        }
        if (expr instanceof EndsWith endsWith) {
            collectGetPathsFromExpr(endsWith.str(), paths);
            collectGetPathsFromExpr(endsWith.suffix(), paths);
            return;
        }
        if (expr instanceof Split split) {
            collectGetPathsFromExpr(split.str(), paths);
            collectGetPathsFromExpr(split.delimiter(), paths);
        }
    }

    public static List<String> collectGetPathsFromFlow(FlowNode flow) {
        List<String> paths = new ArrayList<>();
        collectGetPathsFromFlow(flow, paths);
        return paths;
    }

    private static void collectGetPathsFromFlow(FlowNode flow, List<String> paths) {
        if (flow == null) {
            return;
        }
        if (flow instanceof FlowNode.Seq seq) {
            for (FlowNode step : seq.getSteps()) {
                collectGetPathsFromFlow(step, paths);
            }
            return;
        }
        if (flow instanceof FlowNode.If ifFlow) {
            collectGetPathsFromExpr(ifFlow.getCond(), paths);
            collectGetPathsFromFlow(ifFlow.getThenBranch(), paths);
            if (ifFlow.getElseBranch() != null) {
                collectGetPathsFromFlow(ifFlow.getElseBranch(), paths);
            }
            return;
        }
        if (flow instanceof FlowNode.Patch patch) {
            if (patch.getOp() != ai.manifesto.core.flow.PatchOp.UNSET && patch.getValue() != null) {
                collectGetPathsFromExpr(patch.getValue(), paths);
            }
            return;
        }
        if (flow instanceof FlowNode.Effect effect) {
            for (ExprNode expr : effect.getParams().values()) {
                collectGetPathsFromExpr(expr, paths);
            }
            return;
        }
        if (flow instanceof FlowNode.Fail fail) {
            if (fail.getMessage() != null) {
                collectGetPathsFromExpr(fail.getMessage(), paths);
            }
        }
    }

    public static boolean pathExistsInStateSpec(Map<String, FieldSpec> stateFields, String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        String normalized = normalizeDataPath(path);
        if (normalized.isEmpty()) {
            return true;
        }
        String[] segments = normalized.split("\\.");
        if (segments.length > 0 && "$host".equals(segments[0])) {
            return true;
        }
        return stateFields.containsKey(segments[0]);
    }

    public static boolean pathExistsInComputedSpec(Map<String, ComputedFieldDef> computedFields, String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        String normalized = normalizeComputedPath(path);
        if (normalized.isEmpty()) {
            return true;
        }
        return computedFields.containsKey(normalized);
    }

    public static boolean pathExistsInFieldSpec(Map<String, FieldSpec> inputFields, String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        String normalized = normalizeInputPath(path);
        if (normalized.isEmpty()) {
            return true;
        }
        String[] segments = normalized.split("\\.");
        return inputFields.containsKey(segments[0]);
    }

    public static String normalizeComputedPath(String path) {
        if (path == null) {
            return "";
        }
        if (path.equals("computed")) {
            return "";
        }
        if (path.startsWith("computed.")) {
            return path.substring(9);
        }
        return path;
    }

    public static String normalizeDataPath(String path) {
        if (path == null) {
            return "";
        }
        if (path.equals("data")) {
            return "";
        }
        if (path.startsWith("data.")) {
            return path.substring(5);
        }
        return path;
    }

    public static String normalizeInputPath(String path) {
        if (path == null) {
            return "";
        }
        if (path.equals("input")) {
            return "";
        }
        if (path.startsWith("input.")) {
            return path.substring(6);
        }
        return path;
    }

    public static String computeSchemaHash(DomainSchema schema) {
        Map<String, Object> schemaMap = toSchemaMap(schema);
        String canonical = toCanonicalJson(schemaMap);
        return sha256Hex(canonical);
    }

    private static Map<String, Object> toSchemaMap(DomainSchema schema) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", schema.getId());
        root.put("version", schema.getVersion());
        root.put("state", Map.of("fields", toFieldMap(schema.getDataFields())));
        root.put("computed", Map.of("fields", toComputedMap(schema.getComputedFields())));
        root.put("actions", toActionMap(schema.getActions()));
        return root;
    }

    private static Map<String, Object> toActionMap(Map<String, ActionSpec> actions) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, ActionSpec> entry : actions.entrySet()) {
            result.put(entry.getKey(), toActionSpecMap(entry.getValue()));
        }
        return result;
    }

    private static Map<String, Object> toActionSpecMap(ActionSpec action) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!action.getInputFields().isEmpty()) {
            Map<String, Object> inputSpec = new LinkedHashMap<>();
            inputSpec.put("type", "object");
            inputSpec.put("required", true);
            inputSpec.put("fields", toFieldMap(action.getInputFields()));
            result.put("input", inputSpec);
        }
        if (action.getAvailable() != null) {
            result.put("available", toExprMap(action.getAvailable()));
        }
        result.put("flow", toFlowMap(action.getFlow()));
        return result;
    }

    private static Map<String, Object> toComputedMap(Map<String, ComputedFieldDef> computedFields) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, ComputedFieldDef> entry : computedFields.entrySet()) {
            result.put(entry.getKey(), toComputedFieldMap(entry.getValue()));
        }
        return result;
    }

    private static Map<String, Object> toComputedFieldMap(ComputedFieldDef field) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> deps = new ArrayList<>(field.getDependencies());
        deps.sort(String::compareTo);
        result.put("deps", deps);
        result.put("expr", toExprMap(field.getExpression()));
        return result;
    }

    private static Map<String, Object> toFieldMap(Map<String, FieldSpec> fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, FieldSpec> entry : fields.entrySet()) {
            result.put(entry.getKey(), toFieldSpecMap(entry.getValue()));
        }
        return result;
    }

    private static Map<String, Object> toFieldSpecMap(FieldSpec spec) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (spec.getEnumValues() != null && !spec.getEnumValues().isEmpty()) {
            result.put("type", Map.of("enum", spec.getEnumValues()));
        } else {
            result.put("type", spec.getType());
        }
        result.put("required", spec.isRequired());
        if (spec.getDefaultValue() != null) {
            result.put("default", spec.getDefaultValue());
        }
        if (spec.getFields() != null && !spec.getFields().isEmpty()) {
            result.put("fields", toFieldMap(spec.getFields()));
        }
        if (spec.getItems() != null) {
            result.put("items", toFieldSpecMap(spec.getItems()));
        }
        return result;
    }

    private static Map<String, Object> toExprMap(ExprNode expr) {
        if (expr instanceof Lit lit) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("kind", "lit");
            result.put("value", lit.value());
            return result;
        }
        if (expr instanceof Get get) {
            return Map.of("kind", "get", "path", get.path());
        }
        if (expr instanceof Eq eq) {
            return Map.of("kind", "eq", "left", toExprMap(eq.left()), "right", toExprMap(eq.right()));
        }
        if (expr instanceof Neq neq) {
            return Map.of("kind", "neq", "left", toExprMap(neq.left()), "right", toExprMap(neq.right()));
        }
        if (expr instanceof Gt gt) {
            return Map.of("kind", "gt", "left", toExprMap(gt.left()), "right", toExprMap(gt.right()));
        }
        if (expr instanceof Gte gte) {
            return Map.of("kind", "gte", "left", toExprMap(gte.left()), "right", toExprMap(gte.right()));
        }
        if (expr instanceof Lt lt) {
            return Map.of("kind", "lt", "left", toExprMap(lt.left()), "right", toExprMap(lt.right()));
        }
        if (expr instanceof Lte lte) {
            return Map.of("kind", "lte", "left", toExprMap(lte.left()), "right", toExprMap(lte.right()));
        }
        if (expr instanceof Add add) {
            return Map.of("kind", "add", "left", toExprMap(add.left()), "right", toExprMap(add.right()));
        }
        if (expr instanceof Sub sub) {
            return Map.of("kind", "sub", "left", toExprMap(sub.left()), "right", toExprMap(sub.right()));
        }
        if (expr instanceof Mul mul) {
            return Map.of("kind", "mul", "left", toExprMap(mul.left()), "right", toExprMap(mul.right()));
        }
        if (expr instanceof Div div) {
            return Map.of("kind", "div", "left", toExprMap(div.left()), "right", toExprMap(div.right()));
        }
        if (expr instanceof Mod mod) {
            return Map.of("kind", "mod", "left", toExprMap(mod.left()), "right", toExprMap(mod.right()));
        }
        if (expr instanceof Min min) {
            return Map.of("kind", "min", "args", toExprList(min.args()));
        }
        if (expr instanceof Max max) {
            return Map.of("kind", "max", "args", toExprList(max.args()));
        }
        if (expr instanceof Abs abs) {
            return Map.of("kind", "abs", "arg", toExprMap(abs.arg()));
        }
        if (expr instanceof Neg neg) {
            return Map.of("kind", "neg", "arg", toExprMap(neg.arg()));
        }
        if (expr instanceof Round round) {
            return Map.of("kind", "round", "arg", toExprMap(round.arg()));
        }
        if (expr instanceof Floor floor) {
            return Map.of("kind", "floor", "arg", toExprMap(floor.arg()));
        }
        if (expr instanceof Ceil ceil) {
            return Map.of("kind", "ceil", "arg", toExprMap(ceil.arg()));
        }
        if (expr instanceof And and) {
            return Map.of("kind", "and", "args", toExprList(and.args()));
        }
        if (expr instanceof Or or) {
            return Map.of("kind", "or", "args", toExprList(or.args()));
        }
        if (expr instanceof Concat concat) {
            return Map.of("kind", "concat", "args", toExprList(concat.args()));
        }
        if (expr instanceof Coalesce coalesce) {
            return Map.of("kind", "coalesce", "args", toExprList(coalesce.args()));
        }
        if (expr instanceof StartsWith startsWith) {
            return Map.of("kind", "startsWith", "str", toExprMap(startsWith.str()), "prefix", toExprMap(startsWith.prefix()));
        }
        if (expr instanceof EndsWith endsWith) {
            return Map.of("kind", "endsWith", "str", toExprMap(endsWith.str()), "suffix", toExprMap(endsWith.suffix()));
        }
        if (expr instanceof Split split) {
            return Map.of("kind", "split", "str", toExprMap(split.str()), "delimiter", toExprMap(split.delimiter()));
        }
        if (expr instanceof Not not) {
            return Map.of("kind", "not", "arg", toExprMap(not.arg()));
        }
        if (expr instanceof Typeof typeofExpr) {
            return Map.of("kind", "typeof", "arg", toExprMap(typeofExpr.arg()));
        }
        if (expr instanceof IsNull isNull) {
            return Map.of("kind", "isNull", "arg", toExprMap(isNull.arg()));
        }
        if (expr instanceof Len len) {
            return Map.of("kind", "len", "arg", toExprMap(len.arg()));
        }
        if (expr instanceof First first) {
            return Map.of("kind", "first", "array", toExprMap(first.array()));
        }
        if (expr instanceof Last last) {
            return Map.of("kind", "last", "array", toExprMap(last.array()));
        }
        if (expr instanceof At at) {
            return Map.of("kind", "at", "array", toExprMap(at.array()), "index", toExprMap(at.index()));
        }
        if (expr instanceof Slice slice) {
            return Map.of("kind", "slice", "array", toExprMap(slice.array()), "start", toExprMap(slice.start()), "end", toExprMap(slice.end()));
        }
        if (expr instanceof Includes includes) {
            return Map.of("kind", "includes", "array", toExprMap(includes.array()), "item", toExprMap(includes.item()));
        }
        if (expr instanceof Filter filter) {
            return Map.of("kind", "filter", "array", toExprMap(filter.array()), "predicate", toExprMap(filter.predicate()));
        }
        if (expr instanceof ai.manifesto.core.expr.collection.Map map) {
            return Map.of("kind", "map", "array", toExprMap(map.array()), "mapper", toExprMap(map.mapper()));
        }
        if (expr instanceof Find find) {
            return Map.of("kind", "find", "array", toExprMap(find.array()), "predicate", toExprMap(find.predicate()));
        }
        if (expr instanceof Every every) {
            return Map.of("kind", "every", "array", toExprMap(every.array()), "predicate", toExprMap(every.predicate()));
        }
        if (expr instanceof Some some) {
            return Map.of("kind", "some", "array", toExprMap(some.array()), "predicate", toExprMap(some.predicate()));
        }
        if (expr instanceof Append append) {
            return Map.of("kind", "append", "array", toExprMap(append.array()), "items", toExprList(append.items()));
        }
        if (expr instanceof Reduce reduce) {
            return Map.of(
                "kind", "reduce",
                "array", toExprMap(reduce.array()),
                "reducer", toExprMap(reduce.reducer()),
                "initial", toExprMap(reduce.initial())
            );
        }
        if (expr instanceof ObjectExpr objectExpr) {
            return Map.of("kind", "object", "fields", toExprFieldMap(objectExpr.fields()));
        }
        if (expr instanceof Keys keys) {
            return Map.of("kind", "keys", "obj", toExprMap(keys.obj()));
        }
        if (expr instanceof Values values) {
            return Map.of("kind", "values", "obj", toExprMap(values.obj()));
        }
        if (expr instanceof Entries entries) {
            return Map.of("kind", "entries", "obj", toExprMap(entries.obj()));
        }
        if (expr instanceof Merge merge) {
            return Map.of("kind", "merge", "objects", toExprList(merge.objects()));
        }
        if (expr instanceof If ifExpr) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("kind", "if");
            result.put("cond", toExprMap(ifExpr.cond()));
            result.put("then", toExprMap(ifExpr.thenExpr()));
            result.put("else", toExprMap(ifExpr.elseExpr()));
            return result;
        }
        if (expr instanceof Substring substring) {
            return Map.of(
                "kind", "substring",
                "str", toExprMap(substring.str()),
                "start", toExprMap(substring.start()),
                "end", toExprMap(substring.end())
            );
        }
        if (expr instanceof Trim trim) {
            return Map.of("kind", "trim", "str", toExprMap(trim.str()));
        }

        return Map.of("kind", expr.getClass().getSimpleName().toLowerCase(Locale.ROOT));
    }

    private static Map<String, Object> toFlowMap(FlowNode flow) {
        if (flow instanceof FlowNode.Seq seq) {
            return Map.of("kind", "seq", "steps", toFlowList(seq.getSteps()));
        }
        if (flow instanceof FlowNode.If ifFlow) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("kind", "if");
            result.put("cond", toExprMap(ifFlow.getCond()));
            result.put("then", toFlowMap(ifFlow.getThenBranch()));
            if (ifFlow.getElseBranch() != null) {
                result.put("else", toFlowMap(ifFlow.getElseBranch()));
            }
            return result;
        }
        if (flow instanceof FlowNode.Patch patch) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("kind", "patch");
            result.put("op", patch.getOp().name().toLowerCase(Locale.ROOT));
            result.put("path", patch.getPath());
            if (patch.getValue() != null) {
                result.put("value", toExprMap(patch.getValue()));
            }
            return result;
        }
        if (flow instanceof FlowNode.Effect effect) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("kind", "effect");
            result.put("type", effect.getType());
            Map<String, Object> params = new LinkedHashMap<>();
            for (Map.Entry<String, ExprNode> entry : effect.getParams().entrySet()) {
                params.put(entry.getKey(), toExprMap(entry.getValue()));
            }
            result.put("params", params);
            return result;
        }
        if (flow instanceof FlowNode.Call call) {
            return Map.of("kind", "call", "flow", call.getFlow());
        }
        if (flow instanceof FlowNode.Halt halt) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("kind", "halt");
            if (halt.getReason() != null) {
                result.put("reason", halt.getReason());
            }
            return result;
        }
        if (flow instanceof FlowNode.Fail fail) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("kind", "fail");
            result.put("code", fail.getCode());
            if (fail.getMessage() != null) {
                result.put("message", toExprMap(fail.getMessage()));
            }
            return result;
        }
        return Map.of("kind", flow.getClass().getSimpleName().toLowerCase(Locale.ROOT));
    }

    private static List<Object> toExprList(List<ExprNode> exprs) {
        List<Object> result = new ArrayList<>();
        for (ExprNode expr : exprs) {
            result.add(toExprMap(expr));
        }
        return result;
    }

    private static Map<String, Object> toExprFieldMap(Map<String, ExprNode> fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, ExprNode> entry : fields.entrySet()) {
            result.put(entry.getKey(), toExprMap(entry.getValue()));
        }
        return result;
    }

    private static List<Object> toFlowList(List<FlowNode> flows) {
        List<Object> result = new ArrayList<>();
        for (FlowNode flow : flows) {
            result.add(toFlowMap(flow));
        }
        return result;
    }

    private static String toCanonicalJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Map<?, ?> map) {
            return mapToJson(map);
        }
        if (value instanceof List<?> list) {
            return listToJson(list);
        }
        if (value instanceof Set<?> set) {
            List<Object> list = new ArrayList<>(set);
            list.sort((a, b) -> String.valueOf(a).compareTo(String.valueOf(b)));
            return listToJson(list);
        }
        if (value instanceof String str) {
            return quoteJson(str);
        }
        if (value instanceof Boolean bool) {
            return bool ? "true" : "false";
        }
        if (value instanceof Number num) {
            return numberToJson(num);
        }
        if (value instanceof DomainSchema schema) {
            return toCanonicalJson(toSchemaMap(schema));
        }
        if (value instanceof ActionSpec action) {
            return toCanonicalJson(toActionSpecMap(action));
        }
        if (value instanceof ComputedFieldDef computedField) {
            return toCanonicalJson(toComputedFieldMap(computedField));
        }
        if (value instanceof FieldSpec fieldSpec) {
            return toCanonicalJson(toFieldSpecMap(fieldSpec));
        }
        if (value instanceof ExprNode expr) {
            return toCanonicalJson(toExprMap(expr));
        }
        if (value instanceof FlowNode flow) {
            return toCanonicalJson(toFlowMap(flow));
        }
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            List<Object> list = new ArrayList<>();
            for (Object item : array) {
                list.add(item);
            }
            return listToJson(list);
        }
        return quoteJson(String.valueOf(value));
    }

    private static String mapToJson(Map<?, ?> map) {
        List<String> keys = new ArrayList<>();
        for (Object key : map.keySet()) {
            keys.add(String.valueOf(key));
        }
        keys.sort(String::compareTo);

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;
        for (String key : keys) {
            Object value = map.get(key);
            if (!first) {
                builder.append(",");
            }
            builder.append(quoteJson(key)).append(":").append(toCanonicalJson(value));
            first = false;
        }
        builder.append("}");
        return builder.toString();
    }

    private static String listToJson(List<?> list) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                builder.append(",");
            }
            builder.append(toCanonicalJson(item));
            first = false;
        }
        builder.append("]");
        return builder.toString();
    }

    private static String numberToJson(Number number) {
        if (number instanceof Double d) {
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return "null";
            }
        }
        if (number instanceof Float f) {
            if (Float.isNaN(f) || Float.isInfinite(f)) {
                return "null";
            }
        }
        return Objects.toString(number);
    }

    private static String quoteJson(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        builder.append(String.format("\\u%04x", (int) ch));
                    } else {
                        builder.append(ch);
                    }
            }
        }
        builder.append('"');
        return builder.toString();
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
