package utd.team6.workforceresearchguide.lucene;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;



//@author Michael Haertling

public class LuceneSearchSession {
    
    DirectoryReader reader;
//    ExecutorService pool;
    IndexSearcher search;
    Query[] queries;
    Runnable[] threads;
    int queryIndex = 0;
    final int numTopScores;
    TopScoreDocCollector[] collectors;
    
    public LuceneSearchSession(DirectoryReader read,int numThreads,int numTopScores,Query... queries){
        this.reader = read;
//        pool = Executors.newFixedThreadPool(numThreads);
        search = new IndexSearcher(read);
        this.queries = queries;
        this.numTopScores = numTopScores;
    }
    
    public void startQuery(){
        threads = new Runnable[queries.length];
        
    }
    
    public TopDocs getCurrentDocs() throws IOException{
        TopDocs[] tds = new TopDocs[collectors.length];
        for(int i=0;i<collectors.length;i++){
            tds[i]=collectors[i].topDocs();
        }
        return TopDocs.merge(10, tds);
    }
    
//    public boolean queryComplete(){
//        
//    }
}
