package utd.team6.workforceresearchguide.lucene;

import java.io.IOException;
import java.nio.file.FileSystems;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

//@author Michael Haertling
public class LuceneTest {

    public static void main(String[] args) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = FSDirectory.open(FileSystems.getDefault().getPath("test"));

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w = new IndexWriter(index, config);
        
        addDoc(w,"TestDocument","pickles");
        
        w.close();
        
        System.out.println(analyzer.getMaxTokenLength());
        analyzer.close();
        
        String querrystr = "";
        
    }

    public static void addDoc(IndexWriter w, String title, String tag) throws IOException{
        Document doc = new Document();
        //Use TextField for tokenized content
        //Use String Field for ids
        doc.add(new TextField("title",title,Field.Store.YES));
        doc.add(new StringField("tag",tag,Field.Store.YES));
        w.addDocument(doc);
    }
    
}
