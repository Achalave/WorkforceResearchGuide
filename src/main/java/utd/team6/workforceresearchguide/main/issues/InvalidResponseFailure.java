package utd.team6.workforceresearchguide.main.issues;

/**
 * This type of response failure occurs when an invalid response was given to an
 * issue when it was resolved.
 *
 * @author Michael
 */
public class InvalidResponseFailure extends FailedFileSyncIssue {

    /**
     * Creates a new InvalidResponseFailure object.
     *
     * @param is
     */
    public InvalidResponseFailure(FileSyncIssue is) {
        super(is);
    }

    /**
     *
     * @return
     */
    @Override
    public String getReason() {
        return "An invalid user response was recorded for the issue.";
    }

}
