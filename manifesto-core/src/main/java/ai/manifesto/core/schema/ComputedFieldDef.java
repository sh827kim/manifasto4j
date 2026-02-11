package ai.manifesto.core.schema;

import ai.manifesto.core.expr.ExprNode;

import java.util.*;

/**
 * KR: ComputedFieldDef는 Core 스키마 계층에서 computed field def 역할을 수행하는 구현 타입입니다.
 * EN: ComputedFieldDef is an implementation type performing computed field def roles in the Core schema layer.
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
