package denoiser;

import java.util.ArrayList;
import java.util.List;

public class Thresholding {

    // Seuil VisuShrink
    public static double seuilVisu(double sigma, int size) {
        return sigma * Math.sqrt(2 * Math.log(size));
    }

    // Seuil BayesShrink
    public static double seuilBayes(double sigma, double sigmaSignal) {
        if (sigmaSignal == 0) return 0;
        return (sigma * sigma) / sigmaSignal;
    }

    // Estimation de σ_x à partir des contributions projetées (pour un axe donné)
    public static double estimateSigmaSignal(List<Double> contributions, double sigma) {
        int n = contributions.size();
        double mean = 0.0;
        for (double x : contributions) {
            mean += x;
        }
        mean /= n;

        double variance = 0.0;
        for (double x : contributions) {
            variance += (x - mean) * (x - mean);
        }
        variance /= n;

        double signalVariance = Math.max(variance - sigma * sigma, 0);
        return Math.sqrt(signalVariance);
    }

    public static double soft(double lambda, double x) {
        if (x > lambda) return x - lambda;
        else if (x < -lambda) return x + lambda;
        else return 0.0;
    }

    public static double hard(double lambda, double x) {
        return Math.abs(x) >= lambda ? x : 0.0;
    }

    public static double[][] seuillageDouxVisu(double[][] contributions, double lambda) {
        int M = contributions.length;
        int d = contributions[0].length;
        double[][] result = new double[M][d];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < d; j++) {
                result[i][j] = soft(lambda, contributions[i][j]);
            }
        }

        return result;
    }

    public static double[][] seuillageDurVisu(double[][] contributions, double lambda) {
        int M = contributions.length;
        int d = contributions[0].length;
        double[][] result = new double[M][d];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < d; j++) {
                result[i][j] = hard(lambda, contributions[i][j]);
            }
        }

        return result;
    }

    // Seuillage doux par BayesShrink, axe par axe
    public static double[][] seuillageDouxBayes(double[][] contributions, double sigma) {
        int M = contributions.length;
        int d = contributions[0].length;
        double[][] result = new double[M][d];

        for (int j = 0; j < d; j++) {
            List<Double> contribsJ = new ArrayList<>();
            for (int i = 0; i < M; i++) {
                contribsJ.add(contributions[i][j]);
            }

            double sigmaSignal = estimateSigmaSignal(contribsJ, sigma);
            double lambdaJ = sigmaSignal == 0 ? Double.POSITIVE_INFINITY : seuilBayes(sigma, sigmaSignal);


            for (int i = 0; i < M; i++) {
                result[i][j] = soft(lambdaJ, contributions[i][j]);
            }
        }

        return result;
    }

    // Seuillage dur par BayesShrink, axe par axe
    public static double[][] seuillageDurBayes(double[][] contributions, double sigma) {
        int M = contributions.length;
        int d = contributions[0].length;
        double[][] result = new double[M][d];

        for (int j = 0; j < d; j++) {
            List<Double> contribsJ = new ArrayList<>();
            for (int i = 0; i < M; i++) {
                contribsJ.add(contributions[i][j]);
            }

            double sigmaSignal = estimateSigmaSignal(contribsJ, sigma);
            double lambdaJ = seuilBayes(sigma, sigmaSignal);

            for (int i = 0; i < M; i++) {
                result[i][j] = hard(lambdaJ, contributions[i][j]);
            }
        }

        return result;
    }

    // Reconstruire les vecteurs centrés dans l’espace d’origine
    public static List<double[]> reconstructionsDepuisContributions(
            double[][] contributionsSeuillees,
            double[][] base, // base propre
            double[] moyenne // moyenne calculée dans l'ACP
    ) {
        int M = contributionsSeuillees.length; // nombre de patchs
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
