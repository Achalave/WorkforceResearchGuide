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
import static utd.team6.workforceresearchguide.main.issues.MissingFileIssue.RESPONSE_FILE_RELOCATED;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;

/**
 * This class stores data pertaining to a pair of files, one that has been added
 * and one that is missing. It is possible that the missing file has been moved.
 * If the found file is determined to not be the moved missing file, the user
 * should still get an opportunity to browse for the moved file.
 *
 * @author Michael
 */
public class MovedFileIssue extends FileSyncIssue {

    /**
     * This response value indicates that the file has been removed.
     */
    public static final int RESPONSE_FILE_REMOVED = 0;

    /**
     * This response value indicates that the file has been relocated.
     */
    public static final int RESPONSE_FILE_RELOCATED = 1;
    
    /**
     * This response value indicates that the file should be kept in the system.
     */
    public static final int RESPONSE_KEEP_FILE = 2;

    DocumentData missingFile;
    DocumentData newFile;
    DocumentData alternateFile;

    /**
     * Creates a new MovedFileIssue object.
     *
     * @param missingFile
     * @param newFileLocation
     */
    public MovedFileIssue(DocumentData missingFile, DocumentData newFileLocation) {
        this.newFile = newFileLocation;
        this.missingFile = missingFile;
    }

    /**
     * This function should be called if the suggested added file is the
     * relocated missing file.
     */
    public void relocateFile() {
        userResponse = RESPONSE_FILE_RELOCATED;
    }

    /**
     * Changes the file that the missing file will be relocated as.
     * @param data 
     */
    public void changeRelocation(DocumentData data) {
        alternateFile = data;
    }

    /**
     * Sets the user response to keep the missing file.
     */
    public void keepFile(){
        userResponse = RESPONSE_KEEP_FILE;
    }
    
    /**
     * Sets the user response to remove the missing file.
     */
    public void removeFile(){
        userResponse = RESPONSE_FILE_REMOVED;
    }
    
    @Override
    public void resolve(DatabaseController db, LuceneController lucene) throws InvalidResponseException, IndexingSessionNotStartedException, IOException, ReadSessionNotStartedException, TikaException, ConnectionNotStartedException, SQLException {
        switch (userResponse) {
            case RESPONSE_FILE_RELOCATED:
                DocumentData nf;
                if(alternateFile != null){
                    //A different path was given for the new file.
                    nf = alternateFile;
                    //Add the new file
                    lucene.indexNewDocument(newFile.getPath());
                    db.addDocument(newFile);
                }else{
                    nf = newFile;
                }
                //Change the stored path in Lucene
                lucene.updateDocumentPath(missingFile.getPath(), nf.getPath());
                try {
                    db.updateDocumentPath(missingFile.getPath(), nf.getPath());
                    nf.fillName();
                    nf.conditionalCopy(missingFile);
                    //Change the stored path in the databse
                    db.updateDocument(missingFile.getPath(), nf);
                } catch (DatabaseFileDoesNotExistException ex) {
                    Logger.getLogger(MissingFileIssue.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case RESPONSE_FILE_REMOVED:
                //Import the new data
                newFile.fillFromFile();
                lucene.indexNewDocument(newFile.getPath());
                //Add the data to the database
                db.addDocument(newFile);
                break;
            default:
                throw new InvalidResponseException();
        }
    }

    /**
     * Get the missing file object.
     * @return 
     */
    public DocumentData getMissingFile() {
        return missingFile;
    }

    /**
     * Get the added file object.
     * @return 
     */
    public DocumentData getNewFile() {
        return newFile;
    }

}
