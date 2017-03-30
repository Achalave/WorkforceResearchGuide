package utd.team6.workforceresearchguide.main;

import java.sql.Date;



//@author Michael Haertling

public class DocumentData {
    
    String path;
    String name;
    Date lastModDate;
    int hits;
    
    public DocumentData(String path, String name, Date date, int hits){
        this.path = path;
        this.name = name;
        this.lastModDate = date;
        this.hits = hits;
    }
    
}
