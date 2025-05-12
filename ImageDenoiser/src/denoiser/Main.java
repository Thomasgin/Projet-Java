package denoiser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
       try {
            // 1. Chargement de l'image originale
            BufferedImage original = loadImage("ImageDenoiser/images_sources/lena.jpeg");

            // 2. Ajout de bruit gaussien
            double sigma = 25.0;
            BufferedImage noisy = ImageUtils.noising(original, sigma);
            saveImage(noisy, "ImageDenoiser/images_bruitees/lena_noisy1.jpeg");

            // 3. Extraction des patchs
            int s = 8; // Taille du patch (s x s)
            List<Patch> patches = ImageUtils.extractPatches(noisy, s);

            // 4. Conversion des patchs en vecteurs
            List<double[]> vectors = patches.stream()
                                            .map(p -> p.toVector())
                                            .toList();

            // 5. Application de l'ACP
            ACPResult acpResult = ACP.computeACP(vectors);

            // 6. Projection des vecteurs centrés
            List<double[]> Vc = ACP.MoyCov(vectors).Vc;
            double[][] contributions = ACP.project(acpResult.base, Vc);

            // 7. Seuillage des contributions
            double lambda = Thresholding.seuilBayes(sigma, s * s);
            double[][] contributionsSeuillees = Thresholding.seuillageDouxBayes(contributions, lambda);

            // 8. Reconstruction des vecteurs depuis les contributions seuillées
            List<double[]> reconstructions = Thresholding.reconstructionsDepuisContributions(
                contributionsSeuillees,
                acpResult.base,
                acpResult.moyenne
            );

            // 9. Reconstruction de l'image depuis les patchs reconstruits
            List<Patch> reconstructedPatches = new java.util.ArrayList<>();
            for (int i = 0; i < reconstructions.size(); i++) {
                Patch originalPatch = patches.get(i);
                double[] reconstructedData = reconstructions.get(i);
                Patch reconstructedPatch = new Patch(reconstructedData, originalPatch.positionX, originalPatch.positionY);
                reconstructedPatches.add(reconstructedPatch);
            }

            BufferedImage denoised = ImageUtils.reconstructPatches(reconstructedPatches, noisy.getHeight(), noisy.getWidth());

            // 10. Sauvegarde des images
            saveImage(denoised, "ImageDenoiser/images_reconstruites/denoised.jpeg");

            System.out.println("Traitement terminé avec succès.");

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
