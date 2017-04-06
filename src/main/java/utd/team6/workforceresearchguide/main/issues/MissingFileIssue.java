package utd.team6.workforceresearchguide.main.issues;

//@author Michael Haertling
import java.io.IOException;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.main.DocumentData;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;

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
    DocumentData newFileLocation;

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
        this.newFileLocation = newFileLocation;
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
    public void resolve(DatabaseController db, LuceneController lucene) throws InvalidResponseException, IndexingSessionNotStartedException, IOException, ReadSessionNotStartedException {
        switch (userResponse) {
            case RESPONSE_FILE_RELOCATED:
                //Change the stored path in Lucene
                lucene.updateDocumentPath(missingFile.getPath(), newFileLocation.getPath());
                //If the content has been changed, re-index the document
                if (contentChanged) {

                }
                //Update the file data in the database
                break;
            case RESPONSE_FILE_REMOVED:
                //Delete the old data

                break;
            default:
                throw new InvalidResponseException();
        }
    }

}
