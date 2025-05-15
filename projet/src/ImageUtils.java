

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;

/**
 * Use to apply all methode on image as split it, extract patchs, apply noise on it, ...
 */
public class ImageUtils {
    /**
     * Default constructor
     */
    public ImageUtils(){}
    /**
     * Apply noise depending on sigma on a image
     * @param X0 a BufferedImage where we work on it
     * @param sigma a constant
     * @return a noise image
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

    /**
     * Extract patchs from image
     * @param X a BufferedImage
     * @param s a constant which is the width of patchs
     * @return A List of Patch which contains all patchs
     */
    public static List<Patch> extractPatches(BufferedImage X, int s) {
        List<Patch> patches = new ArrayList<>();
        Raster raster = X.getRaster();

        int width = X.getWidth();
        int height = X.getHeight();
        int half = s / 2;
        int start = (s % 2 == 0) ? -half : -half;
        int end   = (s % 2 == 0) ? half - 1 : half;

        for (int y = half; y < height - half; y++) {
            for (int x = half; x < width - half; x++) {
                double[] patchData = new double[s * s];
                int idx = 0;
                for (int dy = start; dy <= end; dy++) {
                    for (int dx = start; dx <= end; dx++) {
                        int val = raster.getSample(x + dx, y + dy, 0);
                        patchData[idx++] = val;
                    }
                }
                patches.add(new Patch(patchData, x, y));
            }
        }

        return patches;
    }
    
    /**
     * Rebuild an image from a List of Patch
     * @param patches a list of all patchs
     * @param height of original image
     * @param width of the original image
     * @return A BufferImage which had been rebuilt from patchs
     */
    public static BufferedImage reconstructPatches(List<Patch> patches, int height, int width) {
        int[][] sum = new int[height][width];
        int[][] count = new int[height][width];

        int patchSize = (int) Math.sqrt(patches.get(0).data.length);
        int half = patchSize / 2;
        int start = (patchSize % 2 == 0) ? -half : -half;
        int end   = (patchSize % 2 == 0) ? half - 1 : half;

        for (Patch patch : patches) {
            double[] data = patch.data;
            int x0 = patch.positionX;
            int y0 = patch.positionY;

            int idx = 0;
            for (int dy = start; dy <= end; dy++) {
                for (int dx = start; dx <= end; dx++) {
                    int x = x0 + dx;
                    int y = y0 + dy;

                    if (x >= 0 && x < width && y >= 0 && y < height) {
                        sum[y][x] += (int) Math.round(data[idx]);
                        count[y][x]++;
                    }
                    idx++;
                }
            }
        }


        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = result.getRaster();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = (count[y][x] == 0) ? 0 : (sum[y][x] / count[y][x]);
                val = Math.max(0, Math.min(255, val));
                raster.setSample(x, y, 0, val);
            }
        }

        return result;
    }
    /**
     * Split an image in sub-images
     * @param X a BufferdImage
     * @param W a constant to delimitation working zone
     * @param n gap between cut in original image X
     * @return A List of ImageZone which contains coordinates of all sub-images
     */
    public static List<ImageZone> decoupeImage(BufferedImage X, int W, int n) {
        List<ImageZone> zones = new ArrayList<>();
        int width = X.getWidth();
        int height = X.getHeight();

        for (int y = 0; y <= height - W; y += n) {
            for (int x = 0; x <= width - W; x += n) {
                BufferedImage subImage = X.getSubimage(x, y, W, W);
                zones.add(new ImageZone(subImage, x, y));
            }
        }

        return zones;
    }

    /**
     * Vectorise patchs
     * @param patches is a list of patchs
     * @return A List of VectorWithPosition
     */
    public static List<VectorWithPosition> VectorPatchs(List<Patch> patches) {
        List<VectorWithPosition> result = new ArrayList<>();

        for (Patch patch : patches) {
            result.add(new VectorWithPosition(patch.toVector(), patch.positionX, patch.positionY));
        }

        return result;
    }
    
    /**
     * Calculate MSE between two images, pixel by pixel
     * @param img1 a BufferedImage
     * @param img2 a BufferedImage
     * @return float MSE
     */
    public static double computeMSE(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        double mse = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = img1.getRaster().getSample(x, y, 0);
                int pixel2 = img2.getRaster().getSample(x, y, 0);
                double diff = pixel1 - pixel2;
                mse += diff * diff;
            }
        }

        mse /= (width * height);
        return mse;
    }
    
    /**
     * Camculate PSNR between two images, pixel by pixel
     * @param mse aka mean squared error from the same case
     * @return Float psnr
     */
    public static double computePSNR(double mse) {
        if (mse == 0) {
            return Double.POSITIVE_INFINITY; // Same images
        }
        return 10 * Math.log10((255 * 255) / mse);
    }



}
