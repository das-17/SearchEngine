import org.apache.lucene.search.similarities.TFIDFSimilarity;

public class TFSimilarity extends TFIDFSimilarity {
//Implementation similiar to CLassicSimilarity
    @Override
    public float tf(float freq) {
        return (float) Math.sqrt(freq);
    }

    @Override
    public float idf(long docFreq, long docCount) {
        return (float) 1.0;
    }

    @Override
    public float lengthNorm(int length) {
        return (float) (1.0 / Math.sqrt(length));
    }
    
}
