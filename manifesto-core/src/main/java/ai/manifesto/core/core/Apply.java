package ai.manifesto.core.core;

import ai.manifesto.core.*;
import ai.manifesto.core.utils.PathUtils;

import java.util.*;

/**
 * Apply - Snapshot에 Patch 배열을 순차적으로 적용
 *
 * Manifesto의 상태 변경은 모두 Apply를 통해 이루어진다.
 * 세 가지 불변 원칙:
 * 1. 결정론적: 같은 Patch 배열 + 같은 Snapshot → 항상 같은 결과
 * 2. 불변성: 원본 Snapshot은 절대 변경되지 않음 (새 Snapshot 반환)
 * 3. 순차적: Patch는 입력 순서대로 정확히 적용됨
 *
 * 세 가지 연산:
 * - SET: 경로의 값 설정 (없으면 생성)
 * - UNSET: 경로의 속성 제거
 * - MERGE: 경로에서 얕은 병합 (맵만)
 */
public class Apply {

    private Apply() {
        // 정적 메서드만 제공
    }

    /**
     * Snapshot에 Patch 배열을 순차적으로 적용
     *
     * @param snapshot 원본 상태 (변경되지 않음)
     * @param patches 적용할 Patch 배열
     * @return 변경된 새로운 Snapshot, 또는 에러
     */
    public static Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        List<Patch> patches
    ) {
        Snapshot current = snapshot;

        for (int i = 0; i < patches.size(); i++) {
            Patch patch = patches.get(i);
            Result<Snapshot, ErrorValue> result = applyPatch(current, patch, i);

            if (result.isErr()) {
                return result;
            }

            current = result.unwrap();
        }

        return Result.ok(current);
    }

    /**
     * 단일 Patch 적용
     *
     * @param snapshot 현재 Snapshot
     * @param patch 적용할 Patch
     * @param index Patch 인덱스 (에러 메시지용)
     * @return 변경된 Snapshot 또는 에러
     */
    @SuppressWarnings("unchecked")
    private static Result<Snapshot, ErrorValue> applyPatch(
        Snapshot snapshot,
        Patch patch,
        int index
    ) {
        try {
            // Snapshot 전체 구조를 PathUtils에 전달
            Map<String, Object> snapshotMap = new HashMap<>();
            snapshotMap.put("data", new HashMap<>(snapshot.getData()));
            snapshotMap.put("computed", new HashMap<>(snapshot.getComputed()));
            snapshotMap.put("system", snapshot.getSystem());
            snapshotMap.put("input", new HashMap<>(snapshot.getInput()));

            Object result = null;

            if (patch instanceof Patch.Set setPatch) {
                // SET: 경로에 값 설정
                result = PathUtils.setByPath(snapshotMap, setPatch.getPath(), setPatch.getValue());
            } else if (patch instanceof Patch.Unset unsetPatch) {
                // UNSET: 경로 제거
                result = PathUtils.unsetByPath(snapshotMap, unsetPatch.getPath());
            } else if (patch instanceof Patch.Merge mergePatch) {
                // MERGE: 얕은 병합
                result = PathUtils.mergeByPath(snapshotMap, mergePatch.getPath(), mergePatch.getValue());
            } else {
                // 알 수 없는 Patch 타입
                return Result.err(ErrorValue.create(
                    "UNKNOWN_PATCH_TYPE",
                    "Unknown patch type at index " + index + ": " + patch.getClass().getSimpleName(),
                    null,
                    null,
                    System.currentTimeMillis()
                ));
            }

            // 결과를 Map으로 변환
            if (!(result instanceof Map)) {
                return Result.err(ErrorValue.create(
                    "PATCH_TYPE_ERROR",
                    "Patch result is not a Map at index " + index + ": " + result.getClass().getSimpleName(),
                    null,
                    null,
                    System.currentTimeMillis()
                ));
            }

            Map<String, Object> resultMap = (Map<String, Object>) result;
            Map<String, Object> newData = (Map<String, Object>) resultMap.getOrDefault("data", new HashMap<>());

            Snapshot.SnapshotMeta newMeta = Snapshot.SnapshotMeta.create(
                snapshot.getMeta().getVersion() + 1,
                System.currentTimeMillis(),
                snapshot.getMeta().getRandomSeed(),
                snapshot.getMeta().getSchemaHash()
            );
            return Result.ok(snapshot.withData(newData).withMeta(newMeta));

        } catch (Exception e) {
            // 예상 밖의 예외 처리 (발생하면 안 됨)
            return Result.err(ErrorValue.create(
                "APPLY_ERROR",
                "Error applying patch at index " + index + ": " + e.getMessage(),
                null,
                null,
                System.currentTimeMillis()
            ));
        }
    }

    /**
     * 단일 Patch를 Snapshot에 적용 (간편 메서드)
     *
     * @param snapshot 현재 Snapshot
     * @param patch 적용할 Patch
     * @return 변경된 Snapshot 또는 에러
     */
    public static Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        Patch patch
    ) {
        return apply(snapshot, List.of(patch));
    }

    /**
     * 여러 Patch를 Snapshot에 적용 (가변인자 편의 메서드)
     *
     * @param snapshot 현재 Snapshot
     * @param patches 적용할 Patch들
     * @return 변경된 Snapshot 또는 에러
     */
    public static Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        Patch... patches
    ) {
        return apply(snapshot, Arrays.asList(patches));
    }
}
