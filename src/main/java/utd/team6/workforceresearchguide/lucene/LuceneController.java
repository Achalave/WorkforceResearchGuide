package utd.team6.workforceresearchguide.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
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
     * Creates a new LuceneController.
     */
    private LuceneController() {
        analyzer = new StandardAnalyzer();
    }

    /**
     *
     * @param lucenePath
     * @throws IOException
     */
    public LuceneController(String lucenePath) throws IOException {
        this();
        //Let Lucene determine what type of FSDirectory to use for the machine
        dir = FSDirectory.open(FileSystems.getDefault().getPath(lucenePath));
    }

    /**
     * Calls Util.extractAllPaths to get the document path hierarchy. Converts
     * the returned ArrayList hierarchy to the filePaths array.
     *
     *
     * @param path
     * @return String[]
     */
    public String[] getFilePaths(String path) {
        //get file heirarchy in documents path
        ArrayList<String> tempDocPaths = Utils.extractAllPaths(path);

        //convert ArrayList file heirarchy to String[]
        return tempDocPaths.toArray(new String[tempDocPaths.size()]);

        //System.out.println(Arrays.toString(filePaths));
    }

    /**
     * Start a new indexing session. This must be done before any Lucene
     * indexing can take place. Receives a boolean to determine if we are
     * creating a new index or updating an existing and sets config mode.
     *
     * @throws IOException
     */
    public void startIndexingSession() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
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
//            writer.commit();
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
        if (docs.scoreDocs.length == 0) {
            return null;
        }
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
        System.out.println("Tag document called: "+docPath);
        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }
        Document doc = getDocument(docPath);
        for(IndexableField field:doc.getFields()){
            System.out.println(field.name());
        }
        doc.add(new TextField("tag", tag, Store.YES));

        writer.updateDocument(new Term("path",docPath), doc);
    }

    /**
     * Removed the specified tag from the document.
     *
     * @param docPath
     * @param tag
     * @throws IOException
     * @throws ReadSessionNotStartedException
     * @throws
     * utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException
     */
    public void removeDocumentTag(String docPath, String tag) throws IOException, ReadSessionNotStartedException, IndexingSessionNotStartedException {
        if (writer == null) {
            throw new IndexingSessionNotStartedException();
        }
        Document doc = getDocument(docPath);
        Document docNew = new Document();
        if (doc == null) {
            return;
        }
        //Find and remove the specified tag
        for (IndexableField field : doc.getFields()) {
            if (!field.name().equals("tag") && !field.stringValue().equals(tag)) {
                docNew.add(field);
            } else {
                System.out.println("FOUND");
            }
        }
        //Update the document
//        writer.updateDocument(new Term("path", docPath), docNew);
    }

    /**
     * Removed all instances of the specified tag.
     *
     * @param tag
     * @throws IOException
     * @throws
     * utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException
     * @throws
     * utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException
     */
    public void removeTag(String tag) throws IOException, IndexingSessionNotStartedException, ReadSessionNotStartedException {
        if (reader == null) {
            throw new ReadSessionNotStartedException();
        }
        if (writer == null) {
            throw new IndexingSessionNotStartedException();
        }

        for (int i = 0; i < writer.maxDoc(); i++) {
            Document doc = reader.document(i);
            //Find and remove the specified tag
            Iterator<IndexableField> it = doc.iterator();
            String docPath = "";
            while (it.hasNext()) {
                IndexableField field = it.next();
                if (field.name().equals("tag") && field.stringValue().equals(tag)) {
                    it.remove();
                } else if (field.name().equals("path")) {
                    //Get the document path
                    docPath = field.stringValue();
                }
            }

            //Update the document
            writer.updateDocument(new Term("path", docPath), doc);
        }
    }

    /**
     * Re-indexes the content of the specified document.
     *
     * @param docPath
     * @throws IndexingSessionNotStartedException
     * @throws IOException
     * @throws ReadSessionNotStartedException
     * @throws TikaException
     */
    public void updateDocumentContent(String docPath) throws IndexingSessionNotStartedException, IOException, ReadSessionNotStartedException, TikaException {
        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }
        String content = Utils.readDocument(docPath);
        writer.updateDocValues(new Term("path", docPath), new TextField("content", content, Store.YES));
    }

    /**
     * Changes the document path of the specified document.
     *
     * @param oldPath
     * @param newPath
     * @throws IndexingSessionNotStartedException
     * @throws IOException
     * @throws ReadSessionNotStartedException
     */
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
