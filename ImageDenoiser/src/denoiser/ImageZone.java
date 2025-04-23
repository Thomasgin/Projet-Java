package denoiser;

import java.awt.image.BufferedImage;

public class ImageZone {
    private BufferedImage image;
    private int x;
    private int y;

    public ImageZone(BufferedImage image, int x, int y) {
        this.image = image;
        this.x = x;
        this.y = y;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int[] getPosition() {
        return new int[] { x, y };
    }
}
