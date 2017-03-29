package utd.team6.workforceresearchguide.tika;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;



//@author Michael Haertling

public class DocumentReader {
    
    public String readDocument(String path) throws IOException, TikaException{
        String file = "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Semester 2\\SE Project\\test1.pdf";
        
        Tika tika = new Tika();
        try(InputStream fis = new FileInputStream(path)){
            String out = tika.parseToString(fis);
            return out;
        }
    }
    
}
