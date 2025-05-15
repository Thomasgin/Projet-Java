/**
 * Use to store vector with their coordinate in an original image
 */
public class VectorWithPosition {
    /**
     * Value of the vector stroe in a double tabular
     */
    public double[] vector;
    /**
     * Abscissa coordinate of the vector
     */
    public int x;
    /**
     * Ordinate coordinate of the vector
     */
    public int y;

    /**
     * Constructor of VectorWithPosition
     * @param vector a vector
     * @param x is it coordinate on abscissa
     * @param y is it coordinate on ordinates
     */
    public VectorWithPosition(double[] vector, int x, int y) {
        this.vector = vector;
        this.x = x;
        this.y = y;
    }
}
