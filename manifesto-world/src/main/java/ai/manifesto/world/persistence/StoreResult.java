package ai.manifesto.world.persistence;

public final class StoreResult<T> {
    private final boolean success;
    private final T data;
    private final String error;

    private StoreResult(boolean success, T data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> StoreResult<T> success(T data) {
        return new StoreResult<>(true, data, null);
    }

    public static <T> StoreResult<T> success() {
        return new StoreResult<>(true, null, null);
    }

    public static <T> StoreResult<T> failure(String error) {
        return new StoreResult<>(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
