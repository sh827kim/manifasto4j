package ai.manifesto.core.evaluator;

import ai.manifesto.core.ErrorValue;
import ai.manifesto.core.Result;
import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.expr.arithmetic.*;
import ai.manifesto.core.expr.collection.*;
import ai.manifesto.core.expr.comparison.*;
import ai.manifesto.core.expr.conditional.If;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.expr.logical.*;
import ai.manifesto.core.expr.object.*;
import ai.manifesto.core.expr.string.*;
import ai.manifesto.core.expr.type.*;
import ai.manifesto.core.utils.PathUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map;

/**
 * KR: ExprEvaluator는 규칙 또는 식을 평가해 결과를 산출하는 평가기 타입입니다.
 * EN: ExprEvaluator is an evaluator type that computes results by evaluating rules or expressions.
 */
public class ExprEvaluator {

    /**
     * 표현식 평가
     * 모든 표현식을 평가하고 Result<Object, ErrorValue>를 반환한다.
     *
     * @param expr 평가할 표현식
     * @param ctx 평가 컨텍스트
     * @return 평가 결과 (성공 또는 에러)
     */
    public static Result<Object, ErrorValue> evaluate(ExprNode expr, EvalContext ctx) {
        try {
            return evaluateExpr(expr, ctx);
        } catch (Exception e) {
            // 예상 밖의 예외 처리 (발생하면 안 됨)
            return Result.err(ErrorValue.create(
                "INTERNAL_ERROR",
                "Unexpected error: " + e.getMessage(),
                ctx.getCurrentAction() != null ? ctx.getCurrentAction() : "",
                ctx.getNodePath(),
                ctx.getTrace().getTimestamp()
            ));
        }
    }

    private static Result<Object, ErrorValue> evaluateExpr(ExprNode expr, EvalContext ctx) {
        // ===== Literals =====
        if (expr instanceof Lit lit) {
            return Result.ok(lit.value());
        }
        if (expr instanceof Get get) {
            return evaluateGet(get.path(), ctx);
        }

        // ===== Comparison =====
        if (expr instanceof Eq eq) {
            return evaluateBinary(eq.left(), eq.right(), ctx, (a, b) -> Objects.equals(a, b));
        }
        if (expr instanceof Neq neq) {
            return evaluateBinary(neq.left(), neq.right(), ctx, (a, b) -> !Objects.equals(a, b));
        }
        if (expr instanceof Gt gt) {
            return evaluateBinary(gt.left(), gt.right(), ctx, (a, b) -> toNumber(a) > toNumber(b));
        }
        if (expr instanceof Gte gte) {
            return evaluateBinary(gte.left(), gte.right(), ctx, (a, b) -> toNumber(a) >= toNumber(b));
        }
        if (expr instanceof Lt lt) {
            return evaluateBinary(lt.left(), lt.right(), ctx, (a, b) -> toNumber(a) < toNumber(b));
        }
        if (expr instanceof Lte lte) {
            return evaluateBinary(lte.left(), lte.right(), ctx, (a, b) -> toNumber(a) <= toNumber(b));
        }

        // ===== Logical =====
        if (expr instanceof And and) {
            return evaluateAnd(and.args(), ctx);
        }
        if (expr instanceof Or or) {
            return evaluateOr(or.args(), ctx);
        }
        if (expr instanceof Not not) {
            return evaluateNot(not.arg(), ctx);
        }

        // ===== Conditional =====
        if (expr instanceof If ifExpr) {
            return evaluateIfExpr(ifExpr, ctx);
        }

        // ===== Arithmetic =====
        if (expr instanceof Add add) {
            return evaluateAdd(add.left(), add.right(), ctx);
        }
        if (expr instanceof Sub sub) {
            return evaluateSub(sub.left(), sub.right(), ctx);
        }
        if (expr instanceof Mul mul) {
            return evaluateMul(mul.left(), mul.right(), ctx);
        }
        if (expr instanceof Div div) {
            return evaluateDiv(div.left(), div.right(), ctx);
        }
        if (expr instanceof Mod mod) {
            return evaluateMod(mod.left(), mod.right(), ctx);
        }
        if (expr instanceof Min min) {
            return evaluateMin(min.args(), ctx);
        }
        if (expr instanceof Max max) {
            return evaluateMax(max.args(), ctx);
        }
        if (expr instanceof SumArray sumArray) {
            return evaluateSumArray(sumArray.array(), ctx);
        }
        if (expr instanceof MinArray minArray) {
            return evaluateMinArray(minArray.array(), ctx);
        }
        if (expr instanceof MaxArray maxArray) {
            return evaluateMaxArray(maxArray.array(), ctx);
        }
        if (expr instanceof Floor floor) {
            return evaluateFloor(floor.arg(), ctx);
        }
        if (expr instanceof Ceil ceil) {
            return evaluateCeil(ceil.arg(), ctx);
        }
        if (expr instanceof Round round) {
            return evaluateRound(round.arg(), ctx);
        }
        if (expr instanceof Sqrt sqrt) {
            return evaluateSqrt(sqrt.arg(), ctx);
        }
        if (expr instanceof Pow pow) {
            return evaluatePow(pow.base(), pow.exponent(), ctx);
        }
        if (expr instanceof Abs abs) {
            return evaluateAbs(abs.arg(), ctx);
        }
        if (expr instanceof Neg neg) {
            return evaluateNeg(neg.arg(), ctx);
        }

        // ===== String =====
        if (expr instanceof Concat concat) {
            return evaluateConcat(concat.args(), ctx);
        }
        if (expr instanceof Substring substring) {
            return evaluateSubstring(substring, ctx);
        }
        if (expr instanceof Trim trim) {
            return evaluateTrim(trim.str(), ctx);
        }
        if (expr instanceof ToLowerCase toLowerCase) {
            return evaluateToLowerCase(toLowerCase.str(), ctx);
        }
        if (expr instanceof ToUpperCase toUpperCase) {
            return evaluateToUpperCase(toUpperCase.str(), ctx);
        }
        if (expr instanceof StrLen strLen) {
            return evaluateStrLen(strLen.str(), ctx);
        }
        if (expr instanceof ToString toStringExpr) {
            return evaluateToString(toStringExpr.arg(), ctx);
        }

        // ===== Collection =====
        if (expr instanceof Len len) {
            return evaluateLen(len.arg(), ctx);
        }
        if (expr instanceof At at) {
            return evaluateAt(at.array(), at.index(), ctx);
        }
        if (expr instanceof First first) {
            return evaluateFirst(first.array(), ctx);
        }
        if (expr instanceof Last last) {
            return evaluateLast(last.array(), ctx);
        }
        if (expr instanceof Slice slice) {
            return evaluateSlice(slice.array(), slice.start(), slice.end(), ctx);
        }
        if (expr instanceof Includes includes) {
            return evaluateIncludes(includes.array(), includes.item(), ctx);
        }
        if (expr instanceof Filter filter) {
            return evaluateFilter(filter.array(), filter.predicate(), ctx);
        }
        if (expr instanceof  ai.manifesto.core.expr.collection.Map map) {
            return evaluateMap(map.array(), map.mapper(), ctx);
        }
        if (expr instanceof Find find) {
            return evaluateFind(find.array(), find.predicate(), ctx);
        }
        if (expr instanceof Every every) {
            return evaluateEvery(every.array(), every.predicate(), ctx);
        }
        if (expr instanceof Some some) {
            return evaluateSome(some.array(), some.predicate(), ctx);
        }
        if (expr instanceof Append append) {
            return evaluateAppend(append.array(), append.items(), ctx);
        }

        // ===== Object =====
        if (expr instanceof ObjectExpr obj) {
            return evaluateObject(obj.fields(), ctx);
        }
        if (expr instanceof Keys keys) {
            return evaluateKeys(keys.obj(), ctx);
        }
        if (expr instanceof Values values) {
            return evaluateValues(values.obj(), ctx);
        }
        if (expr instanceof Entries entries) {
            return evaluateEntries(entries.obj(), ctx);
        }
        if (expr instanceof Merge merge) {
            return evaluateMerge(merge.objects(), ctx);
        }

        // ===== Type =====
        if (expr instanceof Typeof typeOfExpr) {
            return evaluateTypeof(typeOfExpr.arg(), ctx);
        }
        if (expr instanceof IsNull isNull) {
            return evaluateIsNull(isNull.arg(), ctx);
        }
        if (expr instanceof Coalesce coalesce) {
            return evaluateCoalesce(coalesce.args(), ctx);
        }

        // ===== Unknown =====
        return Result.err(ErrorValue.create(
                "INTERNAL_ERROR",
                "Unknown expression type: " + expr.getClass().getName(),
                ctx.getCurrentAction() != null ? ctx.getCurrentAction() : "",
                ctx.getNodePath(),
                ctx.getTrace().getTimestamp()
        ));
    }


    // ===== Type Conversion Helpers =====

    private static boolean isWholeNumber(double value) {
        return value == Math.floor(value) && !Double.isInfinite(value);
    }

    private static double toNumber(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number num) return num.doubleValue();
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        if (value instanceof Boolean bool) return bool ? 1.0 : 0.0;
        return 0.0;
    }

    public static boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number num) return num.doubleValue() != 0;
        if (value instanceof String str) return !str.isEmpty();
        if (value instanceof Collection<?> col) return !col.isEmpty();
        if (value instanceof Map<?, ?> map) return !map.isEmpty();
        return true;
    }

    private static String toString(Object value) {
        if (value == null) return "";
        if (value instanceof String str) return str;
        return String.valueOf(value);
    }

    // ===== Get 평가 =====

    private static Result<Object, ErrorValue> evaluateGet(String path, EvalContext ctx) {
        // 특수 변수들
        if (path.startsWith("$")) {
            return evaluateSpecialPath(path, ctx);
        }
        if (path.startsWith("meta.")) {
            String subPath = path.substring(5);
            if ("intentId".equals(subPath)) {
                return Result.ok(ctx.getIntentId());
            }
            if ("actionName".equals(subPath)) {
                return Result.ok(ctx.getCurrentAction());
            }
            return Result.ok(PathUtils.getByPath(ctx.getSnapshot().getMeta(), subPath));
        }

        if (path.equals("input") || path.startsWith("input.")) {
            String subPath = path.equals("input") ? "" : path.substring(6);
            if (subPath.isEmpty()) {
                return Result.ok(ctx.getSnapshot().getInput());
            }
            return Result.ok(PathUtils.getByPath(ctx.getSnapshot().getInput(), subPath));
        }

        if (path.startsWith("computed.")) {
            Map<String, Object> computed = ctx.getSnapshot().getComputed();
            if (computed == null) {
                return Result.ok(null);
            }
            return Result.ok(computed.get(path));
        }

        if (path.startsWith("system.")) {
            String subPath = path.substring(7);
            return Result.ok(PathUtils.getByPath(ctx.getSnapshot().getSystem(), subPath));
        }

        return Result.ok(PathUtils.getByPath(ctx.getSnapshot().getData(), path));
    }

    private static Result<Object, ErrorValue> evaluateSpecialPath(String path, EvalContext ctx) {
        // $item - 컬렉션 필터링 중 현재 항목
        if (path.startsWith("$item")) {
            Object item = ctx.get$item();
            if (item == null) return Result.ok(null);
            if (path.equals("$item")) return Result.ok(item);
            String subPath = path.substring(6); // "$item." 제거
            return Result.ok(PathUtils.getByPath(item, subPath));
        }

        // $index - 컬렉션 필터링 중 현재 인덱스
        if (path.equals("$index")) {
            return Result.ok(ctx.get$index());
        }

        // $array - 컬렉션 필터링 중 전체 배열
        if (path.equals("$array")) {
            return Result.ok(ctx.get$array());
        }

        // $system.uuid - 결정론적 UUID
        if (path.equals("$system.uuid")) {
            return Result.ok(ctx.nextUuid());
        }

        // $system.timestamp - 타임스탐프
        if (path.equals("$system.timestamp")) {
            long timestamp = ctx.getSnapshot().getMeta().getTimestamp();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneOffset.UTC);
            return Result.ok(formatter.format(Instant.ofEpochMilli(timestamp)));
        }

        return Result.ok(null);
    }

    // ===== Binary Operations =====

    private static Result<Object, ErrorValue> evaluateBinary(
        ExprNode left, ExprNode right, EvalContext ctx,
        java.util.function.BiFunction<Object, Object, Object> op) {

        Result<Object, ErrorValue> leftResult = evaluateExpr(left, ctx);
        if (leftResult.isErr()) return leftResult;

        Result<Object, ErrorValue> rightResult = evaluateExpr(right, ctx);
        if (rightResult.isErr()) return rightResult;

        Object result = op.apply(leftResult.unwrap(), rightResult.unwrap());
        return Result.ok(result);
    }

    // ===== Logical Operations =====

    private static Result<Object, ErrorValue> evaluateAnd(List<ExprNode> args, EvalContext ctx) {
        for (ExprNode arg : args) {
            Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
            if (result.isErr()) return result;
            if (!toBoolean(result.unwrap())) return Result.ok(false);
        }
        return Result.ok(true);
    }

    private static Result<Object, ErrorValue> evaluateOr(List<ExprNode> args, EvalContext ctx) {
        for (ExprNode arg : args) {
            Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
            if (result.isErr()) return result;
            if (toBoolean(result.unwrap())) return Result.ok(true);
        }
        return Result.ok(false);
    }

    private static Result<Object, ErrorValue> evaluateNot(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;
        return Result.ok(!toBoolean(result.unwrap()));
    }

    // ===== Conditional =====

    private static Result<Object, ErrorValue> evaluateIfExpr(If ifExpr, EvalContext ctx) {
        Result<Object, ErrorValue> condResult = evaluateExpr(ifExpr.cond(), ctx);
        if (condResult.isErr()) return condResult;

        ExprNode branch = toBoolean(condResult.unwrap()) ? ifExpr.thenExpr() : ifExpr.elseExpr();
        return evaluateExpr(branch, ctx);
    }

    // ===== Arithmetic =====

    private static Result<Object, ErrorValue> evaluateDiv(ExprNode left, ExprNode right, EvalContext ctx) {
        Result<Object, ErrorValue> leftResult = evaluateExpr(left, ctx);
        if (leftResult.isErr()) return leftResult;

        Result<Object, ErrorValue> rightResult = evaluateExpr(right, ctx);
        if (rightResult.isErr()) return rightResult;

        Object leftVal = leftResult.unwrap();
        Object rightVal = rightResult.unwrap();
        double divisor = toNumber(rightVal);
        if (divisor == 0) {
            return Result.ok(null);
        }
        double result = toNumber(leftVal) / divisor;
        if (isWholeNumber(result)) {
            if (isIntegerValue(leftVal) && isIntegerValue(rightVal)) {
                return Result.ok((int) result);
            }
            return Result.ok((long) result);
        }
        return Result.ok(result);
    }

    private static Result<Object, ErrorValue> evaluateMod(ExprNode left, ExprNode right, EvalContext ctx) {
        Result<Object, ErrorValue> leftResult = evaluateExpr(left, ctx);
        if (leftResult.isErr()) return leftResult;

        Result<Object, ErrorValue> rightResult = evaluateExpr(right, ctx);
        if (rightResult.isErr()) return rightResult;

        double divisor = toNumber(rightResult.unwrap());
        if (divisor == 0) return Result.ok(null);
        return Result.ok((long) toNumber(leftResult.unwrap()) % (long) divisor);
    }

    private static Result<Object, ErrorValue> evaluateMin(List<ExprNode> args, EvalContext ctx) {
        if (args.isEmpty()) {
            return Result.ok(null);
        }

        double min = Double.POSITIVE_INFINITY;
        for (ExprNode arg : args) {
            Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
            if (result.isErr()) return result;
            double value = toNumber(result.unwrap());
            if (value < min) {
                min = value;
            }
        }

        return Result.ok(min);
    }

    private static Result<Object, ErrorValue> evaluateMax(List<ExprNode> args, EvalContext ctx) {
        if (args.isEmpty()) {
            return Result.ok(null);
        }

        double max = Double.NEGATIVE_INFINITY;
        for (ExprNode arg : args) {
            Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
            if (result.isErr()) return result;
            double value = toNumber(result.unwrap());
            if (value > max) {
                max = value;
            }
        }

        return Result.ok(max);
    }

    private static Result<Object, ErrorValue> evaluateSumArray(ExprNode arrayExpr, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arrayExpr, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        if (!(value instanceof List<?> list)) {
            return Result.ok(null);
        }
        if (list.isEmpty()) {
            return Result.ok(0.0);
        }
        double sum = 0.0;
        for (Object item : list) {
            sum += toNumber(item);
        }
        return Result.ok(sum);
    }

    private static Result<Object, ErrorValue> evaluateMinArray(ExprNode arrayExpr, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arrayExpr, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        if (!(value instanceof List<?> list)) {
            return Result.ok(null);
        }
        if (list.isEmpty()) {
            return Result.ok(null);
        }
        double min = Double.POSITIVE_INFINITY;
        for (Object item : list) {
            double num = toNumber(item);
            if (num < min) {
                min = num;
            }
        }
        return Result.ok(min);
    }

    private static Result<Object, ErrorValue> evaluateMaxArray(ExprNode arrayExpr, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arrayExpr, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        if (!(value instanceof List<?> list)) {
            return Result.ok(null);
        }
        if (list.isEmpty()) {
            return Result.ok(null);
        }
        double max = Double.NEGATIVE_INFINITY;
        for (Object item : list) {
            double num = toNumber(item);
            if (num > max) {
                max = num;
            }
        }
        return Result.ok(max);
    }

    private static Result<Object, ErrorValue> evaluateFloor(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;
        return Result.ok(Math.floor(toNumber(result.unwrap())));
    }

    private static Result<Object, ErrorValue> evaluateCeil(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;
        return Result.ok(Math.ceil(toNumber(result.unwrap())));
    }

    private static Result<Object, ErrorValue> evaluateRound(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;
        return Result.ok((double) Math.round(toNumber(result.unwrap())));
    }

    private static Result<Object, ErrorValue> evaluateSqrt(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;
        double value = toNumber(result.unwrap());
        if (value < 0) {
            return Result.ok(null);
        }
        return Result.ok(Math.sqrt(value));
    }

    private static Result<Object, ErrorValue> evaluatePow(ExprNode base, ExprNode exponent, EvalContext ctx) {
        Result<Object, ErrorValue> baseResult = evaluateExpr(base, ctx);
        if (baseResult.isErr()) return baseResult;
        Result<Object, ErrorValue> expResult = evaluateExpr(exponent, ctx);
        if (expResult.isErr()) return expResult;
        return Result.ok(Math.pow(toNumber(baseResult.unwrap()), toNumber(expResult.unwrap())));
    }

    private static Result<Object, ErrorValue> evaluateAbs(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;
        return Result.ok(Math.abs(toNumber(result.unwrap())));
    }

    private static Result<Object, ErrorValue> evaluateNeg(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;
        return Result.ok(-toNumber(result.unwrap()));
    }

    private static Result<Object, ErrorValue> evaluateAdd(ExprNode left, ExprNode right, EvalContext ctx) {
        Result<Object, ErrorValue> leftResult = evaluateExpr(left, ctx);
        if (leftResult.isErr()) return leftResult;

        Result<Object, ErrorValue> rightResult = evaluateExpr(right, ctx);
        if (rightResult.isErr()) return rightResult;

        Object leftVal = leftResult.unwrap();
        Object rightVal = rightResult.unwrap();
        double result = toNumber(leftVal) + toNumber(rightVal);
        if (isWholeNumber(result)) {
            // 두 값이 모두 Integer 범위라면 Integer로 반환
            if (isIntegerValue(leftVal) && isIntegerValue(rightVal)) {
                return Result.ok((int) result);
            }
            return Result.ok((long) result);
        }
        return Result.ok(result);
    }

    private static Result<Object, ErrorValue> evaluateSub(ExprNode left, ExprNode right, EvalContext ctx) {
        Result<Object, ErrorValue> leftResult = evaluateExpr(left, ctx);
        if (leftResult.isErr()) return leftResult;

        Result<Object, ErrorValue> rightResult = evaluateExpr(right, ctx);
        if (rightResult.isErr()) return rightResult;

        Object leftVal = leftResult.unwrap();
        Object rightVal = rightResult.unwrap();
        double result = toNumber(leftVal) - toNumber(rightVal);
        if (isWholeNumber(result)) {
            if (isIntegerValue(leftVal) && isIntegerValue(rightVal)) {
                return Result.ok((int) result);
            }
            return Result.ok((long) result);
        }
        return Result.ok(result);
    }

    private static Result<Object, ErrorValue> evaluateMul(ExprNode left, ExprNode right, EvalContext ctx) {
        Result<Object, ErrorValue> leftResult = evaluateExpr(left, ctx);
        if (leftResult.isErr()) return leftResult;

        Result<Object, ErrorValue> rightResult = evaluateExpr(right, ctx);
        if (rightResult.isErr()) return rightResult;

        Object leftVal = leftResult.unwrap();
        Object rightVal = rightResult.unwrap();
        double result = toNumber(leftVal) * toNumber(rightVal);
        if (isWholeNumber(result)) {
            if (isIntegerValue(leftVal) && isIntegerValue(rightVal)) {
                return Result.ok((int) result);
            }
            return Result.ok((long) result);
        }
        return Result.ok(result);
    }

    private static boolean isIntegerValue(Object value) {
        return value instanceof Integer || (value instanceof Number && ((Number) value).doubleValue() == Math.floor(((Number) value).doubleValue()) && ((Number) value).doubleValue() >= Integer.MIN_VALUE && ((Number) value).doubleValue() <= Integer.MAX_VALUE);
    }

    // ===== String Operations =====

    private static Result<Object, ErrorValue> evaluateConcat(List<ExprNode> args, EvalContext ctx) {
        List<Object> values = new ArrayList<>();
        boolean hasArray = false;
        for (ExprNode arg : args) {
            Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
            if (result.isErr()) return result;
            Object value = result.unwrap();
            if (value instanceof List<?>) {
                hasArray = true;
            }
            values.add(value);
        }

        if (hasArray) {
            List<Object> combined = new ArrayList<>();
            for (Object value : values) {
                if (value instanceof List<?> list) {
                    combined.addAll(list);
                } else if (value != null) {
                    combined.add(value);
                }
            }
            return Result.ok(combined);
        }

        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(toString(value));
        }
        return Result.ok(sb.toString());
    }

    private static Result<Object, ErrorValue> evaluateSubstring(Substring substring, EvalContext ctx) {
        Result<Object, ErrorValue> strResult = evaluateExpr(substring.str(), ctx);
        if (strResult.isErr()) return strResult;

        Result<Object, ErrorValue> startResult = evaluateExpr(substring.start(), ctx);
        if (startResult.isErr()) return startResult;

        String str = toString(strResult.unwrap());
        int start = (int) toNumber(startResult.unwrap());
        if (start < 0) start = 0;

        if (substring.end() != null) {
            Result<Object, ErrorValue> endResult = evaluateExpr(substring.end(), ctx);
            if (endResult.isErr()) return endResult;
            int end = (int) toNumber(endResult.unwrap());
            if (end > str.length()) end = str.length();
            if (start > end) return Result.ok("");
            return Result.ok(str.substring(start, end));
        }

        return Result.ok(str.substring(start));
    }

    private static Result<Object, ErrorValue> evaluateTrim(ExprNode str, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(str, ctx);
        if (result.isErr()) return result;
        return Result.ok(toString(result.unwrap()).trim());
    }

    private static Result<Object, ErrorValue> evaluateToLowerCase(ExprNode str, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(str, ctx);
        if (result.isErr()) return result;
        return Result.ok(toString(result.unwrap()).toLowerCase());
    }

    private static Result<Object, ErrorValue> evaluateToUpperCase(ExprNode str, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(str, ctx);
        if (result.isErr()) return result;
        return Result.ok(toString(result.unwrap()).toUpperCase());
    }

    private static Result<Object, ErrorValue> evaluateStrLen(ExprNode str, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(str, ctx);
        if (result.isErr()) return result;
        return Result.ok((double) toString(result.unwrap()).length());
    }

    private static Result<Object, ErrorValue> evaluateToString(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;
        return Result.ok(toString(result.unwrap()));
    }

    // ===== Collection Operations =====

    private static Result<Object, ErrorValue> evaluateLen(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        if (value instanceof Collection<?>) {
            return Result.ok((double) ((Collection<?>) value).size());
        }
        if (value instanceof Map<?, ?>) {
            return Result.ok((double) ((Map<?, ?>) value).size());
        }
        if (value instanceof String) {
            return Result.ok((double) ((String) value).length());
        }
        return Result.ok(0.0);
    }

    private static Result<Object, ErrorValue> evaluateAt(ExprNode arrayExpr, ExprNode indexExpr, EvalContext ctx) {
        Result<Object, ErrorValue> arrayResult = evaluateExpr(arrayExpr, ctx);
        if (arrayResult.isErr()) return arrayResult;

        Result<Object, ErrorValue> indexResult = evaluateExpr(indexExpr, ctx);
        if (indexResult.isErr()) return indexResult;

        Object arrayValue = arrayResult.unwrap();
        if (!(arrayValue instanceof List<?>)) return Result.ok(null);

        int index = (int) toNumber(indexResult.unwrap());
        List<?> list = (List<?>) arrayValue;
        if (index < 0 || index >= list.size()) return Result.ok(null);

        return Result.ok(list.get(index));
    }

    private static Result<Object, ErrorValue> evaluateFirst(ExprNode arrayExpr, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arrayExpr, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(null);

        List<?> list = (List<?>) value;
        return Result.ok(list.isEmpty() ? null : list.get(0));
    }

    private static Result<Object, ErrorValue> evaluateLast(ExprNode arrayExpr, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arrayExpr, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(null);

        List<?> list = (List<?>) value;
        return Result.ok(list.isEmpty() ? null : list.get(list.size() - 1));
    }

    private static Result<Object, ErrorValue> evaluateSlice(ExprNode arrayExpr, ExprNode startExpr, ExprNode endExpr, EvalContext ctx) {
        Result<Object, ErrorValue> arrayResult = evaluateExpr(arrayExpr, ctx);
        if (arrayResult.isErr()) return arrayResult;

        Result<Object, ErrorValue> startResult = evaluateExpr(startExpr, ctx);
        if (startResult.isErr()) return startResult;

        Object value = arrayResult.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(List.of());

        int start = (int) toNumber(startResult.unwrap());
        List<?> list = (List<?>) value;

        if (start < 0) start = 0;
        if (endExpr != null) {
            Result<Object, ErrorValue> endResult = evaluateExpr(endExpr, ctx);
            if (endResult.isErr()) return endResult;
            int end = (int) toNumber(endResult.unwrap());
            if (end > list.size()) end = list.size();
            if (start > end) return Result.ok(List.of());
            return Result.ok(new ArrayList<>(list.subList(start, end)));
        }

        if (start > list.size()) return Result.ok(List.of());
        return Result.ok(new ArrayList<>(list.subList(start, list.size())));
    }

    private static Result<Object, ErrorValue> evaluateIncludes(ExprNode arrayExpr, ExprNode itemExpr, EvalContext ctx) {
        Result<Object, ErrorValue> arrayResult = evaluateExpr(arrayExpr, ctx);
        if (arrayResult.isErr()) return arrayResult;

        Result<Object, ErrorValue> itemResult = evaluateExpr(itemExpr, ctx);
        if (itemResult.isErr()) return itemResult;

        Object value = arrayResult.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(false);

        List<?> list = (List<?>) value;
        return Result.ok(list.contains(itemResult.unwrap()));
    }

    @SuppressWarnings("unchecked")
    private static Result<Object, ErrorValue> evaluateFilter(ExprNode arrayExpr, ExprNode predicateExpr, EvalContext ctx) {
        Result<Object, ErrorValue> arrayResult = evaluateExpr(arrayExpr, ctx);
        if (arrayResult.isErr()) return arrayResult;

        Object value = arrayResult.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(List.of());

        List<?> list = (List<?>) value;
        List<Object> filtered = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            EvalContext itemCtx = ctx.withCollectionContext(item, i, (List<Object>) list);

            Result<Object, ErrorValue> predicateResult = evaluateExpr(predicateExpr, itemCtx);
            if (predicateResult.isErr()) return predicateResult;

            if (toBoolean(predicateResult.unwrap())) {
                filtered.add(item);
            }
        }

        return Result.ok(filtered);
    }

    @SuppressWarnings("unchecked")
    private static Result<Object, ErrorValue> evaluateMap(ExprNode arrayExpr, ExprNode mapperExpr, EvalContext ctx) {
        Result<Object, ErrorValue> arrayResult = evaluateExpr(arrayExpr, ctx);
        if (arrayResult.isErr()) return arrayResult;

        Object value = arrayResult.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(List.of());

        List<?> list = (List<?>) value;
        List<Object> mapped = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            EvalContext itemCtx = ctx.withCollectionContext(item, i, (List<Object>) list);

            Result<Object, ErrorValue> mapperResult = evaluateExpr(mapperExpr, itemCtx);
            if (mapperResult.isErr()) return mapperResult;

            mapped.add(mapperResult.unwrap());
        }

        return Result.ok(mapped);
    }

    @SuppressWarnings("unchecked")
    private static Result<Object, ErrorValue> evaluateFind(ExprNode arrayExpr, ExprNode predicateExpr, EvalContext ctx) {
        Result<Object, ErrorValue> arrayResult = evaluateExpr(arrayExpr, ctx);
        if (arrayResult.isErr()) return arrayResult;

        Object value = arrayResult.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(null);

        List<?> list = (List<?>) value;
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            EvalContext itemCtx = ctx.withCollectionContext(item, i, (List<Object>) list);

            Result<Object, ErrorValue> predicateResult = evaluateExpr(predicateExpr, itemCtx);
            if (predicateResult.isErr()) return predicateResult;

            if (toBoolean(predicateResult.unwrap())) {
                return Result.ok(item);
            }
        }

        return Result.ok(null);
    }

    @SuppressWarnings("unchecked")
    private static Result<Object, ErrorValue> evaluateEvery(ExprNode arrayExpr, ExprNode predicateExpr, EvalContext ctx) {
        Result<Object, ErrorValue> arrayResult = evaluateExpr(arrayExpr, ctx);
        if (arrayResult.isErr()) return arrayResult;

        Object value = arrayResult.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(true);

        List<?> list = (List<?>) value;
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            EvalContext itemCtx = ctx.withCollectionContext(item, i, (List<Object>) list);

            Result<Object, ErrorValue> predicateResult = evaluateExpr(predicateExpr, itemCtx);
            if (predicateResult.isErr()) return predicateResult;

            if (!toBoolean(predicateResult.unwrap())) {
                return Result.ok(false);
            }
        }

        return Result.ok(true);
    }

    @SuppressWarnings("unchecked")
    private static Result<Object, ErrorValue> evaluateSome(ExprNode arrayExpr, ExprNode predicateExpr, EvalContext ctx) {
        Result<Object, ErrorValue> arrayResult = evaluateExpr(arrayExpr, ctx);
        if (arrayResult.isErr()) return arrayResult;

        Object value = arrayResult.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(false);

        List<?> list = (List<?>) value;
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            EvalContext itemCtx = ctx.withCollectionContext(item, i, (List<Object>) list);

            Result<Object, ErrorValue> predicateResult = evaluateExpr(predicateExpr, itemCtx);
            if (predicateResult.isErr()) return predicateResult;

            if (toBoolean(predicateResult.unwrap())) {
                return Result.ok(true);
            }
        }

        return Result.ok(false);
    }

    private static Result<Object, ErrorValue> evaluateAppend(ExprNode arrayExpr, List<ExprNode> itemExprs, EvalContext ctx) {
        Result<Object, ErrorValue> arrayResult = evaluateExpr(arrayExpr, ctx);
        if (arrayResult.isErr()) return arrayResult;

        Object value = arrayResult.unwrap();
        if (!(value instanceof List<?>)) return Result.ok(List.of());

        List<Object> result = new ArrayList<>((List<?>) value);

        for (ExprNode itemExpr : itemExprs) {
            Result<Object, ErrorValue> itemResult = evaluateExpr(itemExpr, ctx);
            if (itemResult.isErr()) return itemResult;
            result.add(itemResult.unwrap());
        }

        return Result.ok(result);
    }

    // ===== Object Operations =====

    private static Result<Object, ErrorValue> evaluateObject(Map<String, ExprNode> fields, EvalContext ctx) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, ExprNode> entry : fields.entrySet()) {
            Result<Object, ErrorValue> fieldResult = evaluateExpr(entry.getValue(), ctx);
            if (fieldResult.isErr()) return fieldResult;
            result.put(entry.getKey(), fieldResult.unwrap());
        }

        return Result.ok(result);
    }

    private static Result<Object, ErrorValue> evaluateKeys(ExprNode objExpr, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(objExpr, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        if (!(value instanceof Map<?, ?>)) return Result.ok(List.of());

        return Result.ok(new ArrayList<>(((Map<?, ?>) value).keySet()));
    }

    private static Result<Object, ErrorValue> evaluateValues(ExprNode objExpr, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(objExpr, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        if (!(value instanceof Map<?, ?>)) return Result.ok(List.of());

        return Result.ok(new ArrayList<>(((Map<?, ?>) value).values()));
    }

    private static Result<Object, ErrorValue> evaluateEntries(ExprNode objExpr, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(objExpr, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        if (!(value instanceof Map<?, ?>)) return Result.ok(List.of());

        Map<?, ?> map = (Map<?, ?>) value;
        List<Object> entries = new ArrayList<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            entries.add(Arrays.asList(entry.getKey(), entry.getValue()));
        }

        return Result.ok(entries);
    }

    @SuppressWarnings("unchecked")
    private static Result<Object, ErrorValue> evaluateMerge(List<ExprNode> objExprs, EvalContext ctx) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (ExprNode objExpr : objExprs) {
            Result<Object, ErrorValue> objResult = evaluateExpr(objExpr, ctx);
            if (objResult.isErr()) return objResult;

            Object value = objResult.unwrap();
            if (value instanceof Map) {
                result.putAll((Map<String, Object>) value);
            }
        }

        return Result.ok(result);
    }

    // ===== Type Operations =====

    private static Result<Object, ErrorValue> evaluateTypeof(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;

        Object value = result.unwrap();
        String type = getType(value);
        return Result.ok(type);
    }

    private static String getType(Object value) {
        if (value == null) return "null";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Number) return "number";
        if (value instanceof String) return "string";
        if (value instanceof List<?>) return "array";
        if (value instanceof Map<?, ?>) return "object";
        return "object"; // 기본값
    }

    private static Result<Object, ErrorValue> evaluateIsNull(ExprNode arg, EvalContext ctx) {
        Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
        if (result.isErr()) return result;
        return Result.ok(result.unwrap() == null);
    }

    private static Result<Object, ErrorValue> evaluateCoalesce(List<ExprNode> args, EvalContext ctx) {
        for (ExprNode arg : args) {
            Result<Object, ErrorValue> result = evaluateExpr(arg, ctx);
            if (result.isErr()) return result;
            if (result.unwrap() != null) return result;
        }
        return Result.ok(null);
    }
}
