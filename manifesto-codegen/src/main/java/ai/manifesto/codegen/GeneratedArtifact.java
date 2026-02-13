package ai.manifesto.codegen;

/**
 * KR: 생성된 단일 파일 산출물(상대 경로 + 내용)입니다.
 * EN: Single generated file artifact (relative path + content).
 */
public record GeneratedArtifact(String relativePath, String content) {}
