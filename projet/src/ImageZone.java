import java.awt.image.BufferedImage;

/**
 * Represents a square region (zone) of an image, typically used for local denoising.
 * Stores the sub-image and its top-left coordinates within the original image.
 */
public class ImageZone {
    private BufferedImage image;
    private int x;
    private int y;

    /**
     * Constructs an ImageZone with the given sub-image and its top-left position.
     *
     * @param image The image patch representing the zone.
     * @param x The x-coordinate of the top-left corner in the original image.
     * @param y The y-coordinate of the top-left corner in the original image.
     */
    public ImageZone(BufferedImage image, int x, int y) {
        this.image = image;
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the image patch of this zone.
     *
     * @return The BufferedImage representing the zone.
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Returns the top-left coordinates of this zone in the original image.
     *
     * @return An array of two integers: [x, y] position.
     */
    public int[] getPosition() {
        return new int[] { x, y };
    }
}
