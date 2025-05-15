import java.awt.image.BufferedImage;

/**
 * Use to store sub-image extract form original image in order to use them for local ACP
 */
public class ImageZone {
    /**
     * Sub-image extract from an original image
     */
    private BufferedImage image;
    /**
     * It abscissa coordinate in original image
     */
    private int x;
    /**
     * It ordinate coordinate in original image
     */
    private int y;

    /**
     * Constructor of ImageZone
     * @param image is a BufferdImage
     * @param x as it coordinate on abscissa
     * @param y as it coordinate on ordinate
     */
    public ImageZone(BufferedImage image, int x, int y) {
        this.image = image;
        this.x = x;
        this.y = y;
    }

    /**
     * getter for the image attribut
     * @return image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * getter for the position's attributs
     * @return x and y
     */
    public int[] getPosition() {
        return new int[] { x, y };
    }
}
