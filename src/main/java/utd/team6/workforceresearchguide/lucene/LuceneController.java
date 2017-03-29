package utd.team6.workforceresearchguide.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.tika.DocumentReader;

//@author Michael Haertling
// If possible it is best to reulse the IndexWriter as much as possible
public class LuceneController {

    private static int NUM_SEARCH_THREADS = 5;
    
    String filePath;
    FSDirectory dir;
    DocumentReader scan;
    StandardAnalyzer analyzer;

    IndexWriter writer;
    IndexWriterConfig config;

    private LuceneController() {
        scan = new DocumentReader();
        analyzer = new StandardAnalyzer();
        config = new IndexWriterConfig(analyzer);
    }

    public LuceneController(String lucenePath, String filePath) throws IOException {
        this();
        this.filePath = filePath;
        //Let Lucene determine what type of FSDirectory to use for the machine
        dir = FSDirectory.open(FileSystems.getDefault().getPath(lucenePath));
    }

    public void beginIndexingSession() throws IOException {
        writer = new IndexWriter(dir, config);
    }

    public void endIndexingSession() throws IOException {
        writer.close();
        writer = null;
    }

    public void indexNewDocument(String path) throws IOException, TikaException, IndexingSessionNotStartedException {

        if (writer == null) {
            throw new IndexingSessionNotStartedException();
        }

        Document doc = new Document();

        //Add the document path
        doc.add(new StringField("path", path, Store.YES));

        //Add the document name
        doc.add(new TextField("title", new File(path).getName(), Store.YES));

        //Scan the document text
        String text = scan.readDocument(path);
        doc.add(new TextField("content", text, Store.YES));

        try {
            writer.addDocument(doc);
        } catch (NullPointerException e) {
            throw new IndexingSessionNotStartedException();
        }
    }

    public void indexNewDocuments(String[] paths) throws IOException, TikaException, IndexingSessionNotStartedException {
        for (String path : paths) {
            indexNewDocument(path);
        }
    }

    public void deleteDocument(String path) throws IOException {
        writer.deleteDocuments(new Term("path",path));
    }

    public void search(String query) throws IOException {
        //TermQuery - Matches a single term (can be combined with BooleanQuery)
        //BooleanQuery
        //WildcardQuery
        //PhraseQuery - Matches a particular sequence of terms
        //PrefixQuery
        //MultiPhraseQuery
        
        //LeafReader.terms(String)
        
        
        //Build the query into multiple phases
        //Phase 1: tags, titles, dates
        //Phase 2: content
        MultiPhraseQuery.Builder phase1QueryBuilder = new MultiPhraseQuery.Builder();
        MultiPhraseQuery.Builder phase2QueryBuilder = new MultiPhraseQuery.Builder();
        String[] terms = query.split(" ");
        for(String term:terms){
            phase1QueryBuilder.add(new Term("tag",term));
            phase1QueryBuilder.add(new Term("title",term));
            phase2QueryBuilder.add(new Term("content",term));
        }
        
        MultiPhraseQuery phase1Query = phase1QueryBuilder.build();
        MultiPhraseQuery phase2Query = phase2QueryBuilder.build();
        
        
        DirectoryReader reader = DirectoryReader.open(dir);
        ExecutorService pool = Executors.newFixedThreadPool(NUM_SEARCH_THREADS);
        IndexSearcher searcher = new IndexSearcher(reader,pool);
        
        
        //SimpleCollector
        //TopDocsCollector
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(10);
        searcher.search(phase2Query, collector);
        TopDocs docs = searcher.search(phase1Query, 10);
    }

}
