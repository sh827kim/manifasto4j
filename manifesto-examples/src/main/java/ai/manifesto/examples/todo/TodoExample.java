package ai.manifesto.examples.todo;

import ai.manifesto.core.*;
import ai.manifesto.core.core.*;
import ai.manifesto.core.expr.collection.Append;
import ai.manifesto.core.expr.collection.Filter;
import ai.manifesto.core.expr.collection.Len;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.expr.object.ObjectExpr;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.*;

import java.util.*;

/**
 * TodoExample - Todo 애플리케이션 완전한 예제
 *
 * 이 예제는 Manifesto의 완전한 흐름을 보여준다:
 * 1️⃣ 데이터 구조 시연 (Snapshot, Patch, Result 등)
 * 2️⃣ DomainSchema 정의 (스키마, 액션, 필드)
 * 3️⃣ Intent 발행 및 Compute 호출
 * 4️⃣ Patch 적용 및 검증
 * 5️⃣ 결과 처리
 *
 * 단계별로 Manifesto의 핵심 개념을 배울 수 있다.
 *
 * Phase 5 완성: 모든 핵심 엔진 구현 완료 ✅
 */
public class TodoExample {

    /**
     * 메인 메서드
     */
    public static void main(String[] args) throws Exception {
        System.out.println("\n" +
            "╔══════════════════════════════════════════╗\n" +
            "║   Manifesto Todo Application Example     ║\n" +
            "║          Phase 5 - 완전 구현             ║\n" +
            "╚══════════════════════════════════════════╝\n");

        // 📚 1단계: 기본 데이터 구조 시연
        System.out.println("📚 1단계: 기본 데이터 구조 시연");
        System.out.println("─".repeat(42));
        demonstrateDataStructures();
        System.out.println();

        // 🏗️ 2단계: DomainSchema 정의 및 생성
        System.out.println("🏗️ 2단계: DomainSchema 정의 및 생성");
        System.out.println("─".repeat(42));
        DomainSchema schema = createTodoSchema();
        System.out.println("✓ Schema 생성 완료: " + schema);
        System.out.println("  - ID: " + schema.getId());
        System.out.println("  - 액션: " + schema.getActions().size());
        System.out.println("  - 필드: " + schema.getDataFields().size());
        System.out.println();

        // 📸 3단계: 초기 Snapshot 생성
        System.out.println("📸 3단계: 초기 Snapshot 생성");
        System.out.println("─".repeat(42));
        Snapshot initialSnapshot = createInitialSnapshot(schema.getHash());
        System.out.println("✓ Snapshot 생성 완료:");
        System.out.println("  - 버전: " + initialSnapshot.getMeta().getVersion());
        System.out.println("  - Todos: " + initialSnapshot.getData().get("todos"));
        System.out.println("  - Computed: " + initialSnapshot.getComputed());
        System.out.println();

        // 📋 4단계: Intent 생성 및 Validate
        System.out.println("📋 4단계: Intent 생성 및 Validate");
        System.out.println("─".repeat(42));
        Intent intent = createIntent("addTodo", "Buy milk");
        System.out.println("✓ Intent 생성:");
        System.out.println("  - 액션: " + intent.getType());
        System.out.println("  - 입력: " + intent.getInput());
        System.out.println("  - ID: " + intent.getIntentId().substring(0, 8) + "...");

        // Validate 검증
        boolean isValid = Validate.isValid(schema, initialSnapshot);
        System.out.println("✓ Snapshot 검증: " + (isValid ? "✅ 유효" : "❌ 무효"));
        System.out.println();

        // 🔧 5단계: Compute 호출 (동기식)
        System.out.println("🔧 5단계: Compute 호출");
        System.out.println("─".repeat(42));
        ComputeResult result = ManifestoCore.getInstance().computeSync(
            schema,
            initialSnapshot,
            intent,
            5  // 5초 타임아웃
        );
        System.out.println("✓ Compute 완료:");
        System.out.println("  - 상태: " + result.getStatus());
        System.out.println("  - 요구사항: " + result.getRequirements().size());
        System.out.println("  - Todos: " + result.getSnapshot().getData().get("todos"));
        System.out.println("  - Computed: " + result.getSnapshot().getComputed());
        System.out.println();

        // 🔨 6단계: Patch 적용
        System.out.println("🔨 6단계: Patch 적용");
        System.out.println("─".repeat(42));
        demonstrateApply();
        System.out.println();

        // ✅ 7단계: 완료
        System.out.println("✅ 예제 완료!");
        System.out.println("═".repeat(42));
        System.out.println("\n📊 Manifesto 주요 개념:");
        System.out.println("  1. Snapshot: 불변 상태 저장소");
        System.out.println("  2. Patch: 상태 변경 (SET, UNSET, MERGE)");
        System.out.println("  3. Intent: 액션 요청");
        System.out.println("  4. DomainSchema: 도메인 정의 (액션, 필드)");
        System.out.println("  5. Compute: 상태 전환 엔진");
        System.out.println("  6. Result: 함수형 에러 처리");
        System.out.println("  7. Validate: 스키마 검증");
        System.out.println("\n");
    }

    /**
     * 데이터 구조 시연
     */
    private static void demonstrateDataStructures() {
        // 1. Result 모나드
        Result<String, Integer> ok = Result.ok("성공!");
        System.out.println("✓ Result.ok: " + ok.unwrap());

        // 2. Patch 3가지 종류
        Patch setPatch = Patch.set("data.count", 42);
        Patch unsetPatch = Patch.unset("data.user");
        Patch mergePatch = Patch.merge("data.metadata", Map.of("updated", true));
        System.out.println("✓ Patch 3가지: SET, UNSET, MERGE");

        // 3. Intent
        Intent intent = new Intent("addTodo", Map.of("title", "Learn Manifesto"), "intent_123");
        System.out.println("✓ Intent: " + intent.getType());

        // 4. SystemState
        SystemState state = SystemState.initial();
        System.out.println("✓ SystemState: " + state.getStatus());

        // 5. ComputeResult
        Snapshot snapshot = Snapshot.initial();
        ComputeResult result = ComputeResult.complete(snapshot, null);
        System.out.println("✓ ComputeResult: " + result.getStatus());
    }

    /**
     * Todo 스키마 생성
     */
    private static DomainSchema createTodoSchema() {
        // 필드 정의
        FieldSpec todosField = new FieldSpec("todos", "array", false, new ArrayList<>());
        FieldSpec filterField = new FieldSpec("filter", "string", false, "all");

        // addTodo 액션 정의 (data.todos에 새로운 항목 추가)
        ActionSpec addTodoAction = new ActionSpec.Builder("addTodo")
            .addInputField("title", FieldSpec.required("title", "string"))
            .flow(FlowNode.Seq.of(List.of(
                FlowNode.Patch.set(
                    "data.todos",
                    Append.of(
                        new Get("data.todos"),
                        ObjectExpr.of(Map.of(
                            "id", new Get("$system.uuid"),
                            "title", new Get("input.title"),
                            "completed", new Lit(false)
                        ))
                    )
                ),
                FlowNode.Halt.of("added")
            )))
            .build();

        // computed.totalCount = len(data.todos)
        ComputedFieldDef totalCount = new ComputedFieldDef.Builder(
            "totalCount",
            new Len(new Get("data.todos"))
        ).addDependency("data.todos").build();

        // computed.completedCount = len(filter(data.todos, $item.completed))
        ComputedFieldDef completedCount = new ComputedFieldDef.Builder(
            "completedCount",
            new Len(new Filter(new Get("data.todos"), new Get("$item.completed")))
        ).addDependency("data.todos").build();

        DomainSchema.Builder builder = new DomainSchema.Builder("todo-app", "1.0.0")
            .addAction(addTodoAction)
            .addDataField(todosField)
            .addDataField(filterField)
            .addComputedField(totalCount)
            .addComputedField(completedCount);

        DomainSchema temp = builder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(temp);

        return new DomainSchema.Builder("todo-app", "1.0.0")
            .hash(hash)
            .addAction(addTodoAction)
            .addDataField(todosField)
            .addDataField(filterField)
            .addComputedField(totalCount)
            .addComputedField(completedCount)
            .build();
    }

    /**
     * 초기 Snapshot 생성
     */
    private static Snapshot createInitialSnapshot(String schemaHash) {
        // 도메인 데이터
        Map<String, Object> data = new HashMap<>();
        data.put("todos", new ArrayList<>());
        data.put("filter", "all");

        // 계산된 값 (초기에는 비워두고 Compute에서 재계산)
        Map<String, Object> computed = new HashMap<>();

        // 메타데이터
        Snapshot.SnapshotMeta meta = Snapshot.SnapshotMeta.create(
            0,  // version
            System.currentTimeMillis(),  // timestamp
            "random-seed-123",  // randomSeed
            schemaHash   // schemaHash
        );

        return Snapshot.builder()
            .data(data)
            .computed(computed)
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(meta)
            .build();
    }

    /**
     * Intent 생성
     */
    private static Intent createIntent(String actionType, String title) {
        return new Intent.Builder()
            .type(actionType)
            .input("title", title)
            .intentId(UUID.randomUUID().toString())
            .build();
    }

    /**
     * Patch 적용 시연
     */
    private static void demonstrateApply() {
        Snapshot initial = Snapshot.initial();

        // Patch 생성
        Patch patch1 = Patch.set("data.count", 1);
        Patch patch2 = Patch.set("data.name", "Manifesto");

        // Apply
        Result<Snapshot, ErrorValue> result = ManifestoCore.getInstance()
            .apply(initial, patch1, patch2);

        if (result.isOk()) {
            Snapshot updated = result.unwrap();
            System.out.println("✓ Patch 적용 성공:");
            System.out.println("  - count: " + updated.getData().get("count"));
            System.out.println("  - name: " + updated.getData().get("name"));
        } else {
            System.out.println("✗ Patch 적용 실패");
        }
    }

}
