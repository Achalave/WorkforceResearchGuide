package utd.team6.workforceresearchguide.main.issues;

//@author Michael Haertling
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.main.DocumentData;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;

/**
 * This class stores data pertaining to a file that has been changed since it
 * was last indexed into the system
 *
 * @author Michael
 */
public class OutdatedFileIssue extends FileSynchIssue {

    public static final int RESPONSE_UPDATE = 0;
    public static final int RESPONSE_IGNORE = 1;

    DocumentData outdatedFile;

    public OutdatedFileIssue(DocumentData outdatedFile) {
        this.outdatedFile = outdatedFile;
    }

    public void ignoreOutdatedFile(){
        userResponse = RESPONSE_IGNORE;
    }
    
    public void updateOutdatedFile(){
        userResponse = RESPONSE_UPDATE;
    }
    
    @Override
    public void resolve(DatabaseController db, LuceneController lucene) throws InvalidResponseException {
        switch (userResponse) {
            case RESPONSE_UPDATE:
                break;
            case RESPONSE_IGNORE:
                break;
            default:
                throw new InvalidResponseException();
        }
    }

}
