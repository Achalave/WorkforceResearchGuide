package utd.team6.workforceresearchguide.main.issues;

import java.io.IOException;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;

//@author Michael Haertling
public abstract class FileSynchIssue {

    int userResponse;

    /**
     * This function will resolve this issue based on the set user response. It
     * is assumed that an indexing session and read session are open before
     * calling this method.
     *
     * @param db
     * @param lucene
     * @throws
     * utd.team6.workforceresearchguide.main.issues.InvalidResponseException
     */
    public abstract void resolve(DatabaseController db, LuceneController lucene) throws InvalidResponseException,IndexingSessionNotStartedException,ReadSessionNotStartedException,IOException;

    public void setUserResponse(int res) {
        this.userResponse = res;
    }
}
