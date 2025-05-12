package denoiser;

import java.util.ArrayList;
import java.util.List;

public class Thresholding {

    // Seuil VisuShrink (sigma * sqrt(2 * ln(n)))
    public static double seuilVisu(double sigma, int size) {
        return sigma * Math.sqrt(2 * Math.log(size));
    }

    // Seuil BayesShrink (sigma^2 / sigmaSignal)
    public static double seuilBayes(double sigma, double sigmaSignal) {
        if (sigmaSignal == 0) return 0;
        return (sigma * sigma) / sigmaSignal;
    }

    // Estimation globale de sigma_x (σ_signal) à partir de toutes les contributions projetées
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

    // Fonction de seuillage doux (soft thresholding)
    public static double soft(double lambda, double x) {
        if (x > lambda) return x - lambda;
        else if (x < -lambda) return x + lambda;
        else return 0.0;
    }

    // Fonction de seuillage dur (hard thresholding)
    public static double hard(double lambda, double x) {
        return Math.abs(x) >= lambda ? x : 0.0;
    }

    // Fonction générique de seuillage (doux ou dur) une fois lambda calculé
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

    // Reconstruction des vecteurs à partir des contributions seuillées
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
                reconstruction[j] += moyenne[j]; // décentrage
            }
            reconstructions.add(reconstruction);
        }

        return reconstructions;
    }
}
