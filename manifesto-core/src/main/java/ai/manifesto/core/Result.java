package ai.manifesto.core;

/**
 * Result<T, E> - 함수형 에러 처리
 * 예외를 던지지 않고 성공 또는 실패를 값으로 표현한다.
 *
 * Manifesto의 핵심 원칙: 에러는 값이다.
 */
public sealed class Result<T, E> {

    /**
     * 성공 케이스
     */
    public static final class Ok<T, E> extends Result<T, E> {
        private final T value;

        public Ok(T value) {
            this.value = value;
        }

        public T value() {
            return value;
        }
    }

    /**
     * 실패 케이스
     */
    public static final class Err<T, E> extends Result<T, E> {
        private final E error;

        public Err(E error) {
            this.error = error;
        }

        public E error() {
            return error;
        }
    }

    /**
     * Ok 값을 생성한다.
     */
    public static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    /**
     * Err 값을 생성한다.
     */
    public static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }

    /**
     * 성공 여부를 확인한다.
     */
    public boolean isOk() {
        return this instanceof Ok;
    }

    /**
     * 실패 여부를 확인한다.
     */
    public boolean isErr() {
        return this instanceof Err;
    }

    /**
     * Ok 값을 추출한다. (위험: 실패 케이스에서 예외 던짐)
     */
    public T unwrap() {
        if (this instanceof Ok<T, E> ok) {
            return ok.value;
        }
        throw new IllegalStateException("Called unwrap on Err");
    }

    /**
     * 성공 케이스를 매핑한다.
     */
    public <U> Result<U, E> map(Function<T, U> f) {
        if (this instanceof Ok<T, E> ok) {
            return Result.ok(f.apply(ok.value));
        }
        @SuppressWarnings("unchecked")
        Err<T, E> err = (Err<T, E>) this;
        return Result.err(err.error);
    }

    /**
     * 실패 케이스를 매핑한다.
     */
    public <F> Result<T, F> mapErr(Function<E, F> f) {
        if (this instanceof Err<T, E> err) {
            return Result.err(f.apply(err.error));
        }
        @SuppressWarnings("unchecked")
        Ok<T, E> ok = (Ok<T, E>) this;
        return Result.ok(ok.value);
    }

    /**
     * 함수형 인터페이스
     * Java 17+ java.util.function.Function을 사용해도 되지만
     * 이 프로젝트에서는 간단함을 위해 정의함
     */
    @FunctionalInterface
    public interface Function<T, U> {
        U apply(T t);
    }

    /**
     * 값을 Ok에 래핑 (타입 추론 헬퍼)
     * 컴파일러가 타입을 추론하도록 돕는 메서드
     */
    public static <T> Result<T, Void> okValue(T value) {
        return ok(value);
    }

    /**
     * 에러를 Err에 래핑 (타입 추론 헬퍼)
     */
    public static <E> Result<Void, E> errValue(E error) {
        return err(error);
    }
}
