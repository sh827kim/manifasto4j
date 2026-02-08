package ai.manifesto.world.events;

public final class NoopWorldEventSink implements WorldEventSink {
    @Override
    public void emit(WorldEvent event) {
        // no-op
    }
}
