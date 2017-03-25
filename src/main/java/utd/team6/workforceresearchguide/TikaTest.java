package utd.team6.workforceresearchguide;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;



//@author Michael Haertling

public class TikaTest {
    
    public static void main(String[] args) throws FileNotFoundException, IOException, TikaException{
        //https://tika.apache.org/1.8/examples.html
        String file = "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Semester 2\\SE Project\\test1.pdf";
        
        Tika tika = new Tika();
        try(InputStream fis = new FileInputStream(file)){
            String out = tika.parseToString(fis);
            System.out.println(out);
        }
    }
    
}
