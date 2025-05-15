package denoiser;

/**
 * Use to create instance of patch which are extract from an original image
 */
public class Patch {
    /**
     * A vector which represents patch, pixel by pixel in a grey scale
     */
    public double[] data;
    /**
     * abscissa coordinate of the patch
     */
    public int positionX;
    /**
     * ordinate coordinate of the patch
     */
    public int positionY;

    /**
     * Constructor of patchs
     * @param data aka a vector which contains value of pixel from patch
     * @param x as it position in the original image
     * @param y as it position in the original image
     */
    public Patch(double[] data, int x, int y) {
        this.data = data;
        this.positionX = x;
        this.positionY = y;
    }

    /**
     * getter of attribut data
     * @return data's value
     */
    public double[] toVector() {
        return data;
    }

    /**
     * Take a vector, and coordinate in parameters and instancing a new patch
     * @param v a vector
     * @param x aka coordinate on abscissa
     * @param y aka coordinate on ordinate
     * @return a new patch initialise with value in parameters
     */
    public static Patch fromVector(double[] v, int x, int y) {
        return new Patch(v, x, y);
    }
}
