package denoiser;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            BufferedImage image = loadImage("ImageDenoiser/images_bruitees/lena_noisy_sigma25.jpeg");

            List<Patch> patches = ImageUtils.extractPatches(image, 7);
            List<VectorWithPosition> vectorWithPositions = ImageUtils.VectorPatchs(patches);

            System.out.println("Nombre de vecteurs : " + vectorWithPositions.size());
            VectorWithPosition first = vectorWithPositions.get(0);
            System.out.println("Premier vecteur : " + java.util.Arrays.toString(first.vector));
            System.out.println("Position : (" + first.x + ", " + first.y + ")");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static BufferedImage loadImage(String path) {
        try {
            BufferedImage original = ImageIO.read(new File(path));
            BufferedImage gray = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            gray.getGraphics().drawImage(original, 0, 0, null);
            return gray;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement de l'image : " + path, e);
        }
    }

    public static void saveImage(BufferedImage img, String path) {
        try {
            File output = new File(path);
            ImageIO.write(img, "jpeg", output);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de l'image : " + path, e);
        }
    }
}
