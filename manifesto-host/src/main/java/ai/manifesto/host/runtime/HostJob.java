package ai.manifesto.host.runtime;

/**
 * KR: HostJob는 Host 런타임 계층에서 host job 계약을 정의하는 인터페이스입니다.
 * EN: HostJob is an interface defining the host job contract in the Host runtime layer.
 */
public interface HostJob {
    HostJobType getType();
}
