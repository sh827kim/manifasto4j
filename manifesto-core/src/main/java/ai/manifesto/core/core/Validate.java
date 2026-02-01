package ai.manifesto.core.core;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.utils.DagUtils;

import java.util.*;
import java.util.Objects;

/**
 * Validate - DomainSchema와 Snapshot의 일관성을 검증
 *
 * 검증 역할:
 * 1. Snapshot 구조 검증 (필수 필드 확인)
 * 2. Data 필드 검증 (필수 필드, 타입)
 * 3. Input 필드 검증 (필드명 유효성)
 * 4. Computed 필드 검증 (DAG, 순환 참조 감지)
 * 5. System 필드 검증
 *
 * 반환값:
 * - ValidationResult: isValid 여부 + 에러 메시지 목록
 *
 * 상태: Phase 5 완성 ✅
 * - DomainSchema 통합 완료
 * - 모든 검증 로직 구현
 */
public class Validate {

    private Validate() {
        // 정적 메서드만 제공
    }

    /**
     * 검증 결과를 나타내는 record
     *
     * @param isValid 검증 성공 여부
     * @param errors 발견된 에러 목록 (비어있으면 검증 성공)
     */
    public record ValidationResult(
        boolean isValid,
        List<ValidationError> errors
    ) {
        public ValidationResult {
            Objects.requireNonNull(errors, "errors list is required");
        }

        /**
         * 검증 결과 요약
         */
        @Override
        public String toString() {
            if (isValid) {
                return "ValidationResult{valid}";
            }
            return "ValidationResult{errors=" + errors.size() + ": " + errors + "}";
        }

        /**
         * 빈 성공 결과 생성
         */
        public static ValidationResult valid() {
            return new ValidationResult(true, List.of());
        }

        /**
         * 에러 결과 생성
         */
        public static ValidationResult invalid(List<ValidationError> errors) {
            return new ValidationResult(false, new ArrayList<>(errors));
        }

        /**
         * 단일 에러로 결과 생성
         */
        public static ValidationResult invalid(ValidationError error) {
            return new ValidationResult(false, List.of(error));
        }

        /**
         * 레거시 메시지 목록 (문자열) 반환
         */
        public List<String> messages() {
            List<String> messages = new ArrayList<>();
            for (ValidationError error : errors) {
                messages.add(error.message());
            }
            return messages;
        }
    }

    /**
     * 검증 에러
     *
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param path 문제 위치 (semantic path)
     */
    public record ValidationError(
        String code,
        String message,
        String path
    ) {
        public ValidationError {
            Objects.requireNonNull(code, "code is required");
            Objects.requireNonNull(message, "message is required");
        }
    }

    /**
     * Schema와 Snapshot 검증
     *
     * 현재 구현:
     * - 기본 구조 검증만 수행
     * - DomainSchema 구현 후 상세 검증 추가 필요
     *
     * @param schema 도메인 스키마
     * @param snapshot 검증할 Snapshot
     * @return 검증 결과
     */
    public static ValidationResult validate(DomainSchema schema, Snapshot snapshot) {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(snapshot, "snapshot is required");

        List<ValidationError> errors = new ArrayList<>();

        // 0. Schema 검증 (정합성/참조/해시)
        validateSchema(schema, errors);

        // 1. Snapshot의 기본 구조 검증
        validateSnapshotStructure(snapshot, errors);

        // 2. Data 필드 검증
        validateDataField(snapshot, schema, errors);

        // 3. Input 필드 검증
        validateInputField(snapshot, schema, errors);

        // 4. System 필드 검증
        validateSystemField(snapshot, errors);

        return errors.isEmpty()
            ? ValidationResult.valid()
            : ValidationResult.invalid(errors);
    }

    private static void addError(List<ValidationError> errors, String code, String message, String path) {
        errors.add(new ValidationError(code, message, path));
    }

    /**
     * Snapshot의 기본 구조 검증
     *
     * 필수 필드: data, computed, system, input, meta
     */
    private static void validateSnapshotStructure(Snapshot snapshot, List<ValidationError> errors) {
        // data 필드 검증
        if (snapshot.getData() == null) {
            addError(errors, "INVALID_SNAPSHOT", "Snapshot.data must not be null", "snapshot.data");
        } else if (!(snapshot.getData() instanceof Map)) {
            addError(errors, "INVALID_SNAPSHOT", "Snapshot.data must be a Map", "snapshot.data");
        }

        // computed 필드 검증
        if (snapshot.getComputed() == null) {
            addError(errors, "INVALID_SNAPSHOT", "Snapshot.computed must not be null", "snapshot.computed");
        } else if (!(snapshot.getComputed() instanceof Map)) {
            addError(errors, "INVALID_SNAPSHOT", "Snapshot.computed must be a Map", "snapshot.computed");
        }

        // system 필드 검증
        if (snapshot.getSystem() == null) {
            addError(errors, "INVALID_SNAPSHOT", "Snapshot.system must not be null", "snapshot.system");
        }

        // input 필드 검증
        if (snapshot.getInput() == null) {
            addError(errors, "INVALID_SNAPSHOT", "Snapshot.input must not be null", "snapshot.input");
        } else if (!(snapshot.getInput() instanceof Map)) {
            addError(errors, "INVALID_SNAPSHOT", "Snapshot.input must be a Map", "snapshot.input");
        }

        // meta 필드 검증
        if (snapshot.getMeta() == null) {
            addError(errors, "INVALID_SNAPSHOT", "Snapshot.meta must not be null", "snapshot.meta");
        } else {
            // version은 0 이상
            if (snapshot.getMeta().getVersion() < 0) {
                addError(errors, "INVALID_SNAPSHOT", "Snapshot.meta.version must be >= 0", "snapshot.meta.version");
            }
            // timestamp는 유효해야 함
            if (snapshot.getMeta().getTimestamp() <= 0) {
                addError(errors, "INVALID_SNAPSHOT", "Snapshot.meta.timestamp must be > 0", "snapshot.meta.timestamp");
            }
            // schemaHash는 존재해야 함 (빈 문자열 가능)
            if (snapshot.getMeta().getSchemaHash() == null) {
                addError(errors, "INVALID_SNAPSHOT", "Snapshot.meta.schemaHash must not be null", "snapshot.meta.schemaHash");
            }
        }
    }

    /**
     * Data 필드 검증
     *
     * DomainSchema의 dataFields와 비교하여 필드 검증:
     * - 필수 필드 확인
     * - 필드명 유효성 확인
     */
    private static void validateDataField(
        Snapshot snapshot,
        DomainSchema schema,
        List<ValidationError> errors
    ) {
        Map<String, Object> data = snapshot.getData();

        // Schema의 필드 정의와 비교
        Map<String, FieldSpec> schemaFields = schema.getDataFields();

        // 필수 필드 확인 (data가 null이거나 비어있어도 필수 필드는 검증해야 함)
        for (Map.Entry<String, FieldSpec> entry : schemaFields.entrySet()) {
            String fieldName = entry.getKey();
            FieldSpec spec = entry.getValue();

            if (spec.isRequired()) {
                if (data == null || !data.containsKey(fieldName)) {
                    addError(errors, "INVALID_DATA", "Data field '" + fieldName + "' is required", "data." + fieldName);
                }
            }
        }
    }

    /**
     * Input 필드 검증
     *
     * Input의 필드명 유효성 검증:
     * - 키는 영문자, 숫자, 언더스코어만 허용
     */
    private static void validateInputField(
        Snapshot snapshot,
        DomainSchema schema,
        List<ValidationError> errors
    ) {
        Map<String, Object> input = snapshot.getInput();

        if (input != null && !input.isEmpty()) {
            // 모든 입력 필드 검증
            for (Map.Entry<String, Object> entry : input.entrySet()) {
                String key = entry.getKey();

                // 키는 영문자, 숫자, 언더스코어만 허용
                if (!key.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                    addError(errors, "INVALID_INPUT", "Input key '" + key + "' is not a valid identifier", "input." + key);
                }
            }
        }
    }

    /**
     * Computed 필드 검증 (DAG)
     *
     * 계산 필드의 순환 참조를 검증한다.
     * DAG 구조여야 함 (순환 참조 금지).
     */
    private static void validateComputedField(
        DomainSchema schema,
        List<ValidationError> errors
    ) {
        Map<String, ComputedFieldDef> computedFields = schema.getComputedFields();

        if (computedFields.isEmpty()) {
            return;
        }

        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(computedFields);
        List<List<String>> cycles = DagUtils.detectCycles(graph);

        if (!cycles.isEmpty()) {
            for (List<String> cycle : cycles) {
                addError(
                    errors,
                    "V-002",
                    "Cyclic dependency: " + String.join(" -> ", cycle),
                    cycle.isEmpty() ? "computed" : cycle.get(0)
                );
            }
        }
    }

    /**
     * System 필드 검증
     *
     * System 필드는 내부용이므로 기본 구조만 검증
     */
    private static void validateSystemField(Snapshot snapshot, List<ValidationError> errors) {
        var system = snapshot.getSystem();

        if (system != null) {
            // Status가 유효해야 함
            if (system.getStatus() == null) {
                addError(errors, "INVALID_SNAPSHOT", "Snapshot.system.status must not be null", "snapshot.system.status");
            }

            // pendingRequirements는 리스트여야 함
            if (system.getPendingRequirements() != null) {
                if (!(system.getPendingRequirements() instanceof List)) {
                    addError(errors, "INVALID_SNAPSHOT", "Snapshot.system.pendingRequirements must be a List", "snapshot.system.pendingRequirements");
                }
            }

            // errors는 리스트여야 함
            if (system.getErrors() != null) {
                if (!(system.getErrors() instanceof List)) {
                    addError(errors, "INVALID_SNAPSHOT", "Snapshot.system.errors must be a List", "snapshot.system.errors");
                }
            }
        }
    }

    /**
     * 편의 메서드: 검증만 수행하고 boolean 반환
     *
     * @param schema 도메인 스키마
     * @param snapshot 검증할 Snapshot
     * @return 검증 성공 여부
     */
    public static boolean isValid(DomainSchema schema, Snapshot snapshot) {
        return validate(schema, snapshot).isValid();
    }

    /**
     * 편의 메서드: 검증 실패 시 첫 번째 에러 반환
     *
     * @param schema 도메인 스키마
     * @param snapshot 검증할 Snapshot
     * @return 첫 번째 에러, 또는 null (검증 성공)
     */
    public static String getFirstError(DomainSchema schema, Snapshot snapshot) {
        ValidationResult result = validate(schema, snapshot);
        return result.isValid() ? null : result.errors().get(0).message();
    }

    /**
     * Schema 자체 검증
     */
    private static void validateSchema(DomainSchema schema, List<ValidationError> errors) {
        if (!ValidationUtils.isValidSchemaId(schema.getId())) {
            addError(errors, "SCHEMA_ERROR", "Schema id must be a valid URI or UUID", "id");
        }

        if (!ValidationUtils.isValidSemver(schema.getVersion())) {
            addError(errors, "SCHEMA_ERROR", "Schema version must follow Semantic Versioning 2.0", "version");
        }

        String schemaHash = schema.getHash();
        if (schemaHash == null || schemaHash.isEmpty()) {
            addError(errors, "SCHEMA_ERROR", "Schema hash is required", "hash");
        } else {
            String expectedHash = ValidationUtils.computeSchemaHash(schema);
            if (!schemaHash.equals(expectedHash)) {
                addError(errors, "V-008", "Schema hash mismatch: expected " + expectedHash + ", got " + schemaHash, "hash");
            }
        }

        if (schema.getDataFields().isEmpty()) {
            addError(errors, "SCHEMA_ERROR", "StateSpec.fields must not be empty", "state.fields");
        }

        if (schema.getComputedFields().isEmpty()) {
            addError(errors, "SCHEMA_ERROR", "ComputedSpec.fields must not be empty", "computed.fields");
        }

        if (schema.getActions().isEmpty()) {
            addError(errors, "SCHEMA_ERROR", "actions must not be empty", "actions");
        }

        validateStateDefaults(schema, errors);
        validateComputedDeps(schema, errors);
        validateComputedExprPaths(schema, errors);
        validateComputedDepsCoverage(schema, errors);
        validateActionExprPaths(schema, errors);
        validateComputedField(schema, errors);
        validateCallReferences(schema, errors);
        validateCallGraph(schema, errors);
    }

    /**
     * 선택 필드는 기본값 필수
     */
    private static void validateStateDefaults(DomainSchema schema, List<ValidationError> errors) {
        for (Map.Entry<String, FieldSpec> entry : schema.getDataFields().entrySet()) {
            visitFieldSpec(entry.getValue(), "state.fields." + entry.getKey(), errors);
        }
    }

    private static void visitFieldSpec(FieldSpec spec, String path, List<ValidationError> errors) {
        if (spec == null) {
            return;
        }
        if (!spec.isRequired() && spec.getDefaultValue() == null) {
            addError(errors, "SCHEMA_ERROR", "Optional fields must define a default value", path);
        }
        if ("object".equals(spec.getType()) && spec.getFields() != null) {
            for (Map.Entry<String, FieldSpec> entry : spec.getFields().entrySet()) {
                visitFieldSpec(entry.getValue(), path + "." + entry.getKey(), errors);
            }
        }
        if ("array".equals(spec.getType()) && spec.getItems() != null) {
            visitFieldSpec(spec.getItems(), path + "[]", errors);
        }
    }

    /**
     * Computed deps 경로 존재 검증
     */
    private static void validateComputedDeps(DomainSchema schema, List<ValidationError> errors) {
        for (Map.Entry<String, ComputedFieldDef> entry : schema.getComputedFields().entrySet()) {
            String fieldName = entry.getKey();
            ComputedFieldDef spec = entry.getValue();
            for (String dep : spec.getDependencies()) {
                boolean exists = ValidationUtils.pathExistsInComputedSpec(schema.getComputedFields(), dep)
                    || ValidationUtils.pathExistsInStateSpec(schema.getDataFields(), dep);
                if (!exists) {
                    addError(errors, "V-001",
                        "Unknown dependency path: " + dep,
                        "computed.fields." + fieldName);
                }
            }
        }
    }

    /**
     * Computed 표현식 내 get 경로 검증
     */
    private static void validateComputedExprPaths(DomainSchema schema, List<ValidationError> errors) {
        for (Map.Entry<String, ComputedFieldDef> entry : schema.getComputedFields().entrySet()) {
            String fieldName = entry.getKey();
            ComputedFieldDef spec = entry.getValue();
            List<String> exprPaths = ValidationUtils.collectGetPathsFromExpr(spec.getExpression());

            for (String exprPath : exprPaths) {
                if (exprPath.startsWith("$")) {
                    continue;
                }
                if (exprPath.startsWith("computed.")) {
                    if (!ValidationUtils.pathExistsInComputedSpec(schema.getComputedFields(), exprPath)) {
                        addError(errors, "V-003",
                            "Unknown path in computed expression: " + exprPath,
                            "computed.fields." + fieldName);
                    }
                    continue;
                }
                if (exprPath.startsWith("input.")) {
                    addError(errors, "V-003",
                        "Unknown path in computed expression: " + exprPath,
                        "computed.fields." + fieldName);
                    continue;
                }
                if (exprPath.startsWith("system.")) {
                    addError(errors, "V-003",
                        "Unknown path in computed expression: " + exprPath,
                        "computed.fields." + fieldName);
                    continue;
                }
                if (!ValidationUtils.pathExistsInStateSpec(schema.getDataFields(), exprPath)) {
                    addError(errors, "V-003",
                        "Unknown path in computed expression: " + exprPath,
                        "computed.fields." + fieldName);
                }
            }
        }
    }

    /**
     * Computed deps 커버리지 검증
     */
    private static void validateComputedDepsCoverage(DomainSchema schema, List<ValidationError> errors) {
        for (Map.Entry<String, ComputedFieldDef> entry : schema.getComputedFields().entrySet()) {
            String fieldName = entry.getKey();
            ComputedFieldDef spec = entry.getValue();
            List<String> exprPaths = ValidationUtils.collectGetPathsFromExpr(spec.getExpression());
            Set<String> deps = new HashSet<>(spec.getDependencies());

            Set<String> relevantPaths = new HashSet<>();
            for (String exprPath : exprPaths) {
                if (exprPath.startsWith("$")) {
                    continue;
                }
                if (exprPath.equals("input") || exprPath.startsWith("input.")) {
                    continue;
                }
                if (exprPath.equals("system") || exprPath.startsWith("system.")) {
                    continue;
                }
                if (exprPath.equals("meta") || exprPath.startsWith("meta.")) {
                    continue;
                }
                if (exprPath.startsWith("computed.")) {
                    if (ValidationUtils.pathExistsInComputedSpec(schema.getComputedFields(), exprPath)) {
                        relevantPaths.add(exprPath);
                    }
                    continue;
                }
                if (ValidationUtils.pathExistsInStateSpec(schema.getDataFields(), exprPath)) {
                    relevantPaths.add(exprPath);
                }
            }

            for (String exprPath : relevantPaths) {
                if (!hasDependency(deps, exprPath)) {
                    addError(errors, "V-001",
                        "Missing dependency for computed expression path: " + exprPath,
                        "computed.fields." + fieldName);
                }
            }
        }
    }

    private static boolean hasDependency(Set<String> deps, String exprPath) {
        return deps.contains(exprPath);
    }

    /**
     * Action 표현식 경로 검증
     */
    private static void validateActionExprPaths(DomainSchema schema, List<ValidationError> errors) {
        for (Map.Entry<String, ActionSpec> entry : schema.getActions().entrySet()) {
            String actionName = entry.getKey();
            ActionSpec action = entry.getValue();

            List<String> exprPaths = new ArrayList<>();
            exprPaths.addAll(ValidationUtils.collectGetPathsFromFlow(action.getFlow()));
            if (action.getAvailable() != null) {
                exprPaths.addAll(ValidationUtils.collectGetPathsFromExpr(action.getAvailable()));
            }

            for (String exprPath : exprPaths) {
                if (exprPath.startsWith("$")) {
                    continue;
                }

                if (exprPath.equals("input") || exprPath.startsWith("input.")) {
                    if (action.getInputSpec() != null) {
                        String subPath = exprPath.equals("input") ? "" : exprPath.substring(6);
                        if (!ValidationUtils.pathExistsInFieldSpec(action.getInputSpec(), subPath)) {
                            addError(errors, "V-003",
                                "Unknown input path: " + exprPath,
                                "actions." + actionName);
                        }
                    }
                    continue;
                }

                if (exprPath.startsWith("computed.")) {
                    if (!ValidationUtils.pathExistsInComputedSpec(schema.getComputedFields(), exprPath)) {
                        addError(errors, "V-003",
                            "Unknown computed path: " + exprPath,
                            "actions." + actionName);
                    }
                    continue;
                }

                if (exprPath.startsWith("system.")) {
                    continue;
                }

                if (exprPath.equals("meta") || exprPath.startsWith("meta.")) {
                    continue;
                }

                if (!ValidationUtils.pathExistsInStateSpec(schema.getDataFields(), exprPath)) {
                    addError(errors, "V-003",
                        "Unknown state path: " + exprPath,
                        "actions." + actionName);
                }
            }
        }
    }

    /**
     * Flow call 참조 존재 여부 검증
     */
    private static void validateCallReferences(DomainSchema schema, List<ValidationError> errors) {
        Set<String> actionNames = schema.getActions().keySet();
        for (Map.Entry<String, ActionSpec> entry : schema.getActions().entrySet()) {
            String actionName = entry.getKey();
            Set<String> calls = collectCalls(entry.getValue().getFlow());
            for (String callName : calls) {
                if (!actionNames.contains(callName)) {
                    addError(errors, "V-004",
                        "Unknown flow reference: \"" + callName + "\" in action \"" + actionName + "\"",
                        "actions." + actionName);
                }
            }
        }
    }

    /**
     * Flow call 그래프 순환 검증
     */
    private static void validateCallGraph(DomainSchema schema, List<ValidationError> errors) {
        Map<String, List<String>> edges = new HashMap<>();
        for (Map.Entry<String, ActionSpec> entry : schema.getActions().entrySet()) {
            edges.put(entry.getKey(), new ArrayList<>(collectCalls(entry.getValue().getFlow())));
        }

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String actionName : edges.keySet()) {
            if (!visited.contains(actionName)) {
                List<String> path = new ArrayList<>();
                path.add(actionName);
                detectCallCycle(actionName, edges, visited, recursionStack, path, errors);
            }
        }
    }

    private static boolean detectCallCycle(
        String node,
        Map<String, List<String>> edges,
        Set<String> visited,
        Set<String> recursionStack,
        List<String> path,
        List<ValidationError> errors
    ) {
        visited.add(node);
        recursionStack.add(node);

        List<String> deps = edges.getOrDefault(node, List.of());
        for (String dep : deps) {
            if (!visited.contains(dep)) {
                List<String> nextPath = new ArrayList<>(path);
                nextPath.add(dep);
                if (detectCallCycle(dep, edges, visited, recursionStack, nextPath, errors)) {
                    return true;
                }
            } else if (recursionStack.contains(dep)) {
                List<String> cycle = new ArrayList<>(path);
                cycle.add(dep);
                addError(errors, "V-005", "Cyclic call detected: " + String.join(" -> ", cycle), "actions." + node);
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    private static Set<String> collectCalls(FlowNode flow) {
        Set<String> calls = new LinkedHashSet<>();
        collectCalls(flow, calls);
        return calls;
    }

    private static void collectCalls(FlowNode flow, Set<String> calls) {
        if (flow instanceof FlowNode.Call call) {
            calls.add(call.getFlow());
            return;
        }
        if (flow instanceof FlowNode.Seq seq) {
            for (FlowNode step : seq.getSteps()) {
                collectCalls(step, calls);
            }
            return;
        }
        if (flow instanceof FlowNode.If ifFlow) {
            collectCalls(ifFlow.getThenBranch(), calls);
            if (ifFlow.getElseBranch() != null) {
                collectCalls(ifFlow.getElseBranch(), calls);
            }
        }
    }
}
