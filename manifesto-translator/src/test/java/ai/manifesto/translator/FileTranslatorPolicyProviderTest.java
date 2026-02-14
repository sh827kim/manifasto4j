package ai.manifesto.translator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileTranslatorPolicyProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void providerLoadsAndReloadsPoliciesFromFile() throws Exception {
        Path policyFile = tempDir.resolve("translator-policies.properties");
        Files.writeString(
            policyFile,
            "# todo domain\n" +
                "todo.allowedActions=createTask,closeTask\n" +
                "todo.requiredContextKeys=tenantId,userId\n"
        );

        FileTranslatorPolicyProvider provider = new FileTranslatorPolicyProvider(policyFile);
        TranslatorDomainPolicy first = provider.findByDomain("todo").orElseThrow();
        assertEquals(Set.of("createTask", "closeTask"), first.allowedActions());
        assertEquals(Set.of("tenantId", "userId"), first.requiredContextKeys());

        Files.writeString(
            policyFile,
            "todo.allowedActions=createTask\n" +
                "todo.requiredContextKeys=tenantId\n"
        );
        provider.reload();

        TranslatorDomainPolicy reloaded = provider.findByDomain("todo").orElseThrow();
        assertEquals(Set.of("createTask"), reloaded.allowedActions());
        assertEquals(Set.of("tenantId"), reloaded.requiredContextKeys());
        assertTrue(provider.snapshot().containsKey("todo"));
    }
}
