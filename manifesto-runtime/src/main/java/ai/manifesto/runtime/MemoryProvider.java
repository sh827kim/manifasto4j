package ai.manifesto.runtime;

import java.util.List;
import java.util.Optional;

/**
 * KR: Memory 저장소 구현 교체를 위한 provider 계약입니다.
 * EN: Provider contract for pluggable memory storage.
 */
public interface MemoryProvider {
    void save(StoredMemoryRecord record);

    Optional<StoredMemoryRecord> load(String key);

    List<StoredMemoryRecord> list();

    default void remove(String key) {
    }
}
