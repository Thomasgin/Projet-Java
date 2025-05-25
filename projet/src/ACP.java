import java.util.List;
import org.apache.commons.math3.linear.*;

public class ACP {

    /**
     * Performs Principal Component Analysis on a list of vectors
     * @param V List of input vectors (each as double array)
     * @return ACPResult containing mean, eigenbasis and eigenvalues
     */
    public static ACPResult computeACP(List<double[]> V) {
        MoyCovResult stats = MoyCov(V);
        double[] moyenne = stats.moyenne;
        double[][] covariance = stats.covariance;

        RealMatrix covMatrix = new Array2DRowRealMatrix(covariance);

        EigenDecomposition eig = new EigenDecomposition(covMatrix);
        int d = moyenne.length;

        double[] valeurs = eig.getRealEigenvalues();
        double[][] base = new double[d][d];

        for (int i = 0; i < d; i++) {
            double[] vi = eig.getEigenvector(i).toArray();
            for (int j = 0; j < d; j++) {
                base[j][i] = vi[j];
            }
        }

        return new ACPResult(moyenne, base, valeurs);
    }

    /**
     * Computes mean and covariance matrix of input vectors
     * @param V List of input vectors
     * @return MoyCovResult containing mean, covariance and centered vectors
     */
    public static MoyCovResult MoyCov(List<double[]> V) {
        int n = V.size();
        int d = V.get(0).length;

        double[] moyenne = new double[d];
        List<double[]> Vc = new java.util.ArrayList<>();

        for (double[] v : V) {
            for (int i = 0; i < d; i++) {
                moyenne[i] += v[i];
            }
        }
        for (int i = 0; i < d; i++) {
            moyenne[i] /= n;
        }

        for (double[] v : V) {
            double[] centered = new double[d];
            for (int i = 0; i < d; i++) {
                centered[i] = v[i] - moyenne[i];
            }
            Vc.add(centered);
        }

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
     * Projects centered vectors onto principal components
     * @param U Matrix of eigenvectors (each column is an eigenvector)
     * @param Vc List of centered vectors
     * @return Matrix of projections (contributions to each component)
     */
    public static double[][] project(double[][] U, List<double[]> Vc) {
        int d = U.length;
        int s2 = U[0].length;
        int M = Vc.size();

        double[][] contributions = new double[M][s2];

        for (int k = 0; k < M; k++) {
            double[] vk = Vc.get(k);
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