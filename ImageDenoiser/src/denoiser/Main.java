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

            // 2. Ajout de bruit
            double sigma = 25.0;
            BufferedImage noisy = ImageUtils.noising(original, sigma);
            saveImage(noisy, "ImageDenoiser/images_bruitees/lena_noisy1.jpeg");

            // 3. Extraction des patchs
            int s = 8;
            List<Patch> patches = ImageUtils.extractPatches(noisy, s);

            // 4. Conversion en vecteurs
            List<double[]> vectors = patches.stream()
                                            .map(Patch::toVector)
                                            .toList();

            // 5. ACP
            ACPResult acpResult = ACP.computeACP(vectors);

            // 6. Projection
            List<double[]> Vc = ACP.MoyCov(vectors).Vc;
            double[][] contributions = ACP.project(acpResult.base, Vc);

            // 7. Calcul du lambda (choisir entre VisuShrink ou BayesShrink)
            // (A) Pour VisuShrink :
            //double lambda = Thresholding.seuilVisu(sigma, s * s);

            // (B) Pour BayesShrink :
            double sigmaSignal = Thresholding.estimateGlobalSigmaSignal(contributions, sigma);
            double lambda = Thresholding.seuilBayes(sigma, sigmaSignal);

            // 8. Seuillage des contributions (true = doux, false = dur)
            double[][] contributionsSeuillees = Thresholding.appliquerSeuillage(contributions, lambda, false);

            // 9. Reconstruction des vecteurs
            List<double[]> reconstructions = Thresholding.reconstructionsDepuisContributions(
                contributionsSeuillees,
                acpResult.base,
                acpResult.moyenne
            );

            // 10. Reconstruction de l'image depuis les patchs
            List<Patch> reconstructedPatches = new java.util.ArrayList<>();
            for (int i = 0; i < reconstructions.size(); i++) {
                Patch originalPatch = patches.get(i);
                double[] reconstructedData = reconstructions.get(i);
                Patch reconstructedPatch = new Patch(reconstructedData, originalPatch.positionX, originalPatch.positionY);
                reconstructedPatches.add(reconstructedPatch);
            }

            BufferedImage denoised = ImageUtils.reconstructPatches(reconstructedPatches, noisy.getHeight(), noisy.getWidth());

            // 11. Sauvegarde
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
