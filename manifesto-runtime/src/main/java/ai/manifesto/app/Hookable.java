package ai.manifesto.app;

/**
 * KR: Hookable은 App hook 등록/해제 경계를 분리한 계약입니다.
 * EN: Hookable separates hook registration/removal boundary from the main App contract.
 */
public interface Hookable {
    void addHook(AppHook hook);

    void removeHook(AppHook hook);
}
