package ai.manifesto.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Result 모나드 테스트")
class ResultTest {

    @Test
    @DisplayName("Result.ok() 생성 및 조회")
    void testResultOk() {
        Result<String, Integer> result = Result.ok("success");

        assertTrue(result.isOk());
        assertFalse(result.isErr());
        assertEquals("success", result.unwrap());
    }

    @Test
    @DisplayName("Result.err() 생성 및 조회")
    void testResultErr() {
        Result<String, Integer> result = Result.err(404);

        assertFalse(result.isOk());
        assertTrue(result.isErr());

        // Err 인스턴스에서 error() 메서드로 값 추출
        if (result instanceof Result.Err<?, ?> err) {
            assertEquals(404, err.error());
        }
    }

    @Test
    @DisplayName("Result.ok()에서 unwrap() 호출 시 예외")
    void testResultOkUnwrap() {
        Result<String, Integer> result = Result.ok("value");
        assertEquals("value", result.unwrap());
    }

    @Test
    @DisplayName("Result.err()에서 unwrap() 호출 시 예외")
    void testResultErrUnwrap() {
        Result<String, Integer> result = Result.err(500);
        assertThrows(IllegalStateException.class, result::unwrap);
    }

    @Test
    @DisplayName("Result map 변환")
    void testResultMap() {
        Result<Integer, String> result = Result.ok(10);
        Result<Integer, String> mapped = result.map(v -> v * 2);

        assertTrue(mapped.isOk());
        assertEquals(20, mapped.unwrap());
    }

    @Test
    @DisplayName("Result map with error")
    void testResultMapError() {
        Result<Integer, String> result = Result.err("error");
        Result<Integer, String> mapped = result.map(v -> v * 2);

        assertTrue(mapped.isErr());
    }

    @Test
    @DisplayName("Result mapErr 변환")
    void testResultMapErr() {
        Result<Integer, String> result = Result.err("not found");
        Result<Integer, Integer> mapped = result.mapErr(err -> 404);

        assertTrue(mapped.isErr());
        if (mapped instanceof Result.Err<?, ?> err) {
            assertEquals(404, err.error());
        }
    }

    @Test
    @DisplayName("Result 타입 패턴 매칭 - Ok")
    void testResultPatternMatchingOk() {
        Result<Integer, String> result = Result.ok(42);

        String output;
        if (result instanceof Result.Ok<Integer, String> ok) {
            output = "Got: " + ok.value();
        } else {
            output = "Error";
        }

        assertEquals("Got: 42", output);
    }

    @Test
    @DisplayName("Result 타입 패턴 매칭 - Err")
    void testResultPatternMatchingErr() {
        Result<Integer, String> result = Result.err("not found");

        String output;
        if (result instanceof Result.Err<Integer, String> err) {
            output = "Error: " + err.error();
        } else {
            output = "Success";
        }

        assertEquals("Error: not found", output);
    }

    @Test
    @DisplayName("여러 map 체이닝")
    void testResultMapChaining() {
        Result<Integer, String> result = Result.<Integer, String>ok(10)
            .map(v -> v * 2)
            .map(v -> v + 5);

        assertTrue(result.isOk());
        assertEquals(25, result.unwrap());
    }

    @Test
    @DisplayName("map 체인에서 에러 전파")
    void testResultMapErrorPropagation() {
        Result<Integer, String> result = Result.err("failed");
        Result<Integer, String> mapped = result.map(v -> v * 2).map(v -> v + 1);

        assertTrue(mapped.isErr());
    }

    @Test
    @DisplayName("Ok/Err 구분")
    void testResultDistinction() {
        Result<String, String> ok = Result.ok("success");
        Result<String, String> err = Result.err("failure");

        assertTrue(ok.isOk());
        assertFalse(ok.isErr());

        assertFalse(err.isOk());
        assertTrue(err.isErr());
    }
}
