package denoiser;

import java.util.List;

public class MoyCovResult {
    public double[] moyenne;
    public double[][] covariance;
    public List<double[]> Vc;

    public MoyCovResult(double[] moyenne, double[][] covariance, List<double[]> Vc) {
        this.moyenne = moyenne;
        this.covariance = covariance;
        this.Vc = Vc;
    }
}
