package utd.team6.workforceresearchguide.main;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.gui.DocumentDisplay;
import utd.team6.workforceresearchguide.gui.DocumentInfoDialogFactory;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.LuceneSearchSession;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.ConnectionAlreadyActiveException;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;
import utd.team6.workforceresearchguide.sqlite.DatabaseTagDoesNotExistException;

/**
 * This is the primary controller for the entire application. It is responsible
 * for connecting the various controllers and other management classes into
 * cohesive functions.
 *
 * @author Michael
 */
public class ApplicationController implements SessionManager, DocumentTagSource {

    Semaphore sessionSem;

    LuceneController lucene;
    DatabaseController db;

    DocumentInfoDialogFactory infoFactory;

    boolean searchInProgress = false;
    LuceneSearchSession search;

    Timer applicationTimer;

    /**
     * Creates a new ApplicationController object.
     *
     * @param lucenePath
     * @param databasePath
     */
    public ApplicationController(String lucenePath, String databasePath) {
        try {
            lucene = new LuceneController(lucenePath);
            db = new DatabaseController(databasePath);
        } catch (IOException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }

        applicationTimer = new Timer(true);

        sessionSem = new Semaphore(1);

        infoFactory = new DocumentInfoDialogFactory(this);
    }

    /**
     * Adds files to the existing Lucene index from provided String[].
     *
     *
     * @param paths
     * @throws IOException
     * @throws TikaException
     * @throws IndexingSessionNotStartedException
     */
    public void addDocuments(String[] paths) throws IOException, TikaException,
            IndexingSessionNotStartedException {

        //Get current file hierarchy
        String[] filePaths = paths;

        //set indexing session to APPEND index
        this.getSessionPermission();
        this.startLuceneIndexingSession();
        this.startLuceneReadSession();

        //indexes files
        lucene.indexNewDocuments(filePaths);

        this.stopLuceneIndexingSession();
        this.stopLuceneReadSession();
        this.releaseSessionPermission();

    }

    /**
     *
     * @return The DocumentInfoDialogFactory for this ApplicationController.
     */
    public DocumentInfoDialogFactory getInfoFactory() {
        return infoFactory;
    }

    /**
     * Starts a new search with the provided query.
     *
     * @param query
     * @throws IOException
     * @throws ReadSessionNotStartedException
     */
    public void beginSearch(String query) throws IOException, ReadSessionNotStartedException {
        this.getSessionPermission();
        this.startLuceneReadSession();
        this.startDBConnection();

        search = lucene.search(query, 100);
        search.startSearch();
    }

    /**
     * Cancels the current ongoing search session.
     */
    public void cancelSearch() {
        search.cancelSearch();
        searchComplete();
    }

    /**
     * Indicates that the search has been completed and the resources should be
     * released.
     */
    public void searchComplete() {
        search = null;
        this.stopLuceneReadSession();
        this.stopDBConnection();
        this.releaseSessionPermission();
    }

    /**
     *
     * @return True if a search is in progress, false otherwise.
     */
    public boolean searchRunning() {
        return search.searchInProgress();
    }

    /**
     * This function is called periodically in order to collect and update
     * search results during a search session.
     *
     * @param results
     * @param resultTags
     */
    public void updateSearchResults(HashMap<Integer, DocumentDisplay> results, HashSet<String> resultTags) {
        try {
            aggregateResultSet(results, resultTags);
        } catch (IOException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Collects all search results into a hash map of SearchResult objects. The
     * key is the Lucene document id, which is subject to change between search
     * sessions.
     *
     * @param results
     * @param tags
     * @throws IOException
     */
    protected void aggregateResultSet(HashMap<Integer, DocumentDisplay> results, HashSet<String> tags) throws IOException {
        TopDocs docs = search.getTagHits();
        for (ScoreDoc score : docs.scoreDocs) {
            DocumentDisplay disp = results.get(score.doc);
            SearchResult result;
            if (disp == null) {
                Document doc = search.getDocument(score.doc);
                result = new SearchResult(doc.get("path"), score.score, 0);
                results.put(score.doc, new DocumentDisplay(result));
                try {
                    //Record the tags on the document
                    for (String tag : db.getDocumentTags(result.getFilePath())) {
                        tags.add(tag);
                    }
                } catch (ConnectionNotStartedException ex) {
                    Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                result = disp.getSearchResult();
                result.updateTagScore(score.score);
            }
        }

        //see if anything added to hashmap
//        System.out.println("Result Map Size: " + results.size());
        docs = search.getContentHits();
        for (ScoreDoc score : docs.scoreDocs) {
            DocumentDisplay disp = results.get(score.doc);
            SearchResult result;
            if (disp == null) {
                Document doc = search.getDocument(score.doc);
                result = new SearchResult(doc.get("path"), 0, score.score);
                results.put(score.doc, new DocumentDisplay(result));
                try {
                    //Record the tags on the document
                    for (String tag : db.getDocumentTags(result.getFilePath())) {
                        tags.add(tag);
                    }
                } catch (ConnectionNotStartedException ex) {
                    Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                result = disp.getSearchResult();
                result.updateContentScore(score.score);
            }
        }
    }

    /**
     * This is a thread safe method of starting a database connection.
     */
    @Override
    public void startDBConnection() {
        try {
            db.startConnection();
        } catch (ConnectionAlreadyActiveException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This is a thread safe method of stopping a database connection. If the
     * corresponding thread safe version was used to start the connection, this
     * function must be used to stop the connection.
     */
    @Override
    public void stopDBConnection() {
        db.stopConnection();
    }

    /**
     * This is a thread safe method of starting a Lucene indexing connection.
     */
    @Override
    public void startLuceneIndexingSession() {
        try {
            lucene.startIndexingSession();
        } catch (IOException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This is a thread safe method of stopping a Lucene indexing connection. If
     * the corresponding thread safe version was used to start the connection,
     * this function must be used to stop the connection.
     */
    @Override
    public void stopLuceneIndexingSession() {
        try {
            lucene.stopIndexingSession();
        } catch (IOException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This is a thread safe method of starting a Lucene read connection.
     */
    @Override
    public void startLuceneReadSession() {
        try {
            lucene.startReadSession();
        } catch (IOException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This is a thread safe method of stopping a Lucene read connection. If the
     * corresponding thread safe version was used to start the connection, this
     * function must be used to stop the connection.
     */
    @Override
    public void stopLuceneReadSession() {
        try {
            lucene.stopReadSession();
        } catch (IOException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void getSessionPermission() {
        try {
            sessionSem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void releaseSessionPermission() {
        sessionSem.release();
    }

    /**
     * Updates the database schema.
     */
    public void updateDatabaseSchema() {
        try {
            this.startDBConnection();
            db.updateDatabaseSchema();
            this.stopDBConnection();
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param filePath
     * @return The corresponding DocumentData object as represented by the
     * database.
     * @throws DatabaseFileDoesNotExistException
     */
    public DocumentData getDocumentData(String filePath) throws DatabaseFileDoesNotExistException {
        DocumentData data = null;
        this.getSessionPermission();
        this.startDBConnection();
        try {
            data = db.getDocumentData(filePath);
        } catch (ConnectionNotStartedException | ParseException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.stopDBConnection();
        this.releaseSessionPermission();
        return data;
    }

    /**
     *
     * @param repPath
     * @return A new FileSyncManager.
     */
    public FileSyncManager generateFileSyncManager(String repPath) {
        return new FileSyncManager(this, lucene, db, Utils.extractAllPaths(repPath));
    }

    @Override
    public ArrayList<String> getDocumentTags(String docPath) {
        ArrayList<String> tags = null;
        this.getSessionPermission();
        this.startDBConnection();
        try {
            tags = db.getDocumentTags(docPath);
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.stopDBConnection();
        this.releaseSessionPermission();
        return tags;
    }

    @Override
    public ArrayList<String> getSuggestedDocumentTags(String docPath, int numTags) {

        HashMap<String, Integer> tagMap = new HashMap<>();

        this.getSessionPermission();
        this.startDBConnection();

        try {
            ArrayList<String> tags = db.getDocumentTags(docPath);
            //Find what tags are suggested based off other tags
            for (String tag : tags) {
                HashMap<String, Integer> tagAssociations = db.getTagAssociation(tag, numTags);
                for (String key : tagAssociations.keySet()) {
                    Integer count = tagMap.get(key);
                    if (count == null) {
                        //The key is not in the tagMap, add it
                        tagMap.put(key, tagAssociations.get(key));
                    } else {
                        //Add the count for that key
                        tagMap.put(key, count + tagAssociations.get(key));
                    }
                }
            }
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stopDBConnection();
        this.releaseSessionPermission();

        //Grab the top tags
        TagNode head = null;
        TagNode end = null;
        int listSize = 0;

        for (String key : tagMap.keySet()) {
            Integer count = tagMap.get(key);
            if (head == null) {
                //There is no head, make it
                head = new TagNode(key, count, null, null);
                end = head;
                listSize++;
            } else {
                //Begin iteration
                TagNode tmp = head;
                for (int i = 0; i < numTags; i++) {
                    if (tmp.count < count) {
                        //Insert the new node in place of this node
                        TagNode oldP = tmp.previous;
                        tmp.previous = new TagNode(key, count, tmp, oldP);
                        listSize++;
                        //Trim the list if needed
                        if (listSize > numTags) {
                            end = end.previous;
                            end.next = null;
                        }
                    } else if (listSize <= numTags) {
                        //The new tag has a smaller count than anything in the list
                        //but the list is can still hold one more
                        //Add the new node at the end
                        end.next = new TagNode(key, count, null, end);
                        end = end.next;
                    }
                }
            }
        }

        //Generate the final list
        ArrayList<String> suggestedTags = new ArrayList<>();

        TagNode tmp = head;
        while (tmp != null) {
            suggestedTags.add(tmp.tag);
            tmp = tmp.next;
        }

        return suggestedTags;
    }

    @Override
    public void addTag(String tag) {
        this.getSessionPermission();
        this.startDBConnection();
        try {
            db.addTag(tag);
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.stopDBConnection();
        this.releaseSessionPermission();
    }

    @Override
    public void addDocumentTag(String docPath, String tag) throws DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException {
        this.getSessionPermission();
        this.startDBConnection();
        this.startLuceneIndexingSession();
        this.startLuceneReadSession();

        try {
            db.tagDocument(docPath, tag);
            lucene.tagDocument(docPath, tag);
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IndexingSessionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ReadSessionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stopLuceneIndexingSession();
        this.stopLuceneReadSession();
        this.stopDBConnection();
        this.releaseSessionPermission();
    }

    @Override
    public ArrayList<String> getTagList() {
        ArrayList<String> tags = null;
        this.getSessionPermission();
        this.startDBConnection();
        try {
            tags = db.getTags();
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.stopDBConnection();
        this.releaseSessionPermission();
        return tags;
    }

    @Override
    public void removeTag(String tag) {
        this.getSessionPermission();
        this.startDBConnection();
        this.startLuceneIndexingSession();
        this.startLuceneReadSession();

        try {
            db.deleteTag(tag);
            lucene.removeTag(tag);
        } catch (ConnectionNotStartedException | IOException | IndexingSessionNotStartedException | ReadSessionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.stopDBConnection();
        this.stopLuceneIndexingSession();
        this.stopLuceneReadSession();
        this.releaseSessionPermission();
    }

    @Override
    public void removeDocumentTag(String docPath, String tag) throws DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException {
        this.getSessionPermission();
        this.startDBConnection();
//        this.startLuceneReadSession();
//        this.startLuceneIndexingSession();
        try {
            db.removeDocumentTag(docPath, tag);
//            lucene.removeDocumentTag(docPath, tag);
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
//        this.stopLuceneIndexingSession();
//        this.stopLuceneReadSession();
        this.stopDBConnection();
        this.releaseSessionPermission();
    }

    /**
     *
     * @return A list of all groups in the database.
     */
    public ArrayList<String> getGroups() {
        ArrayList<String> groups = null;

        this.getSessionPermission();
        this.startDBConnection();

        try {
            groups = db.getGroups();
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stopDBConnection();
        this.releaseSessionPermission();

        return groups;
    }

    /**
     *
     * @param group
     * @return A list of all documents belonging to a group.
     */
    public ArrayList<String> getGroupDocuments(String group) {
        ArrayList<String> files = null;

        this.getSessionPermission();
        this.startDBConnection();

        try {
            files = db.getGroupFiles(group);
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stopDBConnection();
        this.releaseSessionPermission();

        return files;
    }

    /**
     * Adds the document to the specified group.
     *
     * @param group
     * @param docPath
     */
    public void addDocumentToGroup(String group, String docPath) {
        this.getSessionPermission();
        this.startDBConnection();

        try {
            db.addFileToGroup(group, docPath);
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stopDBConnection();
        this.releaseSessionPermission();
    }

    /**
     * Adds a group to the database.
     *
     * @param group
     */
    public void addGroup(String group) {
        this.getSessionPermission();
        this.startDBConnection();

        try {
            db.addGroup(group);
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stopDBConnection();
        this.releaseSessionPermission();
    }

    /**
     * Removes a group from the database.
     *
     * @param group
     */
    public void removeGroup(String group) {
        this.getSessionPermission();
        this.startDBConnection();

        try {
            db.deleteGroup(group);
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stopDBConnection();
        this.releaseSessionPermission();
    }

    /**
     * Removes a document from a group.
     *
     * @param group
     * @param docPath
     */
    public void removeDocumentFromGroup(String group, String docPath) {
        this.getSessionPermission();
        this.startDBConnection();

        try {
            db.deleteFileFromGroup(group, docPath);
        } catch (ConnectionNotStartedException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stopDBConnection();
        this.releaseSessionPermission();
    }

    @Override
    public void rollbackIndexingSession() {
        try {
            lucene.rollbackIndexingSession();
        } catch (IOException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void rollbackDBConnection() {
        db.rollbackConnection();
    }

    class TagNode {

        int count;
        String tag;
        TagNode next;
        TagNode previous;

        public TagNode(String t, int c, TagNode n, TagNode p) {
            count = c;
            tag = t;
            next = n;
            previous = p;
        }
    }

}
