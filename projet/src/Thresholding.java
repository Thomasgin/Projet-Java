

import java.util.ArrayList;
import java.util.List;

/**
 * Use to apply thresholding on results of ACP
 */
public class Thresholding {
    /**
     * Default constructor
     */
    public Thresholding(){}
    /**
     * Calculate Visu threshold (sigma * sqrt(2 * ln(n)))
     * @param sigma a constant
     * @param size from image
     * @return double
     */
    public static double seuilVisu(double sigma, int size) {
        return sigma * Math.sqrt(2 * Math.log(size));
    }

    /**
     * Calculate Bayes' threshold (sigma^2 / sigmaSignal)
     * @param sigma a constant
     * @param sigmaSignal value of signal from sigma
     * @return Float which is equals to Bayes' threshold
     */
    public static double seuilBayes(double sigma, double sigmaSignal) {
        if (sigmaSignal == 0) return 0;
        return 1.5*(sigma * sigma) / (sigmaSignal+0.00001);
    }

    /**
     * Calculate a globoal estimate of sigma_x from all projected contibutions
     * @param contributions is a matrix of contributions
     * @param sigma a constant
     * @return Float which is equals to estimated global sigma signal
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
     * Apply a soft thresholding
     * @param lambda a constant link to thresholding
     * @param x a value to compare to lambda
     * @return result of soft thresholding
     */
    public static double soft(double lambda, double x) {
        if (x > lambda) return x - lambda;
        else if (x < -lambda) return x + lambda;
        else return 0.0;
    }

    /**
     * Apply hard thresholding
     * @param lambda a constant link to thresholding
     * @param x a value to compare to lambda
     * @return result of hard thresholding
     */
    public static double hard(double lambda, double x) {
        return Math.abs(x) >= lambda ? x : 0.0;
    }

    /**
     * Apply thresholding (hard or soft) after calculation of lambda
     * @param contributions is a matrix of contributions
     * @param lambda a constant link to thresholding
     * @param isSoft a boolean which indicate what kind of thresholding we want to apply
     * @return result of the thresholding
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
     * Rebuilding of vectors from threshold contributions
     * @param contributionsSeuillees is a matrix of threshold contributions
     * @param base a matrix which represents original image
     * @param moyenne mean as a vector
     * @return List of double[] of rebuild vectors
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
                reconstruction[j] += moyenne[j]; // decentering
            }
            reconstructions.add(reconstruction);
        }

        return reconstructions;
    }
}
