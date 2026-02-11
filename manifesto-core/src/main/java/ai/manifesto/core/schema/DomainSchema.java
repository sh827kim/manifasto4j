package ai.manifesto.core.schema;

import java.util.*;

/**
 * KR: DomainSchema는 Core 스키마 계층에서 domain schema 역할을 수행하는 구현 타입입니다.
 * EN: DomainSchema is an implementation type performing domain schema roles in the Core schema layer.
 */
public final class DomainSchema {
    private final String id;
    private final String version;
    private final String hash;
    private final Map<String, TypeSpec> types;
    private final DomainMeta meta;
    private final Map<String, ActionSpec> actions;
    private final Map<String, ComputedFieldDef> computedFields;
    private final Map<String, FieldSpec> dataFields;

    public DomainSchema(
        String id,
        String version,
        String hash,
        Map<String, TypeSpec> types,
        DomainMeta meta,
        Map<String, ActionSpec> actions,
        Map<String, ComputedFieldDef> computedFields,
        Map<String, FieldSpec> dataFields
    ) {
        this.id = Objects.requireNonNull(id, "id required");
        this.version = Objects.requireNonNull(version, "version required");
        this.hash = Objects.requireNonNull(hash, "hash required");
        this.types = Collections.unmodifiableMap(
            new HashMap<>(types != null ? types : new HashMap<>())
        );
        this.meta = meta;
        this.actions = Collections.unmodifiableMap(
            new HashMap<>(actions != null ? actions : new HashMap<>())
        );
        this.computedFields = Collections.unmodifiableMap(
            new HashMap<>(computedFields != null ? computedFields : new HashMap<>())
        );
        this.dataFields = Collections.unmodifiableMap(
            new HashMap<>(dataFields != null ? dataFields : new HashMap<>())
        );
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getHash() {
        return hash;
    }

    public Map<String, TypeSpec> getTypes() {
        return types;
    }

    public DomainMeta getMeta() {
        return meta;
    }

    /**
     * 액션 조회
     *
     * @param actionId 액션 ID
     * @return ActionSpec 또는 null (없으면)
     */
    public ActionSpec getAction(String actionId) {
        return actions.get(actionId);
    }

    /**
     * 모든 액션 조회
     */
    public Map<String, ActionSpec> getActions() {
        return actions;
    }

    /**
     * 계산 필드 조회
     *
     * @param fieldName 계산 필드 이름
     * @return ComputedFieldDef 또는 null (없으면)
     */
    public ComputedFieldDef getComputedField(String fieldName) {
        return computedFields.get(fieldName);
    }

    /**
     * 모든 계산 필드 조회
     */
    public Map<String, ComputedFieldDef> getComputedFields() {
        return computedFields;
    }

    /**
     * 데이터 필드 조회
     *
     * @param fieldName 필드 이름
     * @return FieldSpec 또는 null (없으면)
     */
    public FieldSpec getDataField(String fieldName) {
        return dataFields.get(fieldName);
    }

    /**
     * 모든 데이터 필드 조회
     */
    public Map<String, FieldSpec> getDataFields() {
        return dataFields;
    }

    /**
     * 데이터 검증
     *
     * @param data 검증할 데이터
     * @return 검증 성공 여부
     */
    public boolean validateData(Map<String, Object> data) {
        if (data == null) {
            return false;
        }

        // 필수 필드 확인
        for (Map.Entry<String, FieldSpec> entry : dataFields.entrySet()) {
            String fieldName = entry.getKey();
            FieldSpec spec = entry.getValue();

            if (spec.isRequired() && !data.containsKey(fieldName)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 스키마 빌더
     */
    public static class Builder {
        private final String id;
        private final String version;
        private String hash;
        private final Map<String, TypeSpec> types = new HashMap<>();
        private DomainMeta meta;
        private final Map<String, ActionSpec> actions = new HashMap<>();
        private final Map<String, ComputedFieldDef> computedFields = new HashMap<>();
        private final Map<String, FieldSpec> dataFields = new HashMap<>();

        public Builder(String id, String version) {
            this.id = Objects.requireNonNull(id);
            this.version = Objects.requireNonNull(version);
            this.hash = "";
        }

        public Builder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public Builder meta(DomainMeta meta) {
            this.meta = meta;
            return this;
        }

        public Builder addType(TypeSpec typeSpec) {
            this.types.put(typeSpec.getName(), typeSpec);
            return this;
        }

        public Builder types(Map<String, TypeSpec> types) {
            this.types.clear();
            if (types != null) {
                this.types.putAll(types);
            }
            return this;
        }

        public Builder addAction(ActionSpec action) {
            this.actions.put(action.getActionId(), action);
            return this;
        }

        public Builder addComputedField(ComputedFieldDef field) {
            this.computedFields.put(field.getFieldName(), field);
            return this;
        }

        public Builder addDataField(FieldSpec field) {
            this.dataFields.put(field.getFieldName(), field);
            return this;
        }

        public DomainSchema build() {
            return new DomainSchema(id, version, hash, types, meta, actions, computedFields, dataFields);
        }
    }

    /**
     * 빈 스키마 생성 (테스트용)
     */
    public static DomainSchema empty() {
        return new Builder("test-schema", "1.0.0")
            .hash("hash-empty")
            .build();
    }

    @Override
    public String toString() {
        return "DomainSchema{" +
               "id='" + id + '\'' +
               ", version='" + version + '\'' +
               ", types=" + types.size() +
               ", actions=" + actions.size() +
               ", computedFields=" + computedFields.size() +
               ", dataFields=" + dataFields.size() +
               '}';
    }
}
