package utd.team6.workforceresearchguide.main.issues;



//@author Michael Haertling

public class InvalidResponseFaliure extends FailedFileSyncIssue{

    public InvalidResponseFaliure(FileSyncIssue is) {
        super(is);
    }

    @Override
    public String getReason() {
        return "An invalid user response was recorded for the issue.";
    }
    
    
    
}
