package ai.manifesto.core.schema;

import java.util.List;
import java.util.Map;
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
    private final Map<String, FieldSpec> fields;
    private final FieldSpec items;
    private final List<Object> enumValues;
    private final String description;

    public FieldSpec(
        String fieldName,
        String type,
        boolean required,
        Object defaultValue
    ) {
        this(fieldName, type, required, defaultValue, null, null, null, null);
    }

    public FieldSpec(
        String fieldName,
        String type,
        boolean required,
        Object defaultValue,
        Map<String, FieldSpec> fields,
        FieldSpec items,
        List<Object> enumValues
    ) {
        this(fieldName, type, required, defaultValue, fields, items, enumValues, null);
    }

    public FieldSpec(
        String fieldName,
        String type,
        boolean required,
        Object defaultValue,
        Map<String, FieldSpec> fields,
        FieldSpec items,
        List<Object> enumValues,
        String description
    ) {
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName required");
        this.type = normalizeType(Objects.requireNonNull(type, "type required"));
        this.required = required;
        this.defaultValue = defaultValue;
        this.fields = fields != null ? Map.copyOf(fields) : null;
        this.items = items;
        this.enumValues = enumValues != null ? new java.util.ArrayList<>(enumValues) : null;
        this.description = description;
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

    public Map<String, FieldSpec> getFields() {
        return fields;
    }

    public FieldSpec getItems() {
        return items;
    }

    public List<Object> getEnumValues() {
        return enumValues;
    }

    public String getDescription() {
        return description;
    }

    private static String normalizeType(String type) {
        if ("integer".equals(type)) {
            return "number";
        }
        return type;
    }

    /**
     * 필드 빌더
     */
    public static class Builder {
        private final String fieldName;
        private final String type;
        private boolean required = false;
        private Object defaultValue = null;
        private Map<String, FieldSpec> fields = null;
        private FieldSpec items = null;
        private List<Object> enumValues = null;
        private String description = null;

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

        public Builder fields(Map<String, FieldSpec> fields) {
            this.fields = fields;
            return this;
        }

        public Builder items(FieldSpec items) {
            this.items = items;
            return this;
        }

        public Builder enumValues(List<Object> enumValues) {
            this.enumValues = enumValues;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public FieldSpec build() {
            return new FieldSpec(fieldName, type, required, defaultValue, fields, items, enumValues, description);
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
               ", fields=" + (fields != null ? fields.keySet() : null) +
               ", items=" + (items != null ? items.fieldName : null) +
               ", enumValues=" + enumValues +
               ", description=" + description +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldSpec that)) return false;
        return required == that.required &&
               Objects.equals(fieldName, that.fieldName) &&
               Objects.equals(type, that.type) &&
               Objects.equals(defaultValue, that.defaultValue) &&
               Objects.equals(fields, that.fields) &&
               Objects.equals(items, that.items) &&
               Objects.equals(enumValues, that.enumValues) &&
               Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, type, required, defaultValue, fields, items, enumValues, description);
    }
}
