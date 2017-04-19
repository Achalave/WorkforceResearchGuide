package utd.team6.workforceresearchguide.main.issues;

//@author Michael Haertling
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.main.DocumentData;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;

/**
 * This class stores data pertaining to a file that has been changed since it
 * was last indexed into the system
 *
 * @author Michael
 */
public class OutdatedFileIssue extends FileSyncIssue {

    public static final int RESPONSE_UPDATE = 0;
    public static final int RESPONSE_IGNORE = 1;

    DocumentData outdatedFile;

    public OutdatedFileIssue(DocumentData outdatedFile) {
        this.outdatedFile = outdatedFile;
    }

    public void ignoreOutdatedFile() {
        userResponse = RESPONSE_IGNORE;
    }

    public void updateOutdatedFile() {
        userResponse = RESPONSE_UPDATE;
    }

    @Override
    public void resolve(DatabaseController db, LuceneController lucene) throws InvalidResponseException, ConnectionNotStartedException, SQLException, IOException, TikaException, IndexingSessionNotStartedException, ReadSessionNotStartedException {
        outdatedFile.fillFromFile();
        switch (userResponse) {
            case RESPONSE_UPDATE:
                try {
                    //Update the hash value so it isnt detected as outdated next time
                    db.updateDocument(outdatedFile.getPath(), outdatedFile);
                    //Update the lucene stuff
                    lucene.updateDocumentContent(outdatedFile.getPath());
                } catch (DatabaseFileDoesNotExistException ex) {
                    Logger.getLogger(OutdatedFileIssue.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case RESPONSE_IGNORE: {
                try {
                    //Update the hash value so it isnt detected as outdated next time
                    db.updateDocument(outdatedFile.getPath(), outdatedFile);
                } catch (DatabaseFileDoesNotExistException ex) {
                    Logger.getLogger(OutdatedFileIssue.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            default:
                throw new InvalidResponseException();
        }
    }

}
