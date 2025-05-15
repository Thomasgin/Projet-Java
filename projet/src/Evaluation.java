import java.awt.image.BufferedImage;

/**
 * Use to calculate MSE and PSNR of denoise image in order to evaluate the quality of denoising
 */
public
public class Evaluation {
    /**
     * Default constructor
     */
    public Evaluation(){}
    /**
     * Calculate mse's value (mean squared error)
     * @param original original image
     * @param denoised denoised image
     * @return mse's value
     */
    public static double mse(BufferedImage original, BufferedImage denoised) {
        int width = original.getWidth();
        int height = original.getHeight();
        double sumSquaredError = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int orig = original.getRaster().getSample(x, y, 0);
                int denoisedVal = denoised.getRaster().getSample(x, y, 0);
                double diff = orig - denoisedVal;
                sumSquaredError += diff * diff;
            }
        }

        return sumSquaredError / (width * height);
    }

    /**
     * Calculate psnr's value (relation between signal and noise in peak)
     * @param mse mean squared error from the same case
     * @return psnr's value
     */
    public static double psnr(double mse) {
        if (mse == 0) return Double.POSITIVE_INFINITY;
        return 10 * Math.log10((255 * 255) / mse);
    }
}
