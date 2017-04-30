/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gentest;

import java.io.IOException;
import java.nio.file.FileSystems;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author ru5h
 */
public class LuceneTest {

    /**
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        FSDirectory dir = FSDirectory.open(FileSystems.getDefault().getPath("_lucene_test_"));
        
        //Write the document
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));

        Document doc = new Document();

        doc.add(new TextField("content", "test", Field.Store.YES));

        writer.addDocument(doc);

        writer.close();

        //Add the tag
        DirectoryReader reader = DirectoryReader.open(dir);
        writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));

        IndexSearcher search = new IndexSearcher(reader);
        Query q = new TermQuery(new Term("content", "test"));
        TopDocs docs = search.search(q,1);

        doc = reader.document(docs.scoreDocs[0].doc);
        doc.add(new TextField("tag", "tag test", Field.Store.YES));

        writer.updateDocument(new Term("content", "test"), doc);

        writer.close();
        reader.close();
        

        doc = null;

        //Check if the document can still be found
        reader = DirectoryReader.open(dir);
        search = new IndexSearcher(reader);
        TopScoreDocCollector collect = TopScoreDocCollector.create(1);
        q = new TermQuery(new Term("content", "test"));
        search.search(q, collect);

        doc = reader.document(collect.topDocs().scoreDocs[0].doc);

        for (IndexableField field : doc.getFields()) {
            System.out.println(field.name() + "\t" + field.stringValue());
        }

        reader.close();
        writer.close();
    }

}
