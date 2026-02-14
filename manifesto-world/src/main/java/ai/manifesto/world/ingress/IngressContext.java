package ai.manifesto.world.ingress;

/**
 * KR: IngressContext는 proposal ingress epoch를 관리하고 stale 여부를 판정합니다.
 * EN: IngressContext manages proposal ingress epoch and determines staleness.
 */
public final class IngressContext {
    private long epoch;

    public IngressContext() {
        this(0L);
    }

    public IngressContext(long initialEpoch) {
        if (initialEpoch < 0) {
            throw new IllegalArgumentException("initialEpoch must be >= 0");
        }
        this.epoch = initialEpoch;
    }

    public long epoch() {
        return epoch;
    }

    public void incrementEpoch() {
        epoch += 1;
    }

    public boolean isStale(long proposalEpoch) {
        return proposalEpoch < epoch;
    }
}
