package denoiser;

import java.awt.image.BufferedImage;

public class Evaluation {

    // Erreur quadratique moyenne (MSE)
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

    // Rapport signal sur bruit en pic (PSNR)
    public static double psnr(double mse) {
        if (mse == 0) return Double.POSITIVE_INFINITY;
        return 10 * Math.log10((255 * 255) / mse);
    }
}
