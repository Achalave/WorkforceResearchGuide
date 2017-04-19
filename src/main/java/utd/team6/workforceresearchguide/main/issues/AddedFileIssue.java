package utd.team6.workforceresearchguide.main.issues;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.main.DocumentData;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;

//@author Michael Haertling
public class AddedFileIssue extends FileSyncIssue {

    /**
     * This response value indicates that the file should be added to the
     * system.
     */
    public static final int RESPONSE_ADD_FILE = 0;

    /**
     * This response value indicates that the file should not be added to the
     * system.
     */
    public static final int RESPONSE_IGNORE_FILE = 1;

    DocumentData addedFile;

    public AddedFileIssue(DocumentData add) {
        addedFile = add;
    }

    public void addFile(){
        this.setUserResponse(RESPONSE_ADD_FILE);
    }
    
    public void ignoreFile(){
        this.setUserResponse(RESPONSE_IGNORE_FILE);
    }
    
    @Override
    public void resolve(DatabaseController db, LuceneController lucene) throws InvalidResponseException, IndexingSessionNotStartedException, ReadSessionNotStartedException, IOException, TikaException, ConnectionNotStartedException, SQLException {
        switch(userResponse){
            case RESPONSE_ADD_FILE:
                addedFile.fillFromFile();
                db.addDocument(addedFile);
                lucene.indexNewDocument(addedFile.getPath());
                break;
            case RESPONSE_IGNORE_FILE:
                break;
            default:
                throw new InvalidResponseException();
        }
    }

    @Override
    public String toString(){
        return "Added File Issue: "+addedFile.getPath();
    }
    
}
