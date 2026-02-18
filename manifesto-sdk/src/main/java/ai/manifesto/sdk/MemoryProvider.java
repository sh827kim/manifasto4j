package ai.manifesto.sdk;

import java.util.List;
import java.util.Optional;

/**
 * KR: SDK memory provider 계약입니다.
 * EN: SDK memory provider contract.
 */
public interface MemoryProvider {
    void save(StoredMemoryRecord record);

    Optional<StoredMemoryRecord> load(String key);

    List<StoredMemoryRecord> list();

    default void remove(String key) {
    }
}
