package utd.team6.workforceresearchguide.main;

/**
 * This class stores a single document and the score it achieved over the query
 * it was found.
 *
 * @author Michael
 */
public class SearchResult implements Comparable<SearchResult> {

    private double tagScore;
    private double contentScore;
    private double aggregateScore;
    
    private final String filePath;

    /**
     * Creates a new SearchResult object.
     *
     * @param filePath
     * @param tagScore
     * @param contentScore
     */
    public SearchResult(String filePath, double tagScore, double contentScore) {
        this.filePath = filePath;
        this.tagScore = tagScore;
        this.contentScore = contentScore;
        calculateAggregate();
    }

    /**
     * Calculates the total, weighted score for this result.
     */
    private void calculateAggregate() {
        this.aggregateScore = tagScore * 10 + contentScore;
    }

    /**
     * Updates the tag score to the specified value.
     *
     * @param score
     */
    public void updateTagScore(double score) {
        this.tagScore = score;
        this.calculateAggregate();
    }

    /**
     * Updates the content score to the specified value.
     *
     * @param score
     */
    public void updateContentScore(double score) {
        this.contentScore = score;
        this.calculateAggregate();
    }
    
    /**
     *
     * @return
     */
    public double getTagScore() {
        return tagScore;
    }

    /**
     *
     * @return
     */
    public double getContentScore() {
        return contentScore;
    }

    /**
     *
     * @return
     */
    public double getAggregateScore() {
        return aggregateScore;
    }

    /**
     *
     * @return
     */
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int compareTo(SearchResult o) {
        double d = this.aggregateScore - o.aggregateScore;
        if (d == 0) {
            return 0;
        } else if (d > 0) {
            return -1;
        } else {
            return 1;
        }
    }

}
