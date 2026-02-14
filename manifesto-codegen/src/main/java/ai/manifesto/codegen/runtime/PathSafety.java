package ai.manifesto.codegen.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: codegen 산출물 경로를 검증하고 POSIX 상대경로로 정규화합니다.
 * EN: Validates codegen output paths and normalizes into POSIX relative paths.
 */
public final class PathSafety {
    private PathSafety() {
    }

    public static PathValidationResult validatePath(String path) {
        if (path == null || path.isEmpty()) {
            return PathValidationResult.failure("Path must not be empty");
        }
        if (path.indexOf('\0') >= 0) {
            return PathValidationResult.failure("Path must not contain null bytes");
        }

        String normalized = path.replace('\\', '/');
        if (normalized.matches("^[A-Za-z]:.*")) {
            return PathValidationResult.failure("Path must not contain drive letters");
        }
        if (normalized.startsWith("/")) {
            return PathValidationResult.failure("Path must be relative, not absolute");
        }

        normalized = normalized.replaceAll("/{2,}", "/");
        while (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }
        if (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.isEmpty()) {
            return PathValidationResult.failure("Path resolves to empty after normalization");
        }

        String[] rawSegments = normalized.split("/");
        List<String> safeSegments = new ArrayList<>();
        for (String segment : rawSegments) {
            if (segment.isEmpty() || ".".equals(segment)) {
                continue;
            }
            if ("..".equals(segment)) {
                return PathValidationResult.failure("Path must not contain '..' traversal");
            }
            safeSegments.add(segment);
        }

        if (safeSegments.isEmpty()) {
            return PathValidationResult.failure("Path resolves to empty after normalization");
        }

        return PathValidationResult.success(String.join("/", safeSegments));
    }
}
