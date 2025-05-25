/**
 * Represents a patch of the image with its data and position coordinates.
 */
public class Patch {

    /**
     * The pixel data of the patch represented as a vector.
     */
    public double[] data;

    /**
     * The horizontal position of the patch in the image.
     */
    public int positionX;

    /**
     * The vertical position of the patch in the image.
     */
    public int positionY;

    /**
     * Constructs a patch with its data and position.
     *
     * @param data the vector representing patch pixel values
     * @param x the horizontal position of the patch
     * @param y the vertical position of the patch
     */
    public Patch(double[] data, int x, int y) {
        this.data = data;
        this.positionX = x;
        this.positionY = y;
    }

    /**
     * Returns the data of the patch as a vector.
     *
     * @return the vector representing the patch data
     */
    public double[] toVector() {
        return data;
    }

    /**
     * Creates a patch from a vector and its position coordinates.
     *
     * @param v the vector representing patch pixel values
     * @param x the horizontal position of the patch
     * @param y the vertical position of the patch
     * @return a new Patch instance corresponding to the vector and position
     */
    public static Patch fromVector(double[] v, int x, int y) {
        return new Patch(v, x, y);
    }
}
