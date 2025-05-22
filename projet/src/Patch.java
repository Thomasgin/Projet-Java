

public class Patch {
    public double[] data;
    public int positionX;
    public int positionY;

    public Patch(double[] data, int x, int y) {
        this.data = data;
        this.positionX = x;
        this.positionY = y;
    }

    public double[] toVector() {
        return data;
    }

    public static Patch fromVector(double[] v, int x, int y) {
        return new Patch(v, x, y);
    }
}
