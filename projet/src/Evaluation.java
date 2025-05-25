import java.awt.image.BufferedImage;

/**
 * Provides static methods to evaluate the quality of denoised images
 * using common metrics such as Mean Squared Error (MSE) and Peak Signal-to-Noise Ratio (PSNR).
 */
public class Evaluation {

    /**
     * Computes the Mean Squared Error (MSE) between the original and denoised images.
     *
     * @param original The original (noisy or ground truth) image.
     * @param denoised The denoised image to be evaluated.
     * @return The mean squared error between the two images.
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
     * Computes the Peak Signal-to-Noise Ratio (PSNR) given a mean squared error value.
     *
     * @param mse The mean squared error between the original and denoised images.
     * @return The PSNR value in decibels (dB). Returns positive infinity if MSE is 0.
     */
    public static double psnr(double mse) {
        if (mse == 0) return Double.POSITIVE_INFINITY;
        return 10 * Math.log10((255 * 255) / mse);
    }
}
