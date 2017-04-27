package utd.team6.workforceresearchguide.main.issues;

/**
 * This class is used when an issue fails to resolve.
 *
 * @author Michael
 */
public abstract class FailedFileSyncIssue {

    /**
     * The issue that failed to resolve automatically.
     */
    protected FileSyncIssue issue;

    /**
     * Creates a new FailedFileSyncIssue.
     *
     * @param is
     */
    public FailedFileSyncIssue(FileSyncIssue is) {
        this.issue = is;
    }

    /**
     *
     * @return
     */
    public FileSyncIssue getIssue() {
        return issue;
    }

    /**
     *
     * @return
     */
    public abstract String getReason();

}
