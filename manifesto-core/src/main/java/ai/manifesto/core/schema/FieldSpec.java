package ai.manifesto.core.schema;

import java.util.Objects;

/**
 * FieldSpec - 필드(Field) 정의
 *
 * 데이터 필드의 메타데이터:
 * - 이름: fieldName
 * - 타입: type (string, number, boolean, object, array 등)
 * - 필수 여부: required
 * - 기본값: defaultValue
 *
 * 특징:
 * - 불변 객체 (모든 필드 final)
 * - 기본값은 선택사항
 */
public final class FieldSpec {
    private final String fieldName;
    private final String type;
    private final boolean required;
    private final Object defaultValue;

    public FieldSpec(
        String fieldName,
        String type,
        boolean required,
        Object defaultValue
    ) {
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName required");
        this.type = Objects.requireNonNull(type, "type required");
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * 필드 빌더
     */
    public static class Builder {
        private final String fieldName;
        private final String type;
        private boolean required = false;
        private Object defaultValue = null;

        public Builder(String fieldName, String type) {
            this.fieldName = Objects.requireNonNull(fieldName);
            this.type = Objects.requireNonNull(type);
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder defaultValue(Object value) {
            this.defaultValue = value;
            return this;
        }

        public FieldSpec build() {
            return new FieldSpec(fieldName, type, required, defaultValue);
        }
    }

    /**
     * 편의 메서드: 필수 필드
     */
    public static FieldSpec required(String fieldName, String type) {
        return new FieldSpec(fieldName, type, true, null);
    }

    /**
     * 편의 메서드: 선택 필드
     */
    public static FieldSpec optional(String fieldName, String type) {
        return new FieldSpec(fieldName, type, false, null);
    }

    @Override
    public String toString() {
        return "FieldSpec{" +
               "fieldName='" + fieldName + '\'' +
               ", type='" + type + '\'' +
               ", required=" + required +
               '}';
    }
}
