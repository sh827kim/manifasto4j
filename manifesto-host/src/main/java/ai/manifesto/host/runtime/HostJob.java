package ai.manifesto.host.runtime;

/**
 * KR: Host mailbox에서 처리되는 모든 job의 공통 인터페이스입니다.
 * EN: Common interface for all jobs handled by the host mailbox.
 */
public interface HostJob {
    HostJobType getType();
}
