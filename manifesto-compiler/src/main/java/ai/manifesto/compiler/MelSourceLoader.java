package ai.manifesto.compiler;

import ai.manifesto.core.schema.DomainSchema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * KR: MEL 텍스트를 파일/클래스패스에서 로드하고 compiler facade로 컴파일하는 유틸입니다.
 * EN: Utility that loads MEL text from file/classpath and compiles through compiler facade.
 */
public final class MelSourceLoader {

    public String loadFromFile(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new IllegalArgumentException("Failed to read MEL file: " + path, error);
        }
    }

    public String loadFromClasspath(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = MelSourceLoader.class.getClassLoader();
        }

        try (InputStream inputStream = classLoader.getResourceAsStream(normalized)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Classpath MEL resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new IllegalArgumentException("Failed to read MEL classpath resource: " + resourcePath, error);
        }
    }

    public CompilationResult compileFromFile(CompilerFacade compiler, Path path, CompileDomainOptions options) {
        Objects.requireNonNull(compiler, "compiler must not be null");
        String mel = loadFromFile(path);
        return compiler.compileDomain(mel, options == null ? new CompileDomainOptions(null) : options);
    }

    public CompilationResult compileFromFile(CompilerFacade compiler, Path path) {
        return compileFromFile(compiler, path, new CompileDomainOptions(null));
    }

    public DomainSchema compileFromClasspathOrThrow(CompilerFacade compiler, String resourcePath, CompileDomainOptions options) {
        Objects.requireNonNull(compiler, "compiler must not be null");
        String mel = loadFromClasspath(resourcePath);
        return compiler.compileDomainOrThrow(mel, options == null ? new CompileDomainOptions(null) : options);
    }
}
