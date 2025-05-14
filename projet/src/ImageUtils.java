

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;


public class ImageUtils {
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
    
    public static List<VectorWithPosition> VectorPatchs(List<Patch> patches) {
        List<VectorWithPosition> result = new ArrayList<>();

        for (Patch patch : patches) {
            result.add(new VectorWithPosition(patch.toVector(), patch.positionX, patch.positionY));
        }

        return result;
    }
    
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
    
    public static double computePSNR(double mse) {
        if (mse == 0) {
            return Double.POSITIVE_INFINITY; // Images identiques
        }
        return 10 * Math.log10((255 * 255) / mse);
    }



}
