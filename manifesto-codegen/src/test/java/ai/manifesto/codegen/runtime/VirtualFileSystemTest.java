package ai.manifesto.codegen.runtime;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VirtualFileSystemTest {

    @Test
    void storesAndSortsFilesDeterministically() {
        VirtualFileSystem vfs = new VirtualFileSystem();
        vfs.applyPatch(FilePatch.set("z.ts", "z"), "p1");
        vfs.applyPatch(FilePatch.set("a.ts", "a"), "p1");
        vfs.applyPatch(FilePatch.set("m.ts", "m"), "p1");

        List<VirtualFile> files = vfs.getFiles();
        assertEquals(List.of("a.ts", "m.ts", "z.ts"), files.stream().map(VirtualFile::path).toList());
    }

    @Test
    void detectsCollisionsAndWarnings() {
        VirtualFileSystem vfs = new VirtualFileSystem();
        assertNull(vfs.applyPatch(FilePatch.set("a.ts", "v1"), "p1"));

        CodegenDiagnostic duplicateSet = vfs.applyPatch(FilePatch.set("a.ts", "v2"), "p1");
        assertNotNull(duplicateSet);
        assertEquals(CodegenDiagnosticLevel.ERROR, duplicateSet.level());

        CodegenDiagnostic crossPluginSet = vfs.applyPatch(FilePatch.set("a.ts", "v2"), "p2");
        assertNotNull(crossPluginSet);
        assertEquals(CodegenDiagnosticLevel.ERROR, crossPluginSet.level());

        CodegenDiagnostic deleteWarn = vfs.applyPatch(FilePatch.delete("a.ts"), "p2");
        assertNotNull(deleteWarn);
        assertEquals(CodegenDiagnosticLevel.WARN, deleteWarn.level());

        assertNull(vfs.applyPatch(FilePatch.set("a.ts", "v3"), "p2"));
        assertTrue(vfs.has("a.ts"));

        CodegenDiagnostic nonexistentDelete = vfs.applyPatch(FilePatch.delete("nope.ts"), "p3");
        assertNotNull(nonexistentDelete);
        assertEquals(CodegenDiagnosticLevel.WARN, nonexistentDelete.level());
    }
}
