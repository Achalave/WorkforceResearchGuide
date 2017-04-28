/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gentest;

import java.io.IOException;
import java.nio.file.FileSystems;
import org.apache.lucene.index.DirectoryReader;
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
    
    public static void main(String[] args) throws IOException, InterruptedException{
        FSDirectory dir = FSDirectory.open(FileSystems.getDefault().getPath("_lucene_files_"));
        DirectoryReader reader = DirectoryReader.open(dir);
        IndexSearcher search = new IndexSearcher(reader);
        TopScoreDocCollector collect = TopScoreDocCollector.create(10);
        Query q = new TermQuery(new Term("content","dog"));
        
        System.out.println(reader.numDocs());
        
        System.out.println("Starting Search");
        
        search.search(q, collect);
//        TopDocs td = search.search(q, 10);
        
//        Thread.sleep(1000);
        
        System.out.println("Printing...");
        
        for(ScoreDoc doc:collect.topDocs().scoreDocs){
            System.out.println(doc.doc+" "+doc.score);
        }
    }
    
}