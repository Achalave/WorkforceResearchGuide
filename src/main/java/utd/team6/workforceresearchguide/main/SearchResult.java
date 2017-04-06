package utd.team6.workforceresearchguide.main;



//@author Michael Haertling

public class SearchResult implements Comparable<SearchResult>{
    
    private double tagScore;
    private double contentScore;
    private double aggregateScore;
    
    private final String filePath;
        
    public SearchResult(String filePath, double tagScore, double contentScore){
        this.filePath=filePath;
        this.tagScore = tagScore;
        this.contentScore = contentScore;
        calculateAggregate();
    }
    
    private void calculateAggregate(){
        this.aggregateScore = tagScore*10 + contentScore;
    }
    
    public void updateTagScore(double score){
        this.tagScore=score;
        this.calculateAggregate();
    }
    
    public void updateContentScore(double score){
        this.contentScore = score;
        this.calculateAggregate();
    }
    
    public double getTagScore() {
        return tagScore;
    }

    public double getContentScore() {
        return contentScore;
    }

    public double getAggregateScore() {
        return aggregateScore;
    }

    public String getFilePath() {
        return filePath;
    }
    
    @Override
    public int compareTo(SearchResult o) {
        double d = this.aggregateScore-o.aggregateScore;
        if(d==0){
            return 0;
        }else if(d>0){
            return 1;
        }else{
            return -1;
        }
    }
    
}
