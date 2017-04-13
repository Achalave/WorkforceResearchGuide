package utd.team6.workforceresearchguide.main.issues;



//@author Michael Haertling

public class InvalidResponseFaliure extends FailedFileSynchIssue{

    public InvalidResponseFaliure(FileSynchIssue is) {
        super(is);
    }

    @Override
    public String getReason() {
        return "An invalid user response was recorded for the issue.";
    }
    
    
    
}
