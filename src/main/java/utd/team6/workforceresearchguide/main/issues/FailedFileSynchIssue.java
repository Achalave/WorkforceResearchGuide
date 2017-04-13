package utd.team6.workforceresearchguide.main.issues;



//@author Michael Haertling

public abstract class FailedFileSynchIssue {
    
    protected FileSynchIssue issue;
    
    public FailedFileSynchIssue(FileSynchIssue is){
        this.issue = is;
    }

    public FileSynchIssue getIssue() {
        return issue;
    }
    
    public abstract String getReason();
    
}
