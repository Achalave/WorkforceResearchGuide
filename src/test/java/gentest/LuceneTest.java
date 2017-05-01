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
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
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
        t2();
    }

    /**
     * Testing for the Lucene add tag bug.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void t1() throws IOException, InterruptedException {
        FSDirectory dir = FSDirectory.open(FileSystems.getDefault().getPath("_lucene_test_"));

        //Write the document
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));

        Document doc = new Document();

        doc.add(new TextField("content", "test\\test", Field.Store.YES));

        writer.addDocument(doc);

        writer.close();

        //Add the tag
        DirectoryReader reader = DirectoryReader.open(dir);
        writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));

        IndexSearcher search = new IndexSearcher(reader);
        Query q = new TermQuery(new Term("content", "test\\\\test"));
        TopDocs docs = search.search(q, 1);

        doc = reader.document(docs.scoreDocs[0].doc);
        doc.add(new TextField("tag", "tag test", Field.Store.YES));

        writer.updateDocument(new Term("content", "test\\test"), doc);
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
    }

    /**
     * Testing for the Lucene add tag bug.
     *
     * @param dir
     * @throws IOException
     */
    public static void printAll(FSDirectory dir) throws IOException {
        try (DirectoryReader r = DirectoryReader.open(dir)) {
            IndexSearcher search = new IndexSearcher(r);
            TopScoreDocCollector collect = TopScoreDocCollector.create(10);

            MatchAllDocsQuery all = new MatchAllDocsQuery();
            search.search(all, collect);
            TopDocs td = collect.topDocs();
            if (td.scoreDocs.length == 0) {
                System.out.println("NONE FOUND");
            } else {
                for (ScoreDoc sd : td.scoreDocs) {
                    Document doc = r.document(sd.doc);

                    for (IndexableField field : doc.getFields()) {
                        System.out.println("Stored: " + field.fieldType().stored());
                        if (!field.name().equals("content")) {
                            System.out.println(field.name() + "\t" + field.stringValue());
                        } else {
                            System.out.println(field.name());
                        }
                    }
                    System.out.println();
                }
            }
        }
    }

    /**
     * Testing for the Lucene add tag bug.
     *
     * @throws IOException
     */
    public static void t2() throws IOException {
        FSDirectory dir = FSDirectory.open(FileSystems.getDefault().getPath("_lucene_files_"));

        printAll(dir);

        IndexSearcher search;
        Query q;
        Document doc;
        String path = "C:.Users.Michael.Documents.NetBeansProjects.WorkforceResearchGuide.testdocs.Pets.dog.txt";
        String altPath = "doge";
        //Add the tag
        try (DirectoryReader reader = DirectoryReader.open(dir);
                IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            search = new IndexSearcher(reader);
            q = new TermQuery(new Term("path", path));
            TopDocs docs = search.search(q, 1);
            doc = reader.document(docs.scoreDocs[0].doc);
            doc.add(new TextField("tag", "tag test", Field.Store.YES));
            doc.removeField("path");
            doc.add(new StringField("path", path, Field.Store.YES));
//            doc.removeField("path");
//            doc.add(new TextField("path",altPath,Field.Store.YES));
            writer.updateDocument(new Term("path", path), doc);
        }

        //Check for the tag
        try (DirectoryReader r = DirectoryReader.open(dir)) {
            search = new IndexSearcher(r);
            TopScoreDocCollector collect = TopScoreDocCollector.create(10);
//            TermQuery q = new TermQuery(new Term("path", "C:\\Users\\Michael\\Documents\\NetBeansProjects\\WorkforceResearchGuide\\testdocs\\Pets\\dog.txt"));
//            TermQuery q = new TermQuery(new Term("path", "C:\\Users\\Michael\\Documents\\NetBeansProjects\\WorkforceResearchGuide\\testdocs\\Pets\\cats.txt"));
//            TermQuery q = new TermQuery(new Term("title", "dog.txt"));
            q = new TermQuery(new Term("path", path));

            MatchAllDocsQuery all = new MatchAllDocsQuery();
            search.search(q, collect);
            TopDocs td = collect.topDocs();
            if (td.scoreDocs.length == 0) {
                System.out.println("NONE FOUND");
            } else {
                for (ScoreDoc sd : td.scoreDocs) {
                    doc = r.document(sd.doc);

                    for (IndexableField field : doc.getFields()) {
                        System.out.println("Stored: " + field.fieldType().stored());
                        if (!field.name().equals("content")) {
                            System.out.println(field.name() + "\t" + field.stringValue());
                        } else {
                            System.out.println(field.name());
                        }
                    }
                    System.out.println();
                }
            }
        }

    }
}
