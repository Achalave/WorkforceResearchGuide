package utd.team6.workforceresearchguide.main.issues;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;

//@author Michael Haertling
public abstract class FileSyncIssue {

    int userResponse = -1;

    /**
     * This function will resolve this issue based on the set user response. It
     * is assumed that an indexing session and read session are open before
     * calling this method.
     *
     * @param db
     * @param lucene
     * @throws
     * utd.team6.workforceresearchguide.main.issues.InvalidResponseException
     * @throws utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException
     * @throws utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException
     * @throws java.io.IOException
     * @throws org.apache.tika.exception.TikaException
     * @throws utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     * @throws java.sql.SQLException
     */
    public abstract void resolve(DatabaseController db, LuceneController lucene) throws InvalidResponseException,IndexingSessionNotStartedException,ReadSessionNotStartedException,IOException,TikaException,ConnectionNotStartedException,SQLException;

    public void setUserResponse(int res) {
        this.userResponse = res;
    }
}
