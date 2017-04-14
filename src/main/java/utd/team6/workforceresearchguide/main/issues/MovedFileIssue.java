package utd.team6.workforceresearchguide.main.issues;

//@author Michael Haertling
import java.io.IOException;
import java.sql.SQLException;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.main.DocumentData;
import static utd.team6.workforceresearchguide.main.issues.MissingFileIssue.RESPONSE_FILE_REMOVED;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;

/**
 * This class stores data pertaining to a pair of files, one that has been added
 * and one that is missing. It is possible that the missing file has been moved.
 * If the found file is determined to not be the moved missing file, the user
 * should still get an opportunity to browse for the moved file.
 *
 * @author Michael
 */
public class MovedFileIssue extends MissingFileIssue {

    public MovedFileIssue(DocumentData missingFile, DocumentData newFileLocation) {
        super(missingFile);
        this.newFile = newFileLocation;
    }

    /**
     * This function should be called if the suggested added file is the
     * relocated missing file.
     */
    public void relocationSuggestionAccepted() {
        userResponse = RESPONSE_FILE_RELOCATED;
    }

    @Override
    public void resolve(DatabaseController db, LuceneController lucene) throws InvalidResponseException, IndexingSessionNotStartedException, IOException, ReadSessionNotStartedException, TikaException, ConnectionNotStartedException, SQLException {
        super.resolve(db, lucene);
        switch (userResponse) {
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

}
