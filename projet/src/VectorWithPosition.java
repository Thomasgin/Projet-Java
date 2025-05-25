/**
 * Represents a vector associated with its position coordinates,
 * typically used for image patches or features with spatial location.
 */
public class VectorWithPosition {

    /**
     * The vector data, such as pixel values or feature coefficients.
     */
    public double[] vector;

    /**
     * The horizontal position associated with the vector.
     */
    public int x;

    /**
     * The vertical position associated with the vector.
     */
    public int y;

    /**
     * Constructs a vector with its spatial position.
     *
     * @param vector the vector data
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     */
    public VectorWithPosition(double[] vector, int x, int y) {
        this.vector = vector;
        this.x = x;
        this.y = y;
    }
}
