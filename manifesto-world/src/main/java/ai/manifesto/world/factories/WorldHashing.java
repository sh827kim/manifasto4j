package ai.manifesto.world.factories;

import ai.manifesto.core.ErrorValue;
import ai.manifesto.core.Requirement;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.utils.CanonicalUtils;
import ai.manifesto.core.utils.HashUtils;
import ai.manifesto.world.schema.WorldId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WorldHashing {
    private static final String PLATFORM_NAMESPACE_PREFIX = "$";

    private WorldHashing() {
    }

    public static String computeSnapshotHash(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot is required");
        Map<String, Object> hashInput = buildSnapshotHashInput(snapshot);
        return HashUtils.sha256(CanonicalUtils.toCanonical(hashInput));
    }

    public static WorldId computeWorldId(String schemaHash, String snapshotHash) {
        Objects.requireNonNull(schemaHash, "schemaHash is required");
        Objects.requireNonNull(snapshotHash, "snapshotHash is required");

        Map<String, Object> worldInput = new LinkedHashMap<>();
        worldInput.put("schemaHash", schemaHash);
        worldInput.put("snapshotHash", snapshotHash);

        String hash = HashUtils.sha256(CanonicalUtils.toCanonical(worldInput));
        return WorldId.of(hash);
    }

    static Map<String, Object> buildSnapshotHashInput(Snapshot snapshot) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", stripPlatformNamespaces(snapshot.getData()));

        Map<String, Object> system = new LinkedHashMap<>();
        system.put("terminalStatus", deriveTerminalStatusForHash(snapshot));
        system.put("errors", normalizeErrors(snapshot));
        system.put("pendingDigest", computePendingDigest(snapshot.getSystem().getPendingRequirements()));

        result.put("system", system);
        return result;
    }

    private static boolean isPlatformNamespace(String key) {
        return key != null && key.startsWith(PLATFORM_NAMESPACE_PREFIX);
    }

    private static Map<String, Object> stripPlatformNamespaces(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return Map.of();
        }

        boolean hasPlatformNamespace = false;
        for (String key : data.keySet()) {
            if (isPlatformNamespace(key)) {
                hasPlatformNamespace = true;
                break;
            }
        }

        if (!hasPlatformNamespace) {
            return data;
        }

        Map<String, Object> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!isPlatformNamespace(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private static String deriveTerminalStatusForHash(Snapshot snapshot) {
        if (snapshot.getSystem().getLastError() != null) {
            return "failed";
        }
        if (!snapshot.getSystem().getPendingRequirements().isEmpty()) {
            return "failed";
        }
        return "completed";
    }

    private static List<Map<String, Object>> normalizeErrors(Snapshot snapshot) {
        List<ErrorValue> errors = snapshot.getSystem().getErrors();
        if (errors == null || errors.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> signatures = new ArrayList<>();
        for (ErrorValue error : errors) {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("actionId", error.getSource() != null ? error.getSource().getActionId() : null);
            source.put("nodePath", error.getSource() != null ? error.getSource().getNodePath() : null);

            Map<String, Object> signature = new LinkedHashMap<>();
            signature.put("code", error.getCode());
            signature.put("source", source);
            signatures.add(signature);
        }

        signatures.sort(Comparator.comparing(sig -> HashUtils.sha256(CanonicalUtils.toCanonical(sig))));
        return signatures;
    }

    private static String computePendingDigest(List<Requirement> pendingRequirements) {
        if (pendingRequirements == null || pendingRequirements.isEmpty()) {
            return "empty";
        }

        List<String> pendingIds = new ArrayList<>();
        for (Requirement pendingRequirement : pendingRequirements) {
            pendingIds.add(pendingRequirement.getId());
        }
        pendingIds.sort(String::compareTo);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("pendingIds", pendingIds);
        return HashUtils.sha256(CanonicalUtils.toCanonical(input));
    }
}
