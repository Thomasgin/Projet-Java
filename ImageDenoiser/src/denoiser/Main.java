package denoiser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Chargement de l'image originale
            BufferedImage original = loadImage("ImageDenoiser/images_sources/lena.jpeg");

            // 2. Ajout de bruit
            double sigma = 35.0;
            BufferedImage noisy = ImageUtils.noising(original, sigma);
            saveImage(noisy, "ImageDenoiser/images_bruitees/lena_noisy_sigma" + (int) sigma + ".jpeg");

            localDenoising(8, sigma, noisy, original);
           

        } catch (Exception e) {
            System.err.println("Erreur lors du traitement : " + e.getMessage());
            e.printStackTrace();
        }
    }


    
    public static void globalDenoising(int s, double sigma, BufferedImage noisy, BufferedImage original) throws IOException {
        // 3. Extraction des patchs
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

            String outPath = sigmaDir + "global_" + nom + ".jpeg";
            saveImage(result, outPath);

            double mse = Evaluation.mse(original, result);
            double psnr = Evaluation.psnr(mse);

            pw.printf("%s : MSE = %.2f, PSNR = %.2f dB%n", nom, mse, psnr);
        }

        pw.close();
        System.out.println("Traitement global terminé pour sigma = " + sigma + ".");
    }
    
    public static void localDenoising(int s, double sigma, BufferedImage noisy, BufferedImage original) throws IOException {
        int W = 64; // Taille des zones locales
        int pas = 64; // Pas de déplacement : non chevauchantes
        List<ImageZone> zones = ImageUtils.decoupeImage(noisy, W, pas);
        List<ImageZone> denoisedZonesBayesSoft = new ArrayList<>();
        List<ImageZone> denoisedZonesVisuSoft = new ArrayList<>();
        List<ImageZone> denoisedZonesBayesHard = new ArrayList<>();
        List<ImageZone> denoisedZonesVisuHard = new ArrayList<>();

        for (ImageZone zone : zones) {
            BufferedImage subImage = zone.getImage();
            int offsetX = zone.getPosition()[0];
            int offsetY = zone.getPosition()[1];

         // 3. Extraction des patchs
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
                BufferedImage zoneDenoised = ImageUtils.reconstructPatches(reconstructedPatches, subImage.getHeight(), subImage.getWidth());
                // Ajout de la zone débruitée dans la liste
                switch(i) {
                case 0:
                	denoisedZonesBayesSoft.add(new ImageZone(zoneDenoised, offsetX, offsetY));
                }
	            case 1:
	            	denoisedZonesBayesHard.add(new ImageZone(zoneDenoised, offsetX, offsetY));
	            }
		        case 2:
		        	denoisedZonesVisuSoft.add(new ImageZone(zoneDenoised, offsetX, offsetY));
		        }
			    case 3:
			    	denoisedZonesVisuHard.add(new ImageZone(zoneDenoised, offsetX, offsetY));
			    }
                }
                
            }

        // Recomposition finale
        BufferedImage result = ImageUtils.recomposeFromZones(denoisedZones, noisy.getWidth(), noisy.getHeight());

        // Sauvegarde de l'image débruitée finale
        String outputPath = "ImageDenoiser/images_reconstruites/sigma" + (int) sigma + "/Local" + + ".jpeg";
        saveImage(result, outputPath);

        // Évaluation
        double mse = Evaluation.mse(original, result);
        double psnr = Evaluation.psnr(mse);

        // Enregistrement des résultats
        try (PrintWriter pw = new PrintWriter(new FileWriter("ImageDenoiser/images_reconstruites/sigma" + (int) sigma + "/resultats_local.txt"))) {
            pw.printf("Local_DouxBayes : MSE = %.2f, PSNR = %.2f dB%n", mse, psnr);
        }

        System.out.println("Traitement local terminé pour sigma = " + sigma + ".");
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

    public static void saveImage(BufferedImage image, String path) {
        try {
            File outputFile = new File(path);
            outputFile.getParentFile().mkdirs(); // Crée les dossiers manquants
            ImageIO.write(image, "jpeg", outputFile);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de l'image : " + path, e);
        }
    }

}
