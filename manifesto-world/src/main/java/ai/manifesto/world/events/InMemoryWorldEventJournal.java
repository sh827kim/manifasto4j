package ai.manifesto.world.events;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * KR: world 이벤트를 메모리에 저장하고 type/time 기준 질의를 제공하는 sink입니다.
 * EN: In-memory world event sink providing query by type and timestamp.
 */
public final class InMemoryWorldEventJournal implements WorldEventSink {
    private final List<WorldEvent> events = new ArrayList<>();

    @Override
    public synchronized void emit(WorldEvent event) {
        events.add(Objects.requireNonNull(event, "event is required"));
    }

    public synchronized List<WorldEvent> listAll() {
        return events.stream()
            .sorted(Comparator.comparingLong(WorldEvent::getTimestamp))
            .toList();
    }

    public synchronized List<WorldEvent> queryByType(String type) {
        if (type == null || type.isBlank()) {
            return List.of();
        }
        return events.stream()
            .filter(event -> type.equals(event.getType()))
            .sorted(Comparator.comparingLong(WorldEvent::getTimestamp))
            .toList();
    }

    public synchronized List<WorldEvent> querySince(long minTimestampInclusive, int limit) {
        int safeLimit = Math.max(0, limit);
        if (safeLimit == 0) {
            return List.of();
        }
        return events.stream()
            .filter(event -> event.getTimestamp() >= minTimestampInclusive)
            .sorted(Comparator.comparingLong(WorldEvent::getTimestamp))
            .limit(safeLimit)
            .toList();
    }

    public synchronized void clear() {
        events.clear();
    }
}
