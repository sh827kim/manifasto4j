package ai.manifesto.core.schema;

import ai.manifesto.core.expr.ExprNode;

import java.util.*;

/**
 * ComputedFieldDef - 계산 필드(Computed Field) 정의
 *
 * 파생 필드의 메타데이터:
 * - 이름: fieldName
 * - 식: expression (계산 방식)
 * - 의존성: dependencies (다른 computed 필드들)
 *
 * 특징:
 * - 불변 객체 (모든 필드 final)
 * - 식은 결정론적이어야 함
 * - 의존성은 DAG 형태여야 함 (순환 참조 금지)
 */
public final class ComputedFieldDef {
    private final String fieldName;
    private final ExprNode expression;
    private final Set<String> dependencies;  // 다른 computed 필드 의존성

    public ComputedFieldDef(
        String fieldName,
        ExprNode expression,
        Set<String> dependencies
    ) {
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName required");
        this.expression = Objects.requireNonNull(expression, "expression required");
        this.dependencies = Collections.unmodifiableSet(
            new HashSet<>(dependencies != null ? dependencies : new HashSet<>())
        );
    }

    public String getFieldName() {
        return fieldName;
    }

    public ExprNode getExpression() {
        return expression;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    /**
     * 계산 필드 빌더
     */
    public static class Builder {
        private final String fieldName;
        private final ExprNode expression;
        private final Set<String> dependencies = new HashSet<>();

        public Builder(String fieldName, ExprNode expression) {
            this.fieldName = Objects.requireNonNull(fieldName);
            this.expression = Objects.requireNonNull(expression);
        }

        public Builder addDependency(String fieldName) {
            this.dependencies.add(fieldName);
            return this;
        }

        public Builder addDependencies(Collection<String> fieldNames) {
            this.dependencies.addAll(fieldNames);
            return this;
        }

        public ComputedFieldDef build() {
            return new ComputedFieldDef(fieldName, expression, dependencies);
        }
    }

    /**
     * 편의 메서드: 의존성 없는 계산 필드
     */
    public static ComputedFieldDef simple(String fieldName, ExprNode expression) {
        return new ComputedFieldDef(fieldName, expression, new HashSet<>());
    }

    @Override
    public String toString() {
        return "ComputedFieldDef{" +
               "fieldName='" + fieldName + '\'' +
               ", dependencies=" + dependencies +
               '}';
    }
}
