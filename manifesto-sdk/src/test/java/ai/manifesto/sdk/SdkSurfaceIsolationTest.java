package ai.manifesto.sdk;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SdkSurfaceIsolationTest {
    private static final String RUNTIME_PACKAGE = "ai.manifesto.runtime";

    @Test
    void publicApiDoesNotExposeRuntimeTypes() {
        List<Class<?>> surface = List.of(
            App.class,
            AppFactory.class,
            AppConfig.class,
            MemoryProvider.class,
            MemoryVerifier.class,
            MemoryVerificationResult.class,
            StoredMemoryRecord.class,
            MemoryFacade.class,
            SystemFacade.class,
            RecallRequest.class,
            RecallResult.class,
            BackfillConfig.class,
            MemoryMaintenanceOptions.class,
            ActionHandle.class,
            ActionResult.class,
            CompletedActionResult.class,
            FailedActionResult.class,
            RejectedActionResult.class,
            PreparationFailedActionResult.class,
            ActionUpdate.class,
            AppHead.class
        );

        for (Class<?> type : surface) {
            assertNoRuntimeLeak(type.getTypeName());
            for (Method method : type.getMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                assertNoRuntimeLeak(method.getGenericReturnType().getTypeName());
                for (Type parameter : method.getGenericParameterTypes()) {
                    assertNoRuntimeLeak(parameter.getTypeName());
                }
            }
            for (Constructor<?> constructor : type.getConstructors()) {
                for (Type parameter : constructor.getGenericParameterTypes()) {
                    assertNoRuntimeLeak(parameter.getTypeName());
                }
            }
            for (Field field : type.getFields()) {
                if (!Modifier.isPublic(field.getModifiers())) {
                    continue;
                }
                assertNoRuntimeLeak(field.getGenericType().getTypeName());
            }
        }
    }

    private void assertNoRuntimeLeak(String typeName) {
        assertFalse(
            typeName.contains(RUNTIME_PACKAGE),
            () -> "SDK public surface must not expose runtime type: " + typeName
        );
    }
}
