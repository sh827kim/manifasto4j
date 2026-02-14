package ai.manifesto.codegen.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathSafetyTest {

    @Test
    void validatePathAcceptsAndNormalizesValidInputs() {
        assertEquals("types.ts", PathSafety.validatePath("types.ts").normalized());
        assertEquals("dir/sub/file.ts", PathSafety.validatePath("dir//sub///file.ts").normalized());
        assertEquals("dir/file.ts", PathSafety.validatePath("dir\\file.ts").normalized());
        assertEquals("dir/file.ts", PathSafety.validatePath("./dir/file.ts").normalized());
        assertEquals("dir/sub", PathSafety.validatePath("dir/sub/").normalized());
    }

    @Test
    void validatePathRejectsUnsafePaths() {
        assertFalse(PathSafety.validatePath("").valid());
        assertFalse(PathSafety.validatePath("file\0.ts").valid());
        assertFalse(PathSafety.validatePath("/etc/passwd").valid());
        assertFalse(PathSafety.validatePath("C:\\Users\\file.ts").valid());
        assertFalse(PathSafety.validatePath("../escape.ts").valid());
        assertFalse(PathSafety.validatePath("dir/../escape.ts").valid());
    }
}
