import java.util.List;

/**
 * Use to store results of mean of covariance
 */
public class MoyCovResult {
    /**
     * Vector which contains mean
     */
    public double[] moyenne;
    /**
     * A covariance matrix
     */
    public double[][] covariance;
    /**
     * A list of contribution vectors
     */
    public List<double[]> Vc;

    /**
     * Constructor of MoyCovResult
     * @param moyenne mean
     * @param covariance covariance
     * @param Vc a list of vectors
     */
    public MoyCovResult(double[] moyenne, double[][] covariance, List<double[]> Vc) {
        this.moyenne = moyenne;
        this.covariance = covariance;
        this.Vc = Vc;
    }
}
