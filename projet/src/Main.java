

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class Main {
	
	public static double mse;
	public static double psnr;
	public static double minMse;
	public static double maxPsnr;
	public static double amelioration;
	public static double maxAmelioration;

    public static void bruitage(String path, int sigma) throws Exception {
    	// Charger l’image propre (grayscale)
        BufferedImage original = loadImage(path);

        // Appliquer le bruit gaussien
        BufferedImage noisy = ImageUtils.noising(original, sigma);

        // Sauvegarder l’image bruitée
        saveImage(noisy, "images/bruitees/image_noisy_sigma" + sigma + ".jpeg");

        System.out.println("Image bruitée sauvegardée.");
    }
    
    public static void debruitageGlobal(String pathOriginal, String pathNoisy, int sigma, int patchs, String seuillageMethod, String seuilType) throws Exception {
    	BufferedImage original = ImageIO.read(new File(pathOriginal));
    	BufferedImage noisy = ImageIO.read(new File(pathNoisy));
    	
    	// 3. Extraction des patchs
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
        
        double mseBruitee = Evaluation.mse(original, noisy);
        //double psnrBruitee = Evaluation.psnr(mseBruitee);
        
        amelioration = (mseBruitee - mse) / mseBruitee * 100;
        	
        System.out.printf("\nMSE = %.2f", mse);
        System.out.printf("\nPSNR = %.2f", psnr);
        System.out.printf("\nMSE = %.2f", mseBruitee);
       // System.out.printf("\nPSNR = %.2f", psnrBruitee);
   
        pw.printf("-----------Résultats du débruitage de l'image-----------\nSigma : " + sigma + "\nPatchs : " + patchs + "x" + patchs + "\nType d'extraction : Global\nMéthode de seuillage : " + seuillageMethod + "\nType de seuil : " + seuilType);
        pw.printf("\n\nMSE = %.2f, PSNR = %.2f dB%n", mse, psnr);

        pw.close();
        System.out.println("Traitement terminé pour sigma = " + sigma + " et patchs = " + patchs + "x" + patchs);
    }
    
    public static void debruitageLocal(String pathOriginal, String pathNoisy, int sigma, int patchs, String seuillageMethod, String seuilType) throws Exception {
    	BufferedImage original = ImageIO.read(new File(pathOriginal));
    	BufferedImage noisy = ImageIO.read(new File(pathNoisy));
    	
    	int height = noisy.getHeight();
        int width = noisy.getWidth();

        int minDim = Math.min(width, height);

        // Taille des sous-images = moitié de la plus petite dimension
        int W = minDim / 2;
        // Pas = W / 2 = 1/4 de la dimension
        int pas = W / 2;

        System.out.println("Taille image : " + width + "x" + height + ", W = " + W + ", pas = " + pas);

        List<ImageZone> zones = ImageUtils.decoupeImage(noisy, W, pas);
        
        List<ImageZone> reconstructedZones = new ArrayList<ImageZone>();

        for (ImageZone zone : zones) {
            BufferedImage subImage = zone.getImage();
            int offsetX = zone.getPosition()[0];
            int offsetY = zone.getPosition()[1];

            // Pipeline
            List<Patch> patches = ImageUtils.extractPatches(subImage, patchs);
            List<double[]> vectors = patches.stream().map(Patch::toVector).toList();
            ACPResult acpResult = ACP.computeACP(vectors);
            List<double[]> Vc = ACP.MoyCov(vectors).Vc;
            double[][] contributions = ACP.project(acpResult.base, Vc);

            double lambda;
            if (seuilType == "Bayésien") {
            	double sigmaSignal = Thresholding.estimateGlobalSigmaSignal(contributions, sigma);
            	lambda = Thresholding.seuilBayes(sigma, sigmaSignal);
            } else {
            	lambda = Thresholding.seuilVisu(sigma, patchs * patchs);
            }

            double[][] contributionsSeuillees;
            if (seuillageMethod == "Seuillage doux") {
            	contributionsSeuillees = Thresholding.appliquerSeuillage(contributions, lambda, true);
            } else {
            	contributionsSeuillees = Thresholding.appliquerSeuillage(contributions, lambda, false);
            }

            List<double[]> reconstructions = Thresholding.reconstructionsDepuisContributions(
            		contributionsSeuillees, acpResult.base, acpResult.moyenne);

            List<Patch> reconstructedPatches = new ArrayList<>();
            for (int j = 0; j < reconstructions.size(); j++) {
            	Patch originalPatch = patches.get(j);
                reconstructedPatches.add(new Patch(reconstructions.get(j), originalPatch.positionX, originalPatch.positionY));
            }

            BufferedImage zoneDenoised = ImageUtils.reconstructPatches(reconstructedPatches, subImage.getHeight(), subImage.getWidth());
            
            reconstructedZones.add(new ImageZone(zoneDenoised, offsetX, offsetY));

        }

        // Recomposition, sauvegarde et évaluation
        FileWriter fw = new FileWriter("images/results/resultats.txt");
        PrintWriter pw = new PrintWriter(fw);

        BufferedImage result = ImageUtils.recomposeFromZones(reconstructedZones, noisy.getWidth(), noisy.getHeight());

        String outPath =  "images/results/image_denoised_sigma" + sigma + "_patchs" + patchs + ".jpeg";
        saveImage(result, outPath);

        mse = Evaluation.mse(original, result);
        psnr = Evaluation.psnr(mse);
        double mseBruitee = Evaluation.mse(original, noisy);
        //double psnrBruitee = Evaluation.psnr(mseBruitee);
        amelioration = (mseBruitee - mse) / mseBruitee * 100;
        System.out.printf("\nMSE = %.2f", mse);
        System.out.printf("\nPSNR = %.2f", psnr);
   
        pw.printf("-----------Résultats du débruitage de l'image-----------\nSigma : " + sigma + "\nPatchs : " + patchs + "x" + patchs + "\nType d'extraction : Local\nMéthode de seuillage : " + seuillageMethod + "\nType de seuil : " + seuilType);
        pw.printf("\n\nMSE = %.2f, PSNR = %.2f dB%n", mse, psnr);

        pw.close();
        System.out.println("\nTraitement terminé pour sigma = " + sigma + " et patchs = " + patchs + "x" + patchs);
    }

    public static List<String> optimiserDebruitage(String pathOriginal, String pathNoisy, int sigma, int patchs) throws Exception {
    	BufferedImage original = ImageIO.read(new File(pathOriginal));
    	BufferedImage noisy = ImageIO.read(new File(pathNoisy));
    	
    	// Local
    	
    	int height = noisy.getHeight();
        int width = noisy.getWidth();

        int minDim = Math.min(width, height);

        // Taille des sous-images = moitié de la plus petite dimension
        int W = minDim / 2;
        // Pas = W / 2 = 1/4 de la dimension
        int pas = W / 2;

        System.out.println("Taille image : " + width + "x" + height + ", W = " + W + ", pas = " + pas);

        List<ImageZone> zones = ImageUtils.decoupeImage(noisy, W, pas);

        // Préparation des listes pour les 4 méthodes
        List<List<ImageZone>> zonesParMethode = new ArrayList<>();
        for (int i = 0; i < 4; i++) zonesParMethode.add(new ArrayList<>());

        String[] noms = { "Doux_Bayésien", "Dur_Bayésien", "Doux_Visu", "Dur_Visu" };
        boolean[] softFlags = { true, false, true, false };
        boolean[] bayesFlags = { true, true, false, false };

        for (ImageZone zone : zones) {
            BufferedImage subImage = zone.getImage();
            int offsetX = zone.getPosition()[0];
            int offsetY = zone.getPosition()[1];

            // Pipeline
            List<Patch> patches = ImageUtils.extractPatches(subImage, patchs);
            List<double[]> vectors = patches.stream().map(Patch::toVector).toList();
            ACPResult acpResult = ACP.computeACP(vectors);
            List<double[]> Vc = ACP.MoyCov(vectors).Vc;
            double[][] contributions = ACP.project(acpResult.base, Vc);

            for (int i = 0; i < 4; i++) {
                boolean isSoft = softFlags[i];
                boolean isBayes = bayesFlags[i];

                double lambda = isBayes
                    ? Thresholding.seuilBayes(sigma, Thresholding.estimateGlobalSigmaSignal(contributions, sigma))
                    : Thresholding.seuilVisu(sigma, patchs * patchs);

                double[][] contributionsSeuillees = Thresholding.appliquerSeuillage(contributions, lambda, isSoft);

                List<double[]> reconstructions = Thresholding.reconstructionsDepuisContributions(
                    contributionsSeuillees, acpResult.base, acpResult.moyenne);

                List<Patch> reconstructedPatches = new ArrayList<>();
                for (int j = 0; j < reconstructions.size(); j++) {
                    Patch originalPatch = patches.get(j);
                    reconstructedPatches.add(new Patch(reconstructions.get(j), originalPatch.positionX, originalPatch.positionY));
                }

                BufferedImage zoneDenoised = ImageUtils.reconstructPatches(reconstructedPatches, subImage.getHeight(), subImage.getWidth());

                zonesParMethode.get(i).add(new ImageZone(zoneDenoised, offsetX, offsetY));
            }
        }

        // Recomposition, sauvegarde et évaluation

        FileWriter fw = new FileWriter("images/results/resultats.txt");
        PrintWriter pw = new PrintWriter(fw);
        
        pw.printf("-----------Résultats du débruitage de l'image-----------\nSigma : " + sigma + "\nPatchs : " + patchs + "x" + patchs + "\n\n");
        
        Map<Double, List<String>> mapImages = new HashMap<>();

        for (int i = 0; i < 4; i++) {
            BufferedImage result = ImageUtils.recomposeFromZones(zonesParMethode.get(i), noisy.getWidth(), noisy.getHeight());

            String outPath = "images/results/image_denoised_sigma" + sigma + "_patchs" + patchs + "_Local_" + noms[i] + ".jpeg";
            saveImage(result, outPath);

            mse = Evaluation.mse(original, result);
            psnr = Evaluation.psnr(mse);
            double mseBruitee = Evaluation.mse(original, noisy);
            //double psnrBruitee = Evaluation.psnr(mseBruitee);
            amelioration = (mseBruitee - mse) / mseBruitee * 100;
            
            if (maxPsnr < psnr) {
            	maxPsnr = psnr;
            	minMse = mse;
            	maxAmelioration = amelioration;
            }
            
   
            pw.printf("Local_" + noms[i] + " : MSE = %.2f, PSNR = %.2f dB%n", mse, psnr);
            
            List<String> liste = new ArrayList<>();
            liste.add(outPath);
            liste.add("Local_" + noms[i]);
            
            mapImages.put(psnr, liste); 
        }
    	
    	// Global
    	
    	
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
            	lambda = Thresholding.seuilVisu(sigma, patchs * patchs);
            }

            // Seuillage
            double[][] contributionsSeuillees;
            contributionsSeuillees = Thresholding.appliquerSeuillage(contributions, lambda, isSoft);

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

            String outPath =  "images/results/image_denoised_sigma" + sigma + "_patchs" + patchs + "_Global_" + nom + ".jpeg";
            saveImage(result, outPath);

            mse = Evaluation.mse(original, result);
            psnr = Evaluation.psnr(mse);
            double mseBruitee = Evaluation.mse(original, noisy);
            //double psnrBruitee = Evaluation.psnr(mseBruitee);
            amelioration = (mseBruitee - mse) / mseBruitee * 100;
            if (maxPsnr < psnr) {
            	maxPsnr = psnr;
            	minMse = mse;
            	maxAmelioration = amelioration;
            }
            amelioration = 80;
   
            pw.printf("Global_" + nom + " : MSE = %.2f, PSNR = %.2f dB%n", mse, psnr);
            
            List<String> liste = new ArrayList<>();
            liste.add(outPath);
            liste.add("Global_" + nom);
            
            mapImages.put(psnr, liste);        
        }

        pw.close();
        System.out.println("\nTraitement terminé pour sigma = " + sigma + " et patchs = " + patchs + "x" + patchs);
        
        return mapImages.get(maxPsnr);     
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