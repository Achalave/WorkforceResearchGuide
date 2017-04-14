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
 * This class contains data pertaining to a file that has been removed from the
 * physical repository but still exists in the system.
 *
 * @author Michael
 */
public class MissingFileIssue extends FileSynchIssue {

    /**
     * This response value indicates that the file has been removed.
     */
    public static final int RESPONSE_FILE_REMOVED = 0;

    /**
     * This response value indicates that the file has been relocated.
     */
    public static final int RESPONSE_FILE_RELOCATED = 1;

    DocumentData missingFile;
    DocumentData newFile;

    /**
     * Indicates if the file content has been changed and thus needs to be
     * re-indexed. This value is only checked if the file has been relocated.
     */
    boolean contentChanged = false;

    public MissingFileIssue(DocumentData missingFile) {
        this.missingFile = missingFile;
    }

    /**
     * This function should be called if the missing file was determined to be
     * moved to some other location.
     *
     * @param newFileLocation
     * @param contentChanged
     */
    public void documentRelocated(DocumentData newFileLocation, boolean contentChanged) {
        this.newFile = newFileLocation;
        this.contentChanged = contentChanged;
        userResponse = RESPONSE_FILE_RELOCATED;
    }

    /**
     * This function should be called if the missing file was determined to be
     * removed.
     */
    public void documentRemoved() {
        userResponse = RESPONSE_FILE_REMOVED;
    }

    @Override
    public void resolve(DatabaseController db, LuceneController lucene) throws InvalidResponseException, IndexingSessionNotStartedException, IOException, ReadSessionNotStartedException, TikaException, ConnectionNotStartedException, SQLException {
        switch (userResponse) {
            case RESPONSE_FILE_RELOCATED:
                //Change the stored path in Lucene
                lucene.updateDocumentPath(missingFile.getPath(), newFile.getPath());
                //If the content has been changed, re-index the document
                try {
                    if (contentChanged) {
                        lucene.updateDocumentContent(newFile.getPath());
                        //Make sure the DocumentData is fully up to date
                        newFile.fillFromFile();

                    } else {
                        db.updateDocumentPath(missingFile.getPath(), newFile.getPath());
                        newFile.fillName();
                        newFile.conditionalCopy(missingFile);
                    }
                    //Change the stored path in the databse
                    db.updateDocument(missingFile.getPath(), newFile);
                } catch (DatabaseFileDoesNotExistException ex) {
                    Logger.getLogger(MissingFileIssue.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case RESPONSE_FILE_REMOVED:
                //Delete the old data
                db.deleteDocument(missingFile.getPath());
                break;
            default:
                throw new InvalidResponseException();
        }
    }

}
