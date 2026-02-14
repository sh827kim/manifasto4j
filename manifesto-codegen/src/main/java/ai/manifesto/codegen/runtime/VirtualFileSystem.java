package ai.manifesto.codegen.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * KR: patch 충돌 규칙을 적용해 산출물을 합성하는 in-memory virtual fs입니다.
 * EN: In-memory virtual fs that composes outputs with patch collision rules.
 */
public final class VirtualFileSystem {
    private final Map<String, FileEntry> files = new HashMap<>();
    private final Set<String> deletedPaths = new HashSet<>();

    public CodegenDiagnostic applyPatch(FilePatch patch, String pluginId) {
        if (patch == null) {
            return CodegenDiagnostic.error(pluginId, "Patch must not be null");
        }
        if (patch.operation() == FilePatchOperation.SET) {
            return applySet(patch.path(), patch.content(), pluginId);
        }
        return applyDelete(patch.path(), pluginId);
    }

    public List<VirtualFile> getFiles() {
        return files.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
            .map(entry -> new VirtualFile(entry.getKey(), entry.getValue().content))
            .toList();
    }

    public boolean has(String path) {
        return files.containsKey(path);
    }

    private CodegenDiagnostic applySet(String path, String content, String pluginId) {
        FileEntry existing = files.get(path);
        if (existing != null) {
            if (existing.sourcePlugin.equals(pluginId)) {
                return CodegenDiagnostic.error(pluginId, "Duplicate set on \"" + path + "\" within same plugin");
            }
            return CodegenDiagnostic.error(
                pluginId,
                "File \"" + path + "\" already set by plugin \"" + existing.sourcePlugin + "\""
            );
        }

        deletedPaths.remove(path);
        files.put(path, new FileEntry(content == null ? "" : content, pluginId));
        return null;
    }

    private CodegenDiagnostic applyDelete(String path, String pluginId) {
        FileEntry existing = files.get(path);
        if (existing == null && !deletedPaths.contains(path)) {
            deletedPaths.add(path);
            return CodegenDiagnostic.warn(pluginId, "Delete on nonexistent path \"" + path + "\"");
        }

        if (existing != null) {
            files.remove(path);
            deletedPaths.add(path);
            return CodegenDiagnostic.warn(
                pluginId,
                "File \"" + path + "\" set by \"" + existing.sourcePlugin + "\" is deleted by \"" + pluginId + "\""
            );
        }

        deletedPaths.add(path);
        return null;
    }

    private record FileEntry(String content, String sourcePlugin) {
    }
}
