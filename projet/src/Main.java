/**
     * Mean squared error between original and denoised images.
     */
    public static double mse;

    /**
     * Peak signal-to-noise ratio computed from MSE.
     */
    public static double psnr;

    /**
     * Minimum mean squared error observed (for evaluation).
     */
    public static double minMse;

    /**
     * Maximum PSNR observed (for evaluation).
     */
    public static double maxPsnr;

    /**
     * Percentage improvement of denoising compared to noisy image.
     */
    public static double amelioration;

    /**
     * Maximum observed improvement (for evaluation).
     */
    public static double maxAmelioration;

    /**
     * Adds Gaussian noise to the image at the given path and saves the noisy image.
     *
     * @param path the file path to the original clean image (grayscale)
     * @param sigma the standard deviation of the Gaussian noise to add
     * @throws Exception if image loading or saving fails
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
     * Performs global PCA-based denoising on a noisy image given the original image,
     * noise level, patch size, thresholding method, and threshold type.
     * Saves the denoised image and writes the evaluation results to a file.
     *
     * @param pathOriginal the file path to the original clean image
     * @param pathNoisy the file path to the noisy image
     * @param sigma the noise standard deviation
     * @param patchs the size of patches (patch dimension)
     * @param seuillageMethod the thresholding method ("soft" or "hard")
     * @param seuilType the threshold type ("Bayésien" or "VisuShrink")
     * @throws Exception if image loading, processing or saving fails
     */    
    public static void debruitageGlobal(String pathOriginal, String pathNoisy, int sigma, int patchs, String seuillageMethod, String seuilType) throws Exception {
    	BufferedImage original = ImageIO.read(new File(pathOriginal));
    	BufferedImage noisy = ImageIO.read(new File(pathNoisy));
    	

        List<Patch> patches = ImageUtils.extractPatches(noisy, patchs);

        List<double[]> vectors = patches.stream()
                                        .map(Patch::toVector)
                                        .toList();

        ACPResult acpResult = ACP.computeACP(vectors);


        List<double[]> Vc = ACP.MoyCov(vectors).Vc;
        double[][] contributions = ACP.project(acpResult.base, Vc);

        FileWriter fw = new FileWriter("images/results/resultats.txt");
        PrintWriter pw = new PrintWriter(fw);

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

        
        amelioration = (mseBruitee - mse) / mseBruitee * 100;
        	
        System.out.printf("\nMSE = %.2f", mse);
        System.out.printf("\nPSNR = %.2f", psnr);
        System.out.printf("\nMSE = %.2f", mseBruitee);

   
        pw.printf("-----------Résultats du débruitage de l'image-----------\nSigma : " + sigma + "\nPatchs : " + patchs + "x" + patchs + "\nType d'extraction : Global\nMéthode de seuillage : " + seuillageMethod + "\nType de seuil : " + seuilType);
        pw.printf("\n\nMSE = %.2f, PSNR = %.2f dB%n", mse, psnr);

        pw.close();
        System.out.println("Traitement terminé pour sigma = " + sigma + " et patchs = " + patchs + "x" + patchs);
    }
    
    /**
     * Performs local PCA-based denoising on a noisy image by dividing the image
     * into overlapping zones, denoising each zone independently, then recomposing
     * the full image. Supports different thresholding methods and threshold types.
     *
     * @param pathOriginal the file path to the original clean image
     * @param pathNoisy the file path to the noisy image
     * @param sigma the noise standard deviation
     * @param patchs the size of patches (patch dimension)
     * @param seuillageMethod the thresholding method ("soft" or "hard")
     * @param seuilType the threshold type ("Bayésien" or "VisuShrink")
     * @throws Exception if image loading, processing, or saving fails
     */
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

    /**
     * Performs and compares local and global PCA-based denoising on a noisy image using
     * four different thresholding configurations:
     *   - Soft Bayesian
     *   - Hard Bayesian
     *   - Soft VisuShrink
     *   - Hard VisuShrink
     * 
     * The method divides the noisy image into overlapping zones for local denoising,
     * applies PCA, projects coefficients, thresholds, reconstructs patches, and recomposes
     * zones back into full images for each method. Then it also performs global denoising
     * on the entire image with the same four configurations.
     * 
     * It evaluates each denoised image by computing MSE, PSNR, and improvement relative
     * to the noisy image, saves results and images, and returns the best denoising result
     * based on maximum PSNR.
     * 
     * @param pathOriginal the file path to the original clean image
     * @param pathNoisy the file path to the noisy image
     * @param sigma the noise standard deviation
     * @param patchs the size of patches (patch dimension)
     * @return a list containing two elements:
     *         1. The file path of the best denoised image (highest PSNR)
     *         2. The method label (e.g. "Local_Doux_Bayésien" or "Global_Dur_Visu")
     * @throws Exception if image loading, processing, or saving fails
     */
    public static List<String> optimiserDebruitage(String pathOriginal, String pathNoisy, int sigma, int patchs) throws Exception {
    	BufferedImage original = ImageIO.read(new File(pathOriginal));
    	BufferedImage noisy = ImageIO.read(new File(pathNoisy));

    	int height = noisy.getHeight();
        int width = noisy.getWidth();

        int minDim = Math.min(width, height);

        int W = minDim / 2;
        int pas = W / 2;

        System.out.println("Taille image : " + width + "x" + height + ", W = " + W + ", pas = " + pas);

        List<ImageZone> zones = ImageUtils.decoupeImage(noisy, W, pas);

        List<List<ImageZone>> zonesParMethode = new ArrayList<>();
        for (int i = 0; i < 4; i++) zonesParMethode.add(new ArrayList<>());

        String[] noms = { "Doux_Bayésien", "Dur_Bayésien", "Doux_Visu", "Dur_Visu" };
        boolean[] softFlags = { true, false, true, false };
        boolean[] bayesFlags = { true, true, false, false };

        for (ImageZone zone : zones) {
            BufferedImage subImage = zone.getImage();
            int offsetX = zone.getPosition()[0];
            int offsetY = zone.getPosition()[1];

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
    	
        List<Patch> patches = ImageUtils.extractPatches(noisy, patchs);

        List<double[]> vectors = patches.stream()
                                        .map(Patch::toVector)
                                        .toList();

        ACPResult acpResult = ACP.computeACP(vectors);

        List<double[]> Vc = ACP.MoyCov(vectors).Vc;
        double[][] contributions = ACP.project(acpResult.base, Vc);

        for (int i = 0; i < 4; i++) {
           	String nom = noms[i];
            boolean isSoft = softFlags[i];
            boolean isBayes = bayesFlags[i];

            double lambda;
            if (isBayes) {
            	double sigmaSignal = Thresholding.estimateGlobalSigmaSignal(contributions, sigma);
            	lambda = Thresholding.seuilBayes(sigma, sigmaSignal);
            } else {
            	lambda = Thresholding.seuilVisu(sigma, patchs * patchs);
            }

            double[][] contributionsSeuillees;
            contributionsSeuillees = Thresholding.appliquerSeuillage(contributions, lambda, isSoft);

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

    /**
     * Loads an image from the given file path and converts it to grayscale.
     *
     * @param path the file path of the image to load
     * @return a grayscale BufferedImage
     * @throws Exception if the image cannot be read or the file does not exist
     */
    public static BufferedImage loadImage(String path) throws Exception {
    	System.out.println(path);
        BufferedImage original = ImageIO.read(new File(path));

        // Convertir en niveaux de gris si ce n'est pas déjà le cas
        BufferedImage gray = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        gray.getGraphics().drawImage(original, 0, 0, null);
        return gray;
    }
    /**
     * Saves a BufferedImage to the specified file path in JPEG format.
     *
     * @param img the BufferedImage to save
     * @param path the destination file path where the image will be saved
     * @throws Exception if an error occurs during writing the file
     */
    public static void saveImage(BufferedImage img, String path) throws Exception {
        File output = new File(path);
        ImageIO.write(img, "jpeg", output);
    }
}