package denoiser;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;

public class ImageUtils {

    /**
     * Ajoute un bruit gaussien N(0, sigma^2) à une image grayscale.
     * 
     * @param X0    Image d'entrée (BufferedImage en niveau de gris)
     * @param sigma Ecart-type du bruit à ajouter
     * @return      Image bruitée (BufferedImage)
     */
    public static BufferedImage noising(BufferedImage X0, double sigma) {
        int width = X0.getWidth();
        int height = X0.getHeight();

        BufferedImage noisy = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster inputRaster = X0.getRaster();
        WritableRaster noisyRaster = noisy.getRaster();

        Random rand = new Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = inputRaster.getSample(x, y, 0);
                double noise = sigma * rand.nextGaussian();
                int noisyPixel = (int) Math.round(pixel + noise);

                noisyPixel = Math.max(0, Math.min(255, noisyPixel));

                noisyRaster.setSample(x, y, 0, noisyPixel);
            }
        }

        noisy.setData(noisyRaster);
        return noisy;
    }
}
