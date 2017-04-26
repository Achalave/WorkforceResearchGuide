/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.main;

/**
 *
 * @author Michael
 */
public interface SessionManager {

    /**
     * This is a thread safe method of starting a database connection.
     */
    public void startDBConnection();

    /**
     * This is a thread safe method of stopping a database connection. If the
     * corresponding thread safe version was used to start the connection, this
     * function must be used to stop the connection.
     */
    public void stopDBConnection();

    /**
     * This is a thread safe method of starting a Lucene indexing connection.
     */
    public void startLuceneIndexingSession();

    /**
     * This is a thread safe method of stopping a Lucene indexing connection. If
     * the corresponding thread safe version was used to start the connection,
     * this function must be used to stop the connection.
     */
    public void stopLuceneIndexingSession();

    /**
     * This is a thread safe method of starting a Lucene read connection.
     */
    public void startLuceneReadSession();

    /**
     * This is a thread safe method of stopping a Lucene read connection. If the
     * corresponding thread safe version was used to start the connection, this
     * function must be used to stop the connection.
     */
    public void stopLuceneReadSession();

    /**
     * Gets permission to open sessions. This must be called before sessions can
     * be created.
     */
    public void getSessionPermission();

    /**
     * Releases permission to start sessions.
     */
    public void releaseSessionPermission();
}
