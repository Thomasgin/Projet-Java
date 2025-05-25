import java.util.List;

/**
 * Represents the result of the mean and covariance calculation,
 * including principal components used in the denoising process.
 */
public class MoyCovResult {

    /**
     * The mean vector of the image patches or data points.
     */
    public double[] moyenne;

    /**
     * The covariance matrix representing data variability.
     */
    public double[][] covariance;

    /**
     * The list of principal component vectors obtained from PCA.
     */
    public List<double[]> Vc;

    /**
     * Constructs a result object containing the mean, covariance matrix,
     * and principal components.
     *
     * @param moyenne the mean vector
     * @param covariance the covariance matrix
     * @param Vc the list of principal component vectors
     */
    public MoyCovResult(double[] moyenne, double[][] covariance, List<double[]> Vc) {
        this.moyenne = moyenne;
        this.covariance = covariance;
        this.Vc = Vc;
    }
}
