import java.util.ArrayList;
import java.util.List;

/**
 * Provides thresholding methods used in the denoising process,
 * including VisuShrink and BayesShrink thresholds,
 * soft and hard thresholding functions, and reconstruction utilities.
 */
public class Thresholding {

    /**
     * Computes the VisuShrink threshold.
     * Formula: sigma * sqrt(2 * ln(size))
     *
     * @param sigma the noise standard deviation
     * @param size the size of the data (e.g., vector length)
     * @return the VisuShrink threshold value
     */
    public static double seuilVisu(double sigma, int size) {
        return sigma * Math.sqrt(2 * Math.log(size));
    }

    /**
     * Computes the BayesShrink threshold.
     * Formula: 1.5 * (sigma^2) / sigmaSignal (with a small epsilon to avoid division by zero)
     *
     * @param sigma the noise standard deviation
     * @param sigmaSignal the estimated signal standard deviation
     * @return the BayesShrink threshold value
     */
    public static double seuilBayes(double sigma, double sigmaSignal) {
        if (sigmaSignal == 0) return 0;
        return 1.5 * (sigma * sigma) / (sigmaSignal + 0.00001);
    }

    /**
     * Estimates the global signal standard deviation (sigmaSignal)
     * from all projected contributions.
     *
     * @param contributions the matrix of projected coefficients
     * @param sigma the noise standard deviation
     * @return the estimated global signal standard deviation
     */
    public static double estimateGlobalSigmaSignal(double[][] contributions, double sigma) {
        int M = contributions.length;
        int d = contributions[0].length;

        double mean = 0.0;
        for (double[] row : contributions)
            for (double x : row)
                mean += x;

        mean /= (M * d);

        double variance = 0.0;
        for (double[] row : contributions)
            for (double x : row)
                variance += (x - mean) * (x - mean);

        variance /= (M * d);

        double signalVariance = Math.max(variance - sigma * sigma, 0);
        return Math.sqrt(signalVariance);
    }

    /**
     * Applies soft thresholding to a single value.
     * Shrinks the value towards zero by lambda.
     *
     * @param lambda the threshold value
     * @param x the input value
     * @return the thresholded value after soft thresholding
     */
    public static double soft(double lambda, double x) {
        if (x > lambda) return x - lambda;
        else if (x < -lambda) return x + lambda;
        else return 0.0;
    }

    /**
     * Applies hard thresholding to a single value.
     * Sets values below lambda to zero.
     *
     * @param lambda the threshold value
     * @param x the input value
     * @return the thresholded value after hard thresholding
     */
    public static double hard(double lambda, double x) {
        return Math.abs(x) >= lambda ? x : 0.0;
    }

    /**
     * Applies soft or hard thresholding to all coefficients in the matrix.
     *
     * @param contributions the matrix of coefficients to threshold
     * @param lambda the threshold value
     * @param isSoft true to apply soft thresholding, false for hard thresholding
     * @return the matrix of thresholded coefficients
     */
    public static double[][] appliquerSeuillage(double[][] contributions, double lambda, boolean isSoft) {
        int M = contributions.length;
        int d = contributions[0].length;
        double[][] result = new double[M][d];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < d; j++) {
                result[i][j] = isSoft
                    ? soft(lambda, contributions[i][j])
                    : hard(lambda, contributions[i][j]);
            }
        }

        return result;
    }

    /**
     * Reconstructs vectors from thresholded contributions using the PCA basis and mean.
     *
     * @param contributionsSeuillees the matrix of thresholded contributions
     * @param base the PCA basis matrix
     * @param moyenne the mean vector used for centering
     * @return a list of reconstructed vectors after thresholding
     */
    public static List<double[]> reconstructionsDepuisContributions(
            double[][] contributionsSeuillees,
            double[][] base,
            double[] moyenne
    ) {
        int M = contributionsSeuillees.length;
        int d = moyenne.length;
        List<double[]> reconstructions = new ArrayList<>();

        for (int i = 0; i < M; i++) {
            double[] reconstruction = new double[d];
            for (int j = 0; j < d; j++) {
                for (int k = 0; k < d; k++) {
                    reconstruction[j] += base[j][k] * contributionsSeuillees[i][k];
                }
                reconstruction[j] += moyenne[j]; // add back the mean (de-centering)
            }
            reconstructions.add(reconstruction);
        }

        return reconstructions;
    }
}
