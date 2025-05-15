

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.imageio.ImageIO;

public class Main {
    /**
     * Default constructor
     */
    public Main(){}

	/**
     * Static attribut which contains mse's value
     */
	public static double mse;
    /**
     * Static attribut which contains psnr's value
     */
	public static double psnr;
    /**
     * Static attribut which contains value of enhancement
     */
	public static double amelioration;

    /**
     * Load image and apply noise on it
     * @param path is the path of source folder 
     * @param sigma is a constant which defines the noise
     */
    public static void bruitage(String path, int sigma) throws Exception {
    	// Charger l’image propre (grayscale)
        BufferedImage original = loadImage(path);

        // Appliquer le bruit gaussien
        BufferedImage noisy = ImageUtils.noising(original, sigma);

        // Sauvegarder l’image bruitée
        saveImage(noisy, "images/bruitees/image_noisy_sigma" + sigma + ".jpeg");

        System.out.println("Image bruitée sauvegardée.");
    }
    
    /**
     * Apply denoising on the image loaded previously with the method which is define by methode's arguments
     * @param pathOriginal is the path of the original image
     * @param pathNoisy is the path of the image with noise
     * @param sigma is a constant which defines the noise
     * @param patchs is the patch's width
     * @param extractionType is the type of patchs' extraction 
     * @param seuillageMethod is the type of thresholding
     * @param seuilType is the type of threshold
     */
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

    /**
     * load an image from the path
     * @param path aka the locating of destination file
     * @return return the image as a BufferedImage
     */
    public static BufferedImage loadImage(String path) throws Exception {
    	System.out.println(path);
        BufferedImage original = ImageIO.read(new File(path));

        // Convert in a greyscale if not
        BufferedImage gray = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        gray.getGraphics().drawImage(original, 0, 0, null);
        return gray;
    }

    /**
     * Save the image in the selected file
     * @param img a BufferedImage
     * @param path aka the locating of destination file
     */
    public static void saveImage(BufferedImage img, String path) throws Exception {
        File output = new File(path);
        ImageIO.write(img, "jpeg", output);
    }
}