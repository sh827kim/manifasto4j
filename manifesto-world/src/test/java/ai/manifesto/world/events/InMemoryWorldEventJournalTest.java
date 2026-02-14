package ai.manifesto.world.events;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryWorldEventJournalTest {

    @Test
    void supportsTypeAndTimeBoundQueries() {
        InMemoryWorldEventJournal journal = new InMemoryWorldEventJournal();
        journal.emit(new WorldEvent("proposal:submitted", 100L, Map.of("id", "p1")));
        journal.emit(new WorldEvent("proposal:evaluating", 110L, Map.of("id", "p1")));
        journal.emit(new WorldEvent("proposal:submitted", 120L, Map.of("id", "p2")));

        List<WorldEvent> submitted = journal.queryByType("proposal:submitted");
        assertEquals(2, submitted.size());
        assertEquals(100L, submitted.get(0).getTimestamp());
        assertEquals(120L, submitted.get(1).getTimestamp());

        List<WorldEvent> since = journal.querySince(110L, 10);
        assertEquals(2, since.size());
        assertEquals("proposal:evaluating", since.get(0).getType());

        List<WorldEvent> limited = journal.querySince(0L, 1);
        assertEquals(1, limited.size());

        journal.clear();
        assertTrue(journal.listAll().isEmpty());
    }
}
