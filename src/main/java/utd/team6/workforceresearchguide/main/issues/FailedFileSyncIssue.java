package utd.team6.workforceresearchguide.main.issues;



//@author Michael Haertling

public abstract class FailedFileSyncIssue {
    
    protected FileSyncIssue issue;
    
    public FailedFileSyncIssue(FileSyncIssue is){
        this.issue = is;
    }

    public FileSyncIssue getIssue() {
        return issue;
    }
    
    public abstract String getReason();
    
}
