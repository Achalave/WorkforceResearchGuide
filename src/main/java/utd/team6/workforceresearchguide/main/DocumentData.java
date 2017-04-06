package utd.team6.workforceresearchguide.main;

import java.util.Date;



//@author Michael Haertling

public class DocumentData {
    
    String path;
    String name;
    Date lastModDate;
    int hits;
    String hash;
    
    public DocumentData(String path, String name, Date date, int hits,String hash){
        this.path = path;
        this.name = name;
        this.lastModDate = date;
        this.hits = hits;
        this.hash = hash;
    }
    
}
