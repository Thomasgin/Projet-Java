

import java.util.List;
import org.apache.commons.math3.linear.*;

/**
 * Use to apply ACP on each patchs which had been extracted from a noise image
 */
public class ACP {
    /**
     * Default constructor of ACP
     */
    public ACP(){}

    /**
     * Calculate ACP on vectors from patchs in order to isolate noise
     * @param V is a list of vectors
     * @return an ACPResult instance which contains results of ACP
     */
    public static ACPResult computeACP(List<double[]> V) {
        MoyCovResult stats = MoyCov(V);
        double[] moyenne = stats.moyenne;
        double[][] covariance = stats.covariance;

        // Convertir en RealMatrix
        RealMatrix covMatrix = new Array2DRowRealMatrix(covariance);

        // Décomposition en valeurs propres
        EigenDecomposition eig = new EigenDecomposition(covMatrix);
        int d = moyenne.length;

        // Récupérer les valeurs propres et vecteurs propres
        double[] valeurs = eig.getRealEigenvalues();
        double[][] base = new double[d][d]; // colonne i = vecteur propre i

        for (int i = 0; i < d; i++) {
            double[] vi = eig.getEigenvector(i).toArray();
            for (int j = 0; j < d; j++) {
                base[j][i] = vi[j];
            }
        }

        return new ACPResult(moyenne, base, valeurs);
    }

    /**
     * Calculate the mean of covariance from a List of double[]
     * @param V is a list of vectors
     * @return a MoyCovResult instance which contains results of calculation
     */
    public static MoyCovResult MoyCov(List<double[]> V) {
        int n = V.size();
        int d = V.get(0).length;

        double[] moyenne = new double[d];
        List<double[]> Vc = new java.util.ArrayList<>();

        // Mean
        for (double[] v : V) {
            for (int i = 0; i < d; i++) {
                moyenne[i] += v[i];
            }
        }
        for (int i = 0; i < d; i++) {
            moyenne[i] /= n;
        }

        // Centering
        for (double[] v : V) {
            double[] centered = new double[d];
            for (int i = 0; i < d; i++) {
                centered[i] = v[i] - moyenne[i];
            }
            Vc.add(centered);
        }

        // Covariance
        double[][] covariance = new double[d][d];
        for (double[] v : Vc) {
            for (int i = 0; i < d; i++) {
                for (int j = 0; j < d; j++) {
                    covariance[i][j] += v[i] * v[j];
                }
            }
        }
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                covariance[i][j] /= n;
            }
        }

        return new MoyCovResult(moyenne, covariance, Vc);
    }
    
    /**
     * Calculate the contribution
     * @param U is a matrix
     * @param Vc is a list of vectors
     * @return matrix which contains contributions
     */
    public static double[][] project(double[][] U, List<double[]> Vc) {
        int d = U.length;              // dimension d
        int s2 = U[0].length;          // nombre de composantes (s²)
        int M = Vc.size();             // nombre de vecteurs

        double[][] contributions = new double[M][s2];

        for (int k = 0; k < M; k++) {
            double[] vk = Vc.get(k);   // vecteur centré
            for (int i = 0; i < s2; i++) {
                double sum = 0.0;
                for (int j = 0; j < d; j++) {
                    sum += U[j][i] * vk[j];
                }
                contributions[k][i] = sum;
            }
        }

        return contributions;
    }
}
