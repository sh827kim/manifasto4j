package ai.manifesto.app;

/**
 * KR: 요청한 branch alias를 찾지 못할 때 발생합니다.
 * EN: Raised when the requested branch alias cannot be found.
 */
public final class BranchNotFoundException extends ManifestoAppException {
    private final String branchName;

    public BranchNotFoundException(String branchName) {
        super("APP-BRANCH-NOT-FOUND", "Unknown branch alias: " + branchName);
        this.branchName = branchName;
    }

    public String getBranchName() {
        return branchName;
    }
}
