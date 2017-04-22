package utd.team6.workforceresearchguide.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.main.Utils;

//@author Michael Haertling
/**
 * This class handles all interactions with the Lucene API. It should be
 * instantiated with a path to a directory containing Lucene index files. Search
 * and read sessions must be stated and stopped before certain functions are
 * called. These sessions should be reused as much as possible.
 *
 * @author Michael
 */
public class LuceneController {

    FSDirectory dir;
    StandardAnalyzer analyzer;

    IndexWriter writer;

    DirectoryReader reader;

    /**
     * Used to determine if we are creating a new index or updating existing. If
     * set to true, the index files will be overwritten.
     */
    boolean createOnly = false;

    private LuceneController() {
        analyzer = new StandardAnalyzer();
    }

    public LuceneController(String lucenePath) throws IOException {
        this();
        //Let Lucene determine what type of FSDirectory to use for the machine
        dir = FSDirectory.open(FileSystems.getDefault().getPath(lucenePath));
    }

    public static void main(String[] args) throws IOException, TikaException, IndexingSessionNotStartedException, ReadSessionNotStartedException {
        LuceneController cont = new LuceneController("_lucene_files_");

//        //index testing:joharteaga
//        //set documents path
//        String docDir = "C:\\testdocs";
//        cont.startIndexingSession();
//        //get file heirarchy in documents path
//        ArrayList<String> tempDocPaths = Utils.extractAllPaths(docDir);
//        //convert file heirarchy to String[]
//        String[] docPaths = new String[tempDocPaths.size()];
//        docPaths = tempDocPaths.toArray(docPaths);
//        cont.indexNewDocuments(docPaths);
        String filePaths[] = {
            "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Semester 2\\CV Readings\\Attached at the Hip.docx",
            "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Semester 2\\CV Readings\\LifeDegredationPlan.docx",
            "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Semester 2\\CV Readings\\ItsComplicated.pdf"};
        //System.out.println(DocumentReader.readDocument(filePaths[0]));

//        cont.startIndexingSession();
//        for (String fpath : filePaths) {
//            cont.indexNewDocument(fpath);
//        }
//        cont.stopIndexingSession();
//
//        cont.startReadSession();
//        cont.startIndexingSession();
//        
//        cont.tagDocument(filePaths[0], "tag1");
//        cont.tagDocument(filePaths[0], "tag2");
//        cont.tagDocument(filePaths[1], "tag1");
//        cont.tagDocument(filePaths[2], "tag2");
//        
//        cont.stopReadSession();
//        cont.stopIndexingSession();
//        cont.startIndexingSession();
//        cont.deleteDocument(filePaths[0]);
//        cont.stopIndexingSession();
        cont.startReadSession();
        LuceneSearchSession sess = cont.search("There are several potential benifits", 10);
        sess.startSearch();
//        cont.basicSearchTest("There are several potential benifits");
//        cont.stopReadSession();

//        long time = System.currentTimeMillis();
//        String files = "C:\\Users\\Michael\\Downloads\\TESTDOCS\\TESTDOCS";
//        ArrayList<String> paths = Utils.extractAllPaths(files);
//
//        cont.startIndexingSession();
//        for (int i = 0; i < paths.size(); i++) {
//            long time2 = System.currentTimeMillis();
//            try {
//                cont.indexNewDocument(paths.get(i));
//            } catch (TikaException ex) {
//                System.err.println(ex);
//            }
//            System.out.println("DocNum: "+i+"/"+paths.size()+"\tTime: "+(System.currentTimeMillis()-time2));
//        }
//        long time2 = System.currentTimeMillis();
//        cont.stopIndexingSession();
//        System.out.println("END SESSION: " + (System.currentTimeMillis() - time2));
//        System.out.println(System.currentTimeMillis() - time);
    }

    /**
     * Start a new indexing session. This must be done before any Lucene
     * indexing can take place.
     *
     * @throws IOException
     */
    public void startIndexingSession() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

//        //testing index updating
//      create = false;    
        if (createOnly) {
            //create new index (drops any existing index)
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            //add to existing index
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        writer = new IndexWriter(dir, config);
    }

    /**
     * Ends a previously started indexing session. If no session is currently
     * active, this function will do nothing.
     *
     * @throws IOException
     */
    public void stopIndexingSession() throws IOException {
        if (writer != null) {
            writer.commit();
            writer.close();
            writer = null;
        }
    }

    /**
     * Starts a new reading session. A reading session must be active for
     * certain functions to work.
     *
     * @throws IOException
     */
    public void startReadSession() throws IOException {
        reader = DirectoryReader.open(dir);
    }

    /**
     * Ends a previously started reading session. If no session is currently
     * active, this function will do nothing.
     *
     * @throws IOException
     */
    public void stopReadSession() throws IOException {
        if (readSessionActive()) {
            reader.close();
            reader = null;
        }
    }

    /**
     * Ends a previously started indexing session and reverts any changes that
     * occurred within that session.
     *
     * @throws IOException
     */
    public void rollbackIndexingSession() throws IOException {
        if (indexingSessionActive()) {
            writer.rollback();
            writer = null;
        }
    }

    /**
     * Returns true if a read session is currently active.
     *
     * @return
     */
    public boolean readSessionActive() {
        return reader != null;
    }

    /**
     * Returns true if an indexing session is currently active.
     *
     * @return
     */
    public boolean indexingSessionActive() {
        return writer != null;
    }

    /**
     * Indexes a new document and adds it to the Lucene directory. An indexing
     * session must be active when this function is called.
     *
     * @param path
     * @throws IOException
     * @throws TikaException
     * @throws IndexingSessionNotStartedException
     */
    public void indexNewDocument(String path) throws IOException, TikaException, IndexingSessionNotStartedException {

        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }

        Document doc = new Document();

        //Add the document path
        doc.add(new StringField("path", path, Store.YES));

        //Add the document name
        doc.add(new TextField("title", new File(path).getName(), Store.YES));

        //Scan the document text
        String text = Utils.readDocument(path);
        if (!text.isEmpty()) {
            doc.add(new TextField("content", text, Store.YES));
        }

        try {
            writer.addDocument(doc);
        } catch (NullPointerException e) {
            throw new IndexingSessionNotStartedException();
        }
    }

    /**
     * Indexes multiple documents and adds them to the Lucene directory. An
     * indexing session must be active when this function is called.
     *
     * @param paths
     * @throws IOException
     * @throws TikaException
     * @throws IndexingSessionNotStartedException
     */
    public void indexNewDocuments(String[] paths) throws IOException, TikaException, IndexingSessionNotStartedException {
        for (String path : paths) {
            indexNewDocument(path);
        }
    }

    /**
     * Removes a document from the Lucene directory. An indexing session must be
     * active when this function is called.
     *
     * @param path
     * @throws IOException
     * @throws
     * utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException
     */
    public void deleteDocument(String path) throws IOException, IndexingSessionNotStartedException {
        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }
        writer.deleteDocuments(new Term("path", path));
    }

    /**
     * Get the Document object the corresponds to the specified document path. A
     * read session must be active.
     *
     * @param docPath
     * @return
     * @throws IOException
     */
    private Document getDocument(String docPath) throws IOException, ReadSessionNotStartedException {
        if (!readSessionActive()) {
            throw new ReadSessionNotStartedException();
        }
        IndexSearcher search = new IndexSearcher(reader);
        Query query = new TermQuery(new Term("path", docPath));
        TopDocs docs = search.search(query, 1);
        return reader.document(docs.scoreDocs[0].doc);
    }

    /**
     * Appends a new tag field to the specified document. Both a read and
     * indexing session must be active.
     *
     * @param docPath
     * @param tag
     * @throws java.io.IOException
     * @throws
     * utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException
     * @throws
     * utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException
     */
    public void tagDocument(String docPath, String tag) throws IOException, IndexingSessionNotStartedException, ReadSessionNotStartedException {
        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }
        Document doc = getDocument(docPath);
        doc.add(new TextField("tag", tag, Store.YES));
        writer.updateDocument(new Term("path", docPath), doc);
    }

    public void updateDocumentContent(String docPath) throws IndexingSessionNotStartedException, IOException, ReadSessionNotStartedException, TikaException {
        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }
        String content = Utils.readDocument(docPath);
        writer.updateDocValues(new Term("path", docPath), new TextField("content", content, Store.YES));
    }

    public void updateDocumentPath(String oldPath, String newPath) throws IndexingSessionNotStartedException, IOException, ReadSessionNotStartedException {
        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }
        writer.updateDocValues(new Term("path", oldPath), new TextField("path", newPath, Store.YES));
    }

    /**
     * Conducts a search based off a String query. This search function uses
     * BooleanQueries created from the whitespace separated terms within the
     * provided query.
     *
     * @param query
     * @param numTopScores
     * @return
     * @throws IOException
     * @throws
     * utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException
     */
    public LuceneSearchSession search(String query, int numTopScores) throws IOException, ReadSessionNotStartedException {
        if (!readSessionActive()) {
            throw new ReadSessionNotStartedException();
        }

        LuceneSearchSession search = new LuceneSearchSession(reader, numTopScores, query);
        return search;
    }

}
