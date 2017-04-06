package utd.team6.workforceresearchguide.lucene;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import utd.team6.workforceresearchguide.main.Utils;

//@author Michael Haertling
public class LuceneSearchSession {

    DirectoryReader reader;
//    ExecutorService pool;
    IndexSearcher search;

    String[] query;
    Query tagQuery;
    Query contentQuery;

    Thread tagThread;
    Thread contentThread;

    TopScoreDocCollector tagCollector;
    TopScoreDocCollector contentCollector;

    int queryIndex = 0;
    final int numTopScores;

    public LuceneSearchSession(DirectoryReader read, int numTopScores, String query) {
        this.reader = read;
//        pool = Executors.newFixedThreadPool(numThreads);
        search = new IndexSearcher(reader);
        this.numTopScores = numTopScores;
        //Split by whitespace
        this.query = query.split("\\s");
        generateTagQuery();
        generateContentQuery();
    }

    private void generateTagQuery() {
        BooleanQuery.Builder build = new BooleanQuery.Builder();
        for (String term : query) {
            build.add(new BooleanClause(new TermQuery(new Term("tag", term)), BooleanClause.Occur.SHOULD));
            build.add(new BooleanClause(new TermQuery(new Term("title", term)), BooleanClause.Occur.SHOULD));
        }
        tagQuery = build.build();
    }

    private void generateContentQuery() {
        BooleanQuery.Builder build = new BooleanQuery.Builder();
        for (String term : query) {
            build.add(new BooleanClause(new TermQuery(new Term("content", term)), BooleanClause.Occur.SHOULD));
        }
        contentQuery = build.build();
    }

    public void startSearch() {
        tagCollector = TopScoreDocCollector.create(numTopScores);
        contentCollector = TopScoreDocCollector.create(numTopScores);

        tagThread = new Thread() {
            @Override
            public void run() {
                try {
                    search.search(tagQuery, tagCollector);
                } catch (IOException ex) {
                    Logger.getLogger(LuceneSearchSession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        contentThread = new Thread() {
            @Override
            public void run() {
                try {
                    search.search(contentQuery, contentCollector);
                } catch (IOException ex) {
                    Logger.getLogger(LuceneSearchSession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        tagThread.start();
        contentThread.start();

    }

    public void cancelSearch(){
        if(tagThread.isAlive()){
            tagThread.interrupt();
        }
        
        if(contentThread.isAlive()){
            contentThread.interrupt();
        }
    }
    
    public TopDocs getTagHits(){
        return tagCollector.topDocs();
    }
    
    public TopDocs getContentHits(){
        return contentCollector.topDocs();
    }
    
    public Document getDocument(int docID) throws IOException{
        return reader.document(docID);
    }
    
//    public TopDocs getCurrentDocs() throws IOException{
//        TopDocs[] tds = new TopDocs[collectors.length];
//        for(int i=0;i<collectors.length;i++){
//            tds[i]=collectors[i].topDocs();
//        }
//        return TopDocs.merge(10, tds);
//    }
//    public boolean queryComplete(){
//        
//    }
}
