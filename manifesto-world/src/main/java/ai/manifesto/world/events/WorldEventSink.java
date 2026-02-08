package ai.manifesto.world.events;

public interface WorldEventSink {
    void emit(WorldEvent event);
}
