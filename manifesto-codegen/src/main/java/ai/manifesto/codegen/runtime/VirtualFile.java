package ai.manifesto.codegen.runtime;

/**
 * KR: virtual fs의 정규화된 파일 엔트리입니다.
 * EN: Normalized file entry stored in virtual filesystem.
 */
public record VirtualFile(
    String path,
    String content
) {}
