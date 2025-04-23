package denoiser;

public class VectorWithPosition {
    public double[] vector;
    public int x;
    public int y;

    public VectorWithPosition(double[] vector, int x, int y) {
        this.vector = vector;
        this.x = x;
        this.y = y;
    }
}
