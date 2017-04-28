package utd.team6.workforceresearchguide.lucene;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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

/**
 * An object of this class is returned when a Lucene search is started through a
 * LuceneController object. It can be used to manage the results of the search
 * as well as the search process itself.
 *
 * @author Michael
 */
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

    /**
     * Creates a new LuceneSearchSession.
     *
     * @param read
     * @param numTopScores
     * @param query
     */
    public LuceneSearchSession(DirectoryReader read, int numTopScores, String query) {
        this.reader = read;
//        pool = Executors.newFixedThreadPool(numThreads);
        search = new IndexSearcher(reader);
        this.numTopScores = numTopScores;

        //Check current query for testing
        System.out.println("Current Query: " + query);

        //attempt to expand current query with synonyms
        try {
            String expandedQuery = expandSearch(query);
            query = expandedQuery;
            //display expanded query for testing
            System.out.println("Expanded Query: " + query);
        } catch (IOException | ParseException ex) {
            //TODO: Handle exceptions
            System.out.println(ex);
            System.out.println("Failed to expand query. Using initial query.");
        }

        //Split query by whitespace
        this.query = query.split("\\s");
        generateTagQuery();
        generateContentQuery();
    }

    /**
     * Expand a search query using synonyms. This method uses the Lucene's
     * analyzers-common add-on library. NOTE:
     *
     * @param query
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    private String expandSearch(String query) throws FileNotFoundException, IOException, ParseException {

        //thesaurus file currently located in project directory
        //read synonym prolog file
        File synFile = new File("wn_s.pl");
        InputStream is = new FileInputStream(synFile);
        Reader reader = new InputStreamReader(is);

        //build synonym map
        SynonymMap.Builder parser = null;
        parser = new WordnetSynonymParser(true, true, new StandardAnalyzer(CharArraySet.EMPTY_SET));
        ((WordnetSynonymParser) parser).parse(reader);
        SynonymMap synMap = parser.build();

        //convert inital query to TokenStream
        //NOTE: the query is passed through a StandardAnalyzer which drops 
        //standard unimportant terms as determined by Lucene. 
        Analyzer analyzer = new StandardAnalyzer();
        TokenStream tstream = analyzer.tokenStream(null, new StringReader(query));

        //create SynonymGraphFilter from SynonymMap and current TokenStream
        SynonymGraphFilter filter = null;
        if (synMap != null) {
            filter = new SynonymGraphFilter(tstream, synMap, true);
        } else {
            return query;
        }

        //retrieve expanded query from SynonymGraphFilter to TokenStream
        Tokenizer source = new ClassicTokenizer();
        TokenStreamComponents components = new TokenStreamComponents(source, filter);
        TokenStream ts = components.getTokenStream();

        //OffsetAttribute offsetAttribute = ts.addAttribute(OffsetAttribute.class);
        CharTermAttribute termattr = ts.addAttribute(CharTermAttribute.class);
        ts.reset();

        String results = "";

        //iterate through the expanded query tokenstream and convert to String
        while (ts.incrementToken()) {
            //int startOffset = offsetAttribute.startOffset();
            //int endOffset = offsetAttribute.endOffset();
            results += termattr.toString() + " ";
        }
        ts.end();
        ts.close();

        //return expanded stream
        return results;
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

    /**
     * Begins the Lucene search.
     */
    public void startSearch() {
        tagCollector = TopScoreDocCollector.create(numTopScores);
        contentCollector = TopScoreDocCollector.create(numTopScores);

        System.out.println("THE SEARCH IS STARTING");

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

    /**
     * Cancels the Lucene search.
     */
    public void cancelSearch() {
        if (tagThread.isAlive()) {
            tagThread.interrupt();
        }

        if (contentThread.isAlive()) {
            contentThread.interrupt();
        }
    }

    /**
     *
     * @return The current results for the tag search.
     */
    public TopDocs getTagHits() {
        return tagCollector.topDocs();
    }

    /**
     *
     * @return
     */
    public TopDocs getContentHits() {
        return contentCollector.topDocs();
    }

    /**
     *
     * @param docID
     * @return
     * @throws IOException
     */
    public Document getDocument(int docID) throws IOException {
        return reader.document(docID);
    }

    /**
     *
     * @return True if the search is still in progress.
     */
    public boolean searchInProgress() {
        return tagThread.isAlive() && contentThread.isAlive();
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
