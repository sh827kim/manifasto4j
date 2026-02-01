package ai.manifesto.core.expr;

/**
 * ExprNode - 값을 계산하는 표현식
 *
 * ExprNode는 Snapshot 내의 상태를 읽고 계산을 수행한다.
 * 모든 ExprNode는 부수효과가 없는 순수 함수이고,
 * 항상 값을 반환한다 (절대 예외를 던지지 않음).
 *
 * 56가지 표현식 타입을 지원한다:
 * - Literals: Lit, Get
 * - Comparison: Eq, Neq, Gt, Gte, Lt, Lte
 * - Logical: And, Or, Not
 * - Conditional: If
 * - Arithmetic: Add, Sub, Mul, Div, Mod, Min, Max, SumArray, MinArray, MaxArray, Abs, Neg, Round, Floor, Ceil, Pow, Sqrt
 * - String: Concat, Substring, Trim, StartsWith, EndsWith, Split, ToLowerCase, ToUpperCase, StrLen, ToString
 * - Collection: Len, At, First, Last, Slice, Includes, Filter, Map, Find, Every, Some, Append
 * - Object: ObjectExpr, Keys, Values, Entries, Merge
 * - Type: Typeof, IsNull, Coalesce
 *
 * 예: add(get("data.count"), lit(1))
 *    if(eq(get("input.title"), lit("")), lit(false), lit(true))
 *    filter(get("data.todos"), get("$item.completed"))
 *
 * 주: interface로 구현하여 다양한 패키지의 구현체를 지원한다.
 * switch expression의 완전성 검사는 ExprEvaluator에서 모든 케이스를 다루어 보장한다.
 */
public interface ExprNode {
    // 표현식 타입을 정의하는 marker interface
}
