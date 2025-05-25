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
    public static BufferedImage noising(BufferedImage X0, double sigma) { ... }

    /**
     * Extracts all s×s patches from the input image with stride 1.
     *
     * @param X The source image.
     * @param s The size of the square patches.
     * @return A list of patches extracted from the image.
     */
    public static List<Patch> extractPatches(BufferedImage X, int s) { ... }

    /**
     * Reconstructs a full image from overlapping patches using averaging.
     *
     * @param patches The list of patches with their original positions.
     * @param height The height of the output image.
     * @param width The width of the output image.
     * @return The reconstructed image.
     */
    public static BufferedImage reconstructPatches(List<Patch> patches, int height, int width) { ... }

    /**
     * Divides an image into overlapping square zones of size W×W, using a given step (stride).
     * Handles edge cases by including additional zones to cover the entire image.
     *
     * @param image The input image to be divided.
     * @param W The width and height of each square zone.
     * @param pas The step between top-left corners of adjacent zones.
     * @return A list of image zones with position information.
     */
    public static List<ImageZone> decoupeImage(BufferedImage image, int W, int pas) { ... }

    /**
     * Converts a list of patches into vector representations with position information.
     *
     * @param patches The list of patches to be converted.
     * @return A list of vectors with position metadata.
     */
    public static List<VectorWithPosition> VectorPatchs(List<Patch> patches) { ... }

    /**
     * Reconstructs a full image from a list of overlapping zones by averaging overlapping pixel values.
     *
     * @param zones The list of image zones with their positions.
     * @param width The width of the output image.
     * @param height The height of the output image.
     * @return The reconstituted full image.
     */
    public static BufferedImage recomposeFromZones(List<ImageZone> zones, int width, int height) { ... }

}
