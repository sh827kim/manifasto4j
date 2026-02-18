package ai.manifesto.world.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * KR: world store 배치 실행/집계 유틸리티입니다.
 * EN: Utility for world store batch execution and aggregation.
 */
public final class StoreBatching {
    private StoreBatching() {
    }

    public static <T> StoreBatchResult apply(
        List<T> items,
        Function<T, StoreResult<?>> operation
    ) {
        Objects.requireNonNull(operation, "operation is required");
        if (items == null || items.isEmpty()) {
            return new StoreBatchResult(true, 0, 0, List.of());
        }

        int processed = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        for (T item : items) {
            StoreResult<?> result = operation.apply(item);
            if (result != null && result.isSuccess()) {
                processed += 1;
                continue;
            }
            failed += 1;
            String message = result == null ? "INTERNAL_ERROR: null store result" : formatError(result);
            errors.add(message);
        }
        return new StoreBatchResult(failed == 0, processed, failed, errors);
    }

    public static StoreBatchResult summarize(List<? extends StoreResult<?>> results) {
        if (results == null || results.isEmpty()) {
            return new StoreBatchResult(true, 0, 0, List.of());
        }
        int processed = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        for (StoreResult<?> result : results) {
            if (result != null && result.isSuccess()) {
                processed += 1;
                continue;
            }
            failed += 1;
            errors.add(result == null ? "INTERNAL_ERROR: null store result" : formatError(result));
        }
        return new StoreBatchResult(failed == 0, processed, failed, errors);
    }

    private static String formatError(StoreResult<?> result) {
        String code = result.getErrorCode() == null ? WorldErrorCode.INTERNAL_ERROR.name() : result.getErrorCode().name();
        String message = result.getError() == null ? "unknown error" : result.getError();
        return code + ": " + message;
    }
}
