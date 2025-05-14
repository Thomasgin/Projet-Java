

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.imageio.ImageIO;

public class Main {
	
	public static double mse;
	public static double psnr;
	public static double amelioration;

    public static void bruitage(String path, int sigma) throws Exception {
    	// Charger l’image propre (grayscale)
        BufferedImage original = loadImage(path);

        // Appliquer le bruit gaussien
        BufferedImage noisy = ImageUtils.noising(original, sigma);

        // Sauvegarder l’image bruitée
        saveImage(noisy, "images/bruitees/image_noisy_sigma" + sigma + ".jpeg");

        System.out.println("Image bruitée sauvegardée.");
    }
    
    public static void debruitage(String pathOriginal, String pathNoisy, int sigma, int patchs, String extractionType, String seuillageMethod, String seuilType) throws Exception {
    	BufferedImage original = ImageIO.read(new File(pathOriginal));
    	BufferedImage noisy = ImageIO.read(new File(pathNoisy));
    	
    	// 3. Extraction des patchs
    	System.out.println(patchs);
    	System.out.println(pathNoisy);
        List<Patch> patches = ImageUtils.extractPatches(noisy, patchs);

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
       // File dir = new File(sigmaDir);
       // if (!dir.exists()) dir.mkdirs();

        // 8. Écriture du fichier résultats
        FileWriter fw = new FileWriter("images/results/resultats.txt");
        PrintWriter pw = new PrintWriter(fw);

        /*String[] noms = { "DouxBayes", "DurBayes", "DouxVisu", "DurVisu" };
        boolean[] softFlags = { true, false, true, false };
        boolean[] bayesFlags = { true, true, false, false };

        for (int i = 0; i < 4; i++) {
           	String nom = noms[i];
            boolean isSoft = softFlags[i];
            boolean isBayes = bayesFlags[i];*/

        // Calcul lambda
        double lambda;
        if (seuilType == "Bayésien") {
        	double sigmaSignal = Thresholding.estimateGlobalSigmaSignal(contributions, sigma);
            lambda = Thresholding.seuilBayes(sigma, sigmaSignal);
        } else {
            lambda = Thresholding.seuilVisu(sigma, patchs * patchs);
        }

        // Seuillage
        double[][] contributionsSeuillees;
        if (seuillageMethod == "Seuillage doux") {
        	contributionsSeuillees = Thresholding.appliquerSeuillage(contributions, lambda, true);
        } else {
        	contributionsSeuillees = Thresholding.appliquerSeuillage(contributions, lambda, false);
        }

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

        String outPath =  "images/results/image_denoised_sigma" + sigma + "_patchs" + patchs + ".jpeg";
        saveImage(result, outPath);

        mse = Evaluation.mse(original, result);
        psnr = Evaluation.psnr(mse);
        amelioration = 80;
        System.out.printf("\nMSE = %.2f", mse);
        System.out.printf("\nPSNR = %.2f", psnr);
   
        pw.printf("MSE = %.2f, PSNR = %.2f dB%n", mse, psnr);

        pw.close();
        System.out.println("Traitement terminé pour sigma = " + sigma + " et patchs = " + patchs + "x" + patchs);
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
        ImageIO.write(img, "jpeg", output);
    }
}