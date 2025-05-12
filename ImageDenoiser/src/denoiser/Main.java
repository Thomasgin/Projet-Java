package denoiser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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
            saveImage(noisy, "ImageDenoiser/images_bruitees/lena_noisy_sigma" + (int) sigma + ".jpeg");

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

            // 7. Dossier de sortie pour sigma
            String sigmaDir = "ImageDenoiser/images_reconstruites/sigma" + (int) sigma + "/";
            File dir = new File(sigmaDir);
            if (!dir.exists()) dir.mkdirs();

            // 8. Écriture du fichier résultats
            FileWriter fw = new FileWriter(sigmaDir + "resultats.txt");
            PrintWriter pw = new PrintWriter(fw);

            String[] noms = { "DouxBayes", "DurBayes", "DouxVisu", "DurVisu" };
            boolean[] softFlags = { true, false, true, false };
            boolean[] bayesFlags = { true, true, false, false };

            for (int i = 0; i < 4; i++) {
                String nom = noms[i];
                boolean isSoft = softFlags[i];
                boolean isBayes = bayesFlags[i];

                // Calcul lambda
                double lambda;
                if (isBayes) {
                    double sigmaSignal = Thresholding.estimateGlobalSigmaSignal(contributions, sigma);
                    lambda = Thresholding.seuilBayes(sigma, sigmaSignal);
                } else {
                    lambda = Thresholding.seuilVisu(sigma, s * s);
                }

                // Seuillage
                double[][] contributionsSeuillees = Thresholding.appliquerSeuillage(contributions, lambda, isSoft);

                // Reconstruction
                List<double[]> reconstructions = Thresholding.reconstructionsDepuisContributions(
                    contributionsSeuillees, acpResult.base, acpResult.moyenne);

                List<Patch> reconstructedPatches = new java.util.ArrayList<>();
                for (int j = 0; j < reconstructions.size(); j++) {
                    Patch originalPatch = patches.get(j);
                    double[] data = reconstructions.get(j);
                    reconstructedPatches.add(new Patch(data, originalPatch.positionX, originalPatch.positionY));
                }

                BufferedImage result = ImageUtils.reconstructPatches(
                    reconstructedPatches, original.getHeight(), original.getWidth());

                String outPath = sigmaDir + nom + ".jpeg";
                saveImage(result, outPath);

                double mse = Evaluation.mse(original, result);
                double psnr = Evaluation.psnr(mse);

                pw.printf("%s : MSE = %.2f, PSNR = %.2f dB%n", nom, mse, psnr);
            }

            pw.close();
            System.out.println("Traitement terminé pour sigma = " + sigma + ".");

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
