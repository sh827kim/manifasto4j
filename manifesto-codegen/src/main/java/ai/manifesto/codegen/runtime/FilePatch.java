package ai.manifesto.codegen.runtime;

/**
 * KR: virtual fs 적용 단위 patch 모델입니다.
 * EN: Patch model applied to virtual filesystem.
 */
public record FilePatch(
    FilePatchOperation operation,
    String path,
    String content
) {
    public static FilePatch set(String path, String content) {
        return new FilePatch(FilePatchOperation.SET, path, content == null ? "" : content);
    }

    public static FilePatch delete(String path) {
        return new FilePatch(FilePatchOperation.DELETE, path, null);
    }
}
