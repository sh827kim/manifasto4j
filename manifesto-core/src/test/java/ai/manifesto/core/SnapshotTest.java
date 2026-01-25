package ai.manifesto.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Snapshot 테스트")
class SnapshotTest {

    private Snapshot snapshot;

    @BeforeEach
    void setUp() {
        snapshot = Snapshot.initial();
    }

    @Test
    @DisplayName("초기 Snapshot 생성")
    void testInitialSnapshot() {
        assertNotNull(snapshot);
        assertNotNull(snapshot.getData());
        assertNotNull(snapshot.getComputed());
        assertNotNull(snapshot.getSystem());
        assertNotNull(snapshot.getInput());
        assertNotNull(snapshot.getMeta());
    }

    @Test
    @DisplayName("Snapshot 메타데이터 검증")
    void testSnapshotMetadata() {
        Snapshot.SnapshotMeta meta = snapshot.getMeta();
        assertEquals(0, meta.getVersion());
        assertTrue(meta.getTimestamp() > 0);
        assertNotNull(meta.getSchemaHash());
    }

    @Test
    @DisplayName("Data 필드 설정 및 조회")
    void testDataField() {
        Map<String, Object> data = new HashMap<>();
        data.put("count", 42);
        data.put("name", "test");

        Snapshot updated = snapshot.withData(data);
        assertEquals(42, updated.getData().get("count"));
        assertEquals("test", updated.getData().get("name"));
    }

    @Test
    @DisplayName("Input 필드 설정")
    void testInputField() {
        Map<String, Object> input = new HashMap<>();
        input.put("userId", "user123");
        input.put("action", "create");

        Snapshot updated = snapshot.withInput(input);
        assertEquals("user123", updated.getInput().get("userId"));
        assertEquals("create", updated.getInput().get("action"));
    }

    @Test
    @DisplayName("메타데이터 버전")
    void testMetadataVersion() {
        assertEquals(0, snapshot.getMeta().getVersion());

        // 새로운 메타데이터로 업데이트
        Snapshot.SnapshotMeta newMeta = Snapshot.SnapshotMeta.create(
            1,
            System.currentTimeMillis(),
            "seed",
            "hash"
        );
        Snapshot updated = snapshot.withMeta(newMeta);
        assertEquals(1, updated.getMeta().getVersion());
        assertEquals(0, snapshot.getMeta().getVersion()); // 원본은 변경 없음
    }

    @Test
    @DisplayName("Snapshot 불변성")
    void testSnapshotImmutability() {
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("value", 10);

        Snapshot snap1 = snapshot.withData(originalData);

        // 새로운 Snapshot 생성
        Map<String, Object> newData = new HashMap<>();
        newData.put("value", 20);
        Snapshot snap2 = snapshot.withData(newData);

        // 각각 독립적이어야 함
        assertEquals(10, snap1.getData().get("value"));
        assertEquals(20, snap2.getData().get("value"));
    }

    @Test
    @DisplayName("Computed 필드 설정")
    void testComputedField() {
        Map<String, Object> computed = new HashMap<>();
        computed.put("total", 100);
        computed.put("average", 50.0);

        Snapshot updated = snapshot.withComputed(computed);
        assertEquals(100, updated.getComputed().get("total"));
        assertEquals(50.0, updated.getComputed().get("average"));
    }
}
