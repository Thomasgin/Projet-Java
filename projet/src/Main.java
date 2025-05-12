

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main {

    public static void principal(String path, int sigma) throws Exception {
    	// Charger l’image propre (grayscale)
        BufferedImage image = loadImage(path);

        // Appliquer le bruit gaussien
        BufferedImage noisy = ImageUtils.noising(image, sigma);

        // Sauvegarder l’image bruitée
        saveImage(noisy, "images/bruitees/image_noisy_sigma" + sigma + ".png");

        System.out.println("Image bruitée sauvegardée.");
    }

    public static BufferedImage loadImage(String path) throws Exception {
    	System.out.println(path);
        BufferedImage original = ImageIO.read(new File(path));

        // Convertir en niveaux de gris si ce n'est pas déjà le cas
        BufferedImage gray = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        gray.getGraphics().drawImage(original, 0, 0, null);
        return gray;
    }

    public static void saveImage(BufferedImage img, String path) throws Exception {
        File output = new File(path);
        ImageIO.write(img, "png", output);
    }
}