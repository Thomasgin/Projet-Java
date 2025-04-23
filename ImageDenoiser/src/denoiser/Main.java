package denoiser;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        try {
            // Charger l’image propre (grayscale)
            BufferedImage image = loadImage("images_sources/lena.jpeg");

            // Appliquer le bruit gaussien
            double sigma = 10;
            for (int i = 0; i<4; i++) {
            	BufferedImage noisy = ImageUtils.noising(image, sigma);

                // Sauvegarder l’image bruitée
                saveImage(noisy, "images_bruitees/lena_bruitee_" + sigma + ".jpeg");

                System.out.println("Image bruitée sauvegardée.");
                
                // augmenter la valeur du bruit
                sigma += 10;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage loadImage(String path) throws Exception {
        BufferedImage original = ImageIO.read(new File(path));

        // Convertir en niveaux de gris si ce n'est pas déjà le cas
        BufferedImage gray = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        gray.getGraphics().drawImage(original, 0, 0, null);
        return gray;
    }

    public static void saveImage(BufferedImage img, String path) throws Exception {
        File output = new File(path);
        ImageIO.write(img, "jpeg", output);
    }
}
