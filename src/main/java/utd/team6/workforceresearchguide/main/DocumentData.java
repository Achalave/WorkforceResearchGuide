package utd.team6.workforceresearchguide.main;

import java.util.Date;



//@author Michael Haertling

public class DocumentData {
    
    String path;
    String name;
    Date lastModDate;
    int hits;
    String hash;
    
    public DocumentData(){
        
    }
    
    public DocumentData(String path, String name, Date date, int hits,String hash){
        this.path = path;
        this.name = name;
        this.lastModDate = date;
        this.hits = hits;
        this.hash = hash;
    }
    
    public boolean dataComplete(){
        return path!=null && name!=null && lastModDate!=null && hash!=null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastModDate() {
        return lastModDate;
    }

    public void setLastModDate(Date lastModDate) {
        this.lastModDate = lastModDate;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
    
    
    
}
