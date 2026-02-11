package ai.manifesto.core.core;

import ai.manifesto.core.ErrorValue;
import ai.manifesto.core.HostContext;
import ai.manifesto.core.Patch;
import ai.manifesto.core.Result;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.evaluator.ComputedEvaluator;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.utils.PathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Apply - Snapshot에 Patch 배열을 순차적으로 적용
 *
 * TS core/apply.ts와 동일한 흐름:
 * 1) patch 적용 (data/system/input)
 * 2) computed 재계산
 * 3) meta(version/timestamp/randomSeed) 갱신
 *
 * computed/meta patch는 무시된다.
 */
public class Apply {
    private static final String PLATFORM_NAMESPACE_PREFIX = "$";

    private Apply() {
        // 정적 메서드만 제공
    }

    public static Result<Snapshot, ErrorValue> apply(
        DomainSchema schema,
        Snapshot snapshot,
        List<Patch> patches,
        HostContext context
    ) {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(snapshot, "snapshot is required");
        Objects.requireNonNull(patches, "patches is required");

        HostContext ctx = context != null ? context : HostContext.forSnapshot(snapshot);

        Map<String, Object> newData = snapshot.getData();
        SystemState newSystem = snapshot.getSystem();
        Map<String, Object> newInput = snapshot.getInput();

        List<ErrorValue> validationErrors = new ArrayList<>();

        boolean validateDataPaths = !schema.getDataFields().isEmpty();

        for (Patch patch : patches) {
            PatchPath path = splitPatchPath(patch.getPath());
            switch (path.root) {
                case DATA -> {
                    String normalized = path.subPath;
                    String platformNamespace = getPlatformNamespace(normalized);
                    if (validateDataPaths
                        && platformNamespace == null
                        && !ValidationUtils.pathExistsInStateSpec(schema.getDataFields(), normalized)) {
                        validationErrors.add(ErrorValue.create(
                            "PATH_NOT_FOUND",
                            "Unknown patch path: " + patch.getPath(),
                            snapshot.getSystem().getCurrentAction(),
                            patch.getPath(),
                            ctx.getNow()
                        ));
                        break;
                    }

                    if (!(patch instanceof Patch.Unset)) {
                        Object value = null;
                        if (patch instanceof Patch.Set set) {
                            value = set.getValue();
                        } else if (patch instanceof Patch.Merge merge) {
                            value = merge.getValue();
                        }
                        if (platformNamespace != null
                            && normalized.equals(platformNamespace)
                            && !isPlatformRootValueValid(value)) {
                            validationErrors.add(ErrorValue.create(
                                "TYPE_MISMATCH",
                                "Invalid patch value at " + patch.getPath(),
                                snapshot.getSystem().getCurrentAction(),
                                patch.getPath(),
                                ctx.getNow()
                            ));
                            break;
                        }
                        if (patch instanceof Patch.Merge && !isMergeTargetCompatible(newData, normalized)) {
                            validationErrors.add(ErrorValue.create(
                                "TYPE_MISMATCH",
                                "Invalid merge target at " + patch.getPath() + ": target path must be an object or absent",
                                snapshot.getSystem().getCurrentAction(),
                                patch.getPath(),
                                ctx.getNow()
                            ));
                            break;
                        }
                        FieldSpec spec = getRootFieldSpec(schema, normalized);
                        boolean isNested = normalized != null && normalized.contains(".");
                        if (platformNamespace == null
                            && spec != null
                            && !isNested &&
                            !validateValueAgainstFieldSpec(value, spec, patch instanceof Patch.Merge)) {
                            validationErrors.add(ErrorValue.create(
                                "TYPE_MISMATCH",
                                "Invalid patch value at " + patch.getPath(),
                                snapshot.getSystem().getCurrentAction(),
                                patch.getPath(),
                                ctx.getNow()
                            ));
                            break;
                        }
                    }

                    newData = applyPatchToMap(newData, patch, normalized);
                }
                case SYSTEM -> newSystem = applyPatchToSystem(newSystem, patch, path.subPath);
                case INPUT -> newInput = applyPatchToMap(newInput, patch, path.subPath);
                case COMPUTED, META -> {
                    // core-owned; ignore
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            SystemState updated = newSystem.withStatus(SystemState.Status.ERROR);
            for (ErrorValue error : validationErrors) {
                updated = updated.withError(error);
            }
            newSystem = updated;
        }

        Snapshot intermediate = snapshot.copy(newData, snapshot.getComputed(), newSystem, newInput, snapshot.getMeta());

        Result<Map<String, Object>, ErrorValue> computedResult =
            ComputedEvaluator.evaluateComputed(schema, intermediate);
        Map<String, Object> computed = snapshot.getComputed();
        if (computedResult.isOk()) {
            computed = computedResult.unwrap();
        } else if (computedResult instanceof Result.Err<?, ?> err) {
            @SuppressWarnings("unchecked")
            ErrorValue error = (ErrorValue) err.error();
            computed = new HashMap<>();
            newSystem = newSystem.withStatus(SystemState.Status.ERROR).withError(error);
        }

        Snapshot.SnapshotMeta newMeta = Snapshot.SnapshotMeta.create(
            snapshot.getMeta().getVersion() + 1,
            ctx.getNow(),
            ctx.getRandomSeed(),
            snapshot.getMeta().getSchemaHash()
        );

        Snapshot next = snapshot.copy(newData, computed, newSystem, newInput, newMeta);
        return Result.ok(next);
    }

    public static Result<Snapshot, ErrorValue> apply(
        DomainSchema schema,
        Snapshot snapshot,
        List<Patch> patches
    ) {
        return apply(schema, snapshot, patches, null);
    }

    public static Result<Snapshot, ErrorValue> apply(
        DomainSchema schema,
        Snapshot snapshot,
        Patch patch
    ) {
        return apply(schema, snapshot, List.of(patch), null);
    }

    public static Result<Snapshot, ErrorValue> apply(
        DomainSchema schema,
        Snapshot snapshot,
        Patch... patches
    ) {
        return apply(schema, snapshot, Arrays.asList(patches), null);
    }

    // Legacy overloads (no schema/host context)
    public static Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        List<Patch> patches
    ) {
        return apply(DomainSchema.empty(), snapshot, patches, null);
    }

    public static Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        Patch patch
    ) {
        return apply(DomainSchema.empty(), snapshot, List.of(patch), null);
    }

    public static Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        Patch... patches
    ) {
        return apply(DomainSchema.empty(), snapshot, Arrays.asList(patches), null);
    }

    private enum PatchRoot { DATA, SYSTEM, INPUT, COMPUTED, META }

    private record PatchPath(PatchRoot root, String subPath) { }

    private static PatchPath splitPatchPath(String path) {
        if (path == null) {
            return new PatchPath(PatchRoot.DATA, "");
        }
        if (path.equals("system") || path.startsWith("system.")) {
            return new PatchPath(PatchRoot.SYSTEM, path.equals("system") ? "" : path.substring(7));
        }
        if (path.equals("input") || path.startsWith("input.")) {
            return new PatchPath(PatchRoot.INPUT, path.equals("input") ? "" : path.substring(6));
        }
        if (path.equals("computed") || path.startsWith("computed.")) {
            return new PatchPath(PatchRoot.COMPUTED, path.equals("computed") ? "" : path.substring(9));
        }
        if (path.equals("meta") || path.startsWith("meta.")) {
            return new PatchPath(PatchRoot.META, path.equals("meta") ? "" : path.substring(5));
        }
        return new PatchPath(PatchRoot.DATA, path);
    }

    private static String getPlatformNamespace(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        int dotIndex = path.indexOf('.');
        String root = dotIndex < 0 ? path : path.substring(0, dotIndex);
        if (root.startsWith(PLATFORM_NAMESPACE_PREFIX)) {
            return root;
        }
        return null;
    }

    private static boolean isPlatformRootValueValid(Object value) {
        return value == null || value instanceof Map<?, ?>;
    }

    private static boolean isMergeTargetCompatible(Object root, String path) {
        if (path == null || path.isEmpty()) {
            return root instanceof Map<?, ?>;
        }

        String[] segments = path.split("\\.");
        Object current = root;

        for (String segment : segments) {
            if (!(current instanceof Map<?, ?> map)) {
                return false;
            }

            if (!map.containsKey(segment)) {
                // Path is absent; merge can create missing object chain.
                return true;
            }
            current = map.get(segment);
        }

        return current instanceof Map<?, ?>;
    }

    private static Map<String, Object> applyPatchToMap(
        Map<String, Object> base,
        Patch patch,
        String subPath
    ) {
        Object result;
        if (patch instanceof Patch.Set set) {
            result = PathUtils.setByPath(base, subPath, set.getValue());
        } else if (patch instanceof Patch.Unset) {
            result = PathUtils.unsetByPath(base, subPath);
        } else if (patch instanceof Patch.Merge merge) {
            result = PathUtils.mergeByPath(base, subPath, merge.getValue());
        } else {
            result = base;
        }
        if (result instanceof Map<?, ?> mapResult) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) mapResult;
            return cast;
        }
        return base;
    }

    private static SystemState applyPatchToSystem(SystemState system, Patch patch, String subPath) {
        Map<String, Object> map = systemToMap(system);
        Object result;
        if (patch instanceof Patch.Set set) {
            result = PathUtils.setByPath(map, subPath, set.getValue());
        } else if (patch instanceof Patch.Unset) {
            result = PathUtils.unsetByPath(map, subPath);
        } else if (patch instanceof Patch.Merge merge) {
            result = PathUtils.mergeByPath(map, subPath, merge.getValue());
        } else {
            result = map;
        }
        if (result instanceof Map<?, ?> mapResult) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) mapResult;
            return mapToSystemState(system, cast);
        }
        return system;
    }

    private static Map<String, Object> systemToMap(SystemState system) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", system.getStatus().name().toLowerCase());
        map.put("lastError", system.getLastError());
        map.put("errors", system.getErrors());
        map.put("pendingRequirements", system.getPendingRequirements());
        map.put("currentAction", system.getCurrentAction());
        return map;
    }

    private static SystemState mapToSystemState(SystemState base, Map<String, Object> map) {
        SystemState.Status status = base.getStatus();
        Object statusValue = map.get("status");
        if (statusValue instanceof SystemState.Status s) {
            status = s;
        } else if (statusValue instanceof String s) {
            try {
                status = SystemState.Status.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // keep base
            }
        }

        ErrorValue lastError = base.getLastError();
        Object lastErrorValue = map.get("lastError");
        if (lastErrorValue instanceof ErrorValue ev) {
            lastError = ev;
        }

        List<ErrorValue> errors = base.getErrors();
        Object errorsValue = map.get("errors");
        if (errorsValue instanceof List<?> list) {
            List<ErrorValue> cast = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof ErrorValue ev) {
                    cast.add(ev);
                }
            }
            errors = cast;
        }

        List<?> pending = base.getPendingRequirements();
        Object pendingValue = map.get("pendingRequirements");
        if (pendingValue instanceof List<?> list) {
            pending = list;
        }

        String currentAction = base.getCurrentAction();
        Object currentValue = map.get("currentAction");
        if (currentValue instanceof String s) {
            currentAction = s;
        } else if (currentValue == null) {
            currentAction = null;
        }

        return new SystemStateBuilder()
            .status(status)
            .lastError(lastError)
            .errors(errors)
            .pendingRequirements(pending)
            .currentAction(currentAction)
            .build();
    }

    private static FieldSpec getRootFieldSpec(DomainSchema schema, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String root = path.contains(".") ? path.substring(0, path.indexOf('.')) : path;
        return schema.getDataField(root);
    }

    private static boolean validateValueAgainstFieldSpec(Object value, FieldSpec spec, boolean allowPartial) {
        if (value == null) {
            return !spec.isRequired();
        }
        String type = spec.getType();
        return switch (type) {
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            case "integer" -> value instanceof Integer || value instanceof Long;
            case "boolean" -> value instanceof Boolean;
            case "array" -> value instanceof List<?>;
            case "object" -> value instanceof Map<?, ?>;
            default -> allowPartial || true;
        };
    }

    private static final class SystemStateBuilder {
        private SystemState.Status status;
        private ErrorValue lastError;
        private List<ErrorValue> errors;
        private List<?> pendingRequirements;
        private String currentAction;

        public SystemStateBuilder status(SystemState.Status status) {
            this.status = status;
            return this;
        }

        public SystemStateBuilder lastError(ErrorValue lastError) {
            this.lastError = lastError;
            return this;
        }

        public SystemStateBuilder errors(List<ErrorValue> errors) {
            this.errors = errors;
            return this;
        }

        public SystemStateBuilder pendingRequirements(List<?> pendingRequirements) {
            this.pendingRequirements = pendingRequirements;
            return this;
        }

        public SystemStateBuilder currentAction(String currentAction) {
            this.currentAction = currentAction;
            return this;
        }

        @SuppressWarnings("unchecked")
        public SystemState build() {
            List<ai.manifesto.core.Requirement> reqs = new ArrayList<>();
            if (pendingRequirements != null) {
                for (Object item : pendingRequirements) {
                    if (item instanceof ai.manifesto.core.Requirement req) {
                        reqs.add(req);
                    }
                }
            }
            return SystemState.of(status, lastError, errors, reqs, currentAction);
        }
    }
}
