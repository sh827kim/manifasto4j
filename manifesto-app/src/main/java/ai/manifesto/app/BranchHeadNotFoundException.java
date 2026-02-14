package ai.manifesto.app;

/**
 * KR: 복구 대상 branch head를 찾을 수 없을 때 발생하는 예외입니다.
 * EN: Exception raised when the requested branch head cannot be found during recovery.
 */
public final class BranchHeadNotFoundException extends ManifestoAppException {
    private final String branchName;

    public BranchHeadNotFoundException(String branchName) {
        super("APP-BRANCH-HEAD-NOT-FOUND", "Branch head not found: " + branchName);
        this.branchName = branchName;
    }

    public String getBranchName() {
        return branchName;
    }
}
