package utd.team6.workforceresearchguide.main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.LuceneSearchSession;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;



//@author Michael Haertling

public class ApplicationController {
    
    private static final String LUCENE_FILE_PATH = "_lucene_files_";
    private static final String DATABASE_PATH = "lucene.db";
    private static final long SEARCH_RESULT_UPDATE_DELAY = 500;
    
    LuceneController lucene;
    DatabaseController db;
    
    boolean searchInProgress = false;
    LuceneSearchSession search;
    TimerTask searchUpdater;
    HashMap<Integer,SearchResult> results;
    
    Timer applicationTimer;
    
    public ApplicationController(){
        try {
            lucene = new LuceneController(LUCENE_FILE_PATH);
            db = new DatabaseController(DATABASE_PATH);
        } catch (IOException | SQLException ex) {
            Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        applicationTimer = new Timer(true);
               
        
    }
    
    /**
     * Creates the initial lucene index based on provided root file directory.
     * 
     *
     * @param path
     * @throws IOException
     * @throws TikaException
     * @throws IndexingSessionNotStartedException
     */
    public void createIndex(String path) throws IOException, TikaException, 
            IndexingSessionNotStartedException {
        
        //Get file hierarchy
        String[] filePaths = lucene.getFilePaths(path);
        
        //set indexing session to create NEW index
        lucene.startIndexingSession(true);
        lucene.startReadSession();
        
        //indexes files
        lucene.indexNewDocuments(filePaths);
        
        lucene.stopIndexingSession();
        lucene.stopReadSession();

    }
    
    
    /**
     * Adds files to the existing lucene index from provided String[].
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
        lucene.startIndexingSession(false);
        lucene.startReadSession();
        
        //indexes files
        lucene.indexNewDocuments(filePaths);
        
        lucene.stopIndexingSession();
        lucene.stopReadSession();

    }
    
    public void beginSearch(String query) throws IOException, ReadSessionNotStartedException{
        lucene.startIndexingSession(false);
        search = lucene.search(query, 100);
        search.startSearch();
        //Instantiate the result set
        results = new HashMap<>();
        //Start the update timer
        searchUpdater = new TimerTask(){
            @Override
            public void run() {
                updateSearchResults();
            }
        };
        applicationTimer.scheduleAtFixedRate(searchUpdater, SEARCH_RESULT_UPDATE_DELAY, SEARCH_RESULT_UPDATE_DELAY);
    }
    
    /**
     * Cancels the current ongoing search session.
     */
    public void cancelSearch(){
        //Stop periodic updates
        searchUpdater.cancel();
        search.cancelSearch();
        search = null;
        //Change the GUI to reflect the cancelation
    }
    
    /**
     * This function is called periodically in order to collect and update
     * search results during a search session.
     */
    public void updateSearchResults(){
        
    }
    
    /**
     * Collects all search results into a hash map of SearchResult objects. The
     * key is the Lucene document id, which is subject to change between search
     * sessions.
     * @param results
     * @throws IOException 
     */
    public void aggregateResultSet(HashMap<Integer,SearchResult> results) throws IOException{
        TopDocs docs = search.getTagHits();
        for(ScoreDoc score:docs.scoreDocs){
            SearchResult result = results.get(score.doc);
            if(result == null){
                Document doc = search.getDocument(score.doc);
                result = new SearchResult(doc.get("path"),score.score,0);
                results.put(score.doc, result);
            }else{
                result.updateTagScore(score.score);
            }
        }
        
        docs = search.getContentHits();
        for(ScoreDoc score:docs.scoreDocs){
            SearchResult result = results.get(score.doc);
            if(result == null){
                Document doc = search.getDocument(score.doc);
                result = new SearchResult(doc.get("path"),0,score.score);
                results.put(score.doc, result);
            }else{
                result.updateContentScore(score.score);
            }
        }
    }
    
    
    
    
    public void importFile(String filePath){
        
    }
    
}
