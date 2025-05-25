/**
 * Utility class providing helper methods for image denoising.
 * Includes operations such as adding Gaussian noise, extracting and reconstructing patches,
 * and dividing/recomposing images for local PCA-based denoising.
 */
public class ImageUtils {

    /**
     * Adds Gaussian noise to a grayscale image.
     *
     * @param X0 The original clean grayscale image.
     * @param sigma The standard deviation of the Gaussian noise to be added.
     * @return A new image with added Gaussian noise.
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
     * Extracts all s×s patches from the input image with stride 1.
     *
     * @param X The source image.
     * @param s The size of the square patches.
     * @return A list of patches extracted from the image.
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
     * Reconstructs a full image from overlapping patches using averaging.
     *
     * @param patches The list of patches with their original positions.
     * @param height The height of the output image.
     * @param width The width of the output image.
     * @return The reconstructed image.
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
     * Divides an image into overlapping square zones of size W×W, using a given step (stride).
     * Handles edge cases by including additional zones to cover the entire image.
     *
     * @param image The input image to be divided.
     * @param W The width and height of each square zone.
     * @param pas The step between top-left corners of adjacent zones.
     * @return A list of image zones with position information.
     */
    public static List<ImageZone> decoupeImage(BufferedImage X, int W, int n) {
        List<ImageZone> zones = new ArrayList<>();
        int width = X.getWidth();
        int height = X.getHeight();

        for (int y = 0; y <= height - W; y += n) {
            for (int x = 0; x <= width - W; x += n) {
                BufferedImage subImage = new BufferedImage(W, W, BufferedImage.TYPE_BYTE_GRAY);
                WritableRaster subRaster = subImage.getRaster();
                Raster originalRaster = X.getRaster();

                for (int j = 0; j < W; j++) {
                    for (int i = 0; i < W; i++) {
                        int val = originalRaster.getSample(x + i, y + j, 0);
                        subRaster.setSample(i, j, 0, val);
                    }
                }

                zones.add(new ImageZone(subImage, x, y));
            }
        }

        return zones;
    }

    /**
     * Converts a list of patches into vector representations with position information.
     *
     * @param patches The list of patches to be converted.
     * @return A list of vectors with position metadata.
     */
    public static List<VectorWithPosition> VectorPatchs(List<Patch> patches) {
        List<VectorWithPosition> result = new ArrayList<>();

        for (Patch patch : patches) {
            result.add(new VectorWithPosition(patch.toVector(), patch.positionX, patch.positionY));
        }

        return result;
    }

    /**
     * Reconstructs a full image from a list of overlapping zones by averaging overlapping pixel values.
     *
     * @param zones The list of image zones with their positions.
     * @param width The width of the output image.
     * @param height The height of the output image.
     * @return The reconstituted full image.
     */
     public static BufferedImage recomposeFromZones(List<ImageZone> zones, int fullWidth, int fullHeight) {
        int[][] sum = new int[fullHeight][fullWidth];
        int[][] count = new int[fullHeight][fullWidth];

        for (ImageZone zone : zones) {
            BufferedImage img = zone.getImage();
            Raster raster = img.getRaster();
            int[] pos = zone.getPosition();
            int offsetX = pos[0];
            int offsetY = pos[1];

            int w = img.getWidth();
            int h = img.getHeight();

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int val = raster.getSample(x, y, 0);
                    int globalX = offsetX + x;
                    int globalY = offsetY + y;

                    if (globalX < fullWidth && globalY < fullHeight) {
                        sum[globalY][globalX] += val;
                        count[globalY][globalX]++;
                    }
                }
            }
        }

        BufferedImage result = new BufferedImage(fullWidth, fullHeight, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster resultRaster = result.getRaster();

        for (int y = 0; y < fullHeight; y++) {
            for (int x = 0; x < fullWidth; x++) {
                int val = (count[y][x] == 0) ? 0 : (sum[y][x] / count[y][x]);
                resultRaster.setSample(x, y, 0, val);
            }
        }

        result.setData(resultRaster);
        return result;
    }

}
