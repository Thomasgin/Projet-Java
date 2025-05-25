import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
/**
 * Main JavaFX GUI class for an image denoising application using
 * Principal Component Analysis (PCA).
 * 
 * <p>
 * This class manages the creation and display of the main window containing:
 * <ul>
 *   <li>A parameters panel to adjust options for adding noise, denoising, and optimization.</li>
 *   <li>A display area showing the original, noisy, and denoised images.</li>
 *   <li>Buttons to apply noise, apply denoising, optimize results, export, reset, and save outputs.</li>
 * </ul>
 * 
 * <p>
 * The typical workflow is to load an original image, add artificial noise,
 * apply denoising with various parameters (extraction type, thresholding method, etc.),
 * then visualize and compare the results.
 * 
 * <p>
 * This class relies on the Main class (not included here) to perform image processing
 * tasks such as noise addition and denoising.
 * 
 * @see javafx.application.Application
 */
public class Maquette extends Application {
/** Path to the loaded original image */
    private String pathOriginal;
    
    /** Path to the generated noisy image */
    private String pathNoisy;
    
    /** Path to the generated denoised image */
    private String pathDenoised;
    
    /** ImageView displaying the noisy image */
    private ImageView imageBruitee;
    
    /** ImageView displaying the denoised image */
    private ImageView imageDebruitee;
    
    /** Label placeholder shown when no noisy image is loaded */
    private Label placeholderLabelBruitee;
    
    /** Label placeholder shown when no denoised image is loaded */
    private Label placeholderLabelDebruitee;
    
    /** Size of image patches used for PCA */
    private int patchs;
    
    /** Noise level (standard deviation sigma) */
    private int sigma;
    
    /** Label for the noisy image description */
    private Label bruiteeLabel;
    
    /** Label for the denoised image description */
    private Label debruiteeLabel;
    
    /** Container displaying quality metrics like MSE, PSNR, and improvement */
    private HBox metricsBox;

    /**
     * Entry point of the JavaFX application.
     * 
     * <p>
     * Sets up the main window with controls for noise addition, denoising,
     * optimization, and image displays. Connects UI elements to the image processing
     * logic in the Main class.
     * 
     * @param primaryStage the main stage provided by JavaFX runtime
     */
    @Override
    public void start(Stage primaryStage) {
	    // Main container with CSS styles
	    BorderPane mainContainer = new BorderPane();
	    mainContainer.setPrefSize(900, 650);
	    mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

	    // Application header
	    Label header = new Label("Débruitage d'images par ACP");
	    header.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 18px 25px;");
	    HBox headerBox = new HBox(header);
	    mainContainer.setTop(headerBox);

	    // Parameters panel on the left
	    VBox paramsPanel = new VBox(20);
	    paramsPanel.setPadding(new Insets(20));
	    paramsPanel.setPrefWidth(280);
	    paramsPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 1px 0 0;");

	    // Extraction type choice (Global or Local PCA)
	    ComboBox<String> extractionType = new ComboBox<>();
	    extractionType.getItems().addAll("Global PCA", "Local PCA");
	    extractionType.setValue("Global PCA");

	    // Slider for patch size
	    Slider patchSizeSlider = new Slider(5, 30, 15);
	    patchs = 15;
	    Label patchSizeLabel = new Label("15x15 pixels");

	    // Slider for noise (sigma)
	    Slider noiseSlider = new Slider(0, 50, 20);
	    sigma = 20;
	    Label noiseLabel = new Label("σ = " + sigma);

	    // Thresholding method choice
	    ComboBox<String> seuillageMethod = new ComboBox<>();
	    seuillageMethod.getItems().addAll("Seuillage dur", "Seuillage doux");
	    seuillageMethod.setValue("Seuillage dur");
	    
	    // Threshold type choice
	    ComboBox<String> seuilType = new ComboBox<>();
	    seuilType.getItems().addAll("Visu", "Bayésien");
	    seuilType.setValue("Visu");
	    
	    // Button to apply noise
	    Button appliquerBruitage = new Button("Appliquer le bruitage");
	    appliquerBruitage.setMaxWidth(Double.MAX_VALUE);
	    appliquerBruitage.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-font-weight: 500;");
	    appliquerBruitage.setOnAction(e -> {
	        try {
	            Main.bruitage(pathOriginal, sigma); // call of external method
	        } catch (Exception e1) {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
	        }
	        pathNoisy = "images/bruitees/image_noisy_sigma" + sigma + ".jpeg";
	        imageNoisy.setImage(new Image("file:" + pathNoisy));
	        imageNoisy.setVisible(true);
	        placeholderLabelNoisy.setVisible(false);
	    });
	    
	    // Button to apply denoising with parameters
	    Button appliquerDebruitage = new Button("Appliquer le débruitage");
	    appliquerDebruitage.setMaxWidth(Double.MAX_VALUE);
	    appliquerDebruitage.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-font-weight: 500;");
	    appliquerDebruitage.setOnAction(e -> {
	        metricsBox.getChildren().clear();
	        try {
	            if (extractionType.getValue() == "Global PCA") {
	                Main.debruitageGlobal(pathOriginal, pathNoisy, sigma, patchs, seuillageMethod.getValue(), seuilType.getValue());
	            } else {
	                Main.debruitageLocal(pathOriginal, pathNoisy, sigma, patchs, seuillageMethod.getValue(), seuilType.getValue());
	            }
	        } catch (Exception e2) {
	            // TODO Auto-generated catch block
	            e2.printStackTrace();
	        }
	        pathDenoised = "images/results/image_denoised_sigma" + sigma + "_patchs" + patchs +".jpeg";
	        imageDenoised.setImage(new Image("file:" + pathDenoised));
	        imageDenoised.setVisible(true);
	        placeholderLabelDebruitee.setVisible(false);
	        
	        labelDenoised.setText(String.format("Type d'extraction : " + extractionType.getValue() + "\nMéthode de Seuillage : " + seuillageMethod.getValue() + "\nType de Seuil : " + seuilType.getValue()));
	        
	        // Display of metrics
	        metricsBox.getChildren().addAll(
	                createMetricBox(Main.mse, "MSE"),
	                createMetricBox(Main.psnr, "PSNR (dB)"),
	                createMetricBox(Main.amelioration, "Amélioration")
	        );
	    });
	    
	    // Button to optimize denoising with best parameters
	    Button optimiserDebruitage = new Button("Optimiser le débruitage");
	    optimiserDebruitage.setMaxWidth(Double.MAX_VALUE);
	    optimiserDebruitage.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-font-weight: 500;");
	    optimiserDebruitage.setOnAction(e -> {
	        metricsBox.getChildren().clear();
	        List<String> liste = null;
	        try {
	            liste = Main.optimiserDebruitage(pathOriginal, pathNoisy, sigma, patchs);
	        } catch (Exception e2) {
	            // TODO Auto-generated catch block
	            e2.printStackTrace();
	        }
	        pathDenoised = liste.get(0);
	        imageDenoised.setImage(new Image("file:" + pathDenoised));
	        imageDenoised.setVisible(true);
	        placeholderLabelDebruitee.setVisible(false);
	        
	        // Label update according to optimization
	        switch(liste.get(1)) {
	            case "Global_Doux_Bayésien": 
	                labelDenoised.setText(String.format("Résultat optimisé\nType d'extraction : Global\nMéthode de seuillage : Seuillage doux\nType de seuil : Bayésien"));
	                break;
	            case "Global_Dur_Bayésien":
	                labelDenoised.setText(String.format("Résultat optimisé\nType d'extraction : Global\nMéthode de seuillage : Seuillage dur\nType de seuil : Bayésien"));
	                break;
	            case "Global_Doux_Visu":
	                labelDenoised.setText(String.format("Résultat optimisé\nType d'extraction : Global\nMéthode de seuillage : Seuillage doux\nType de seuil : Visu"));
	                break;
	            case "Global_Dur_Visu":
	                labelDenoised.setText(String.format("Résultat optimisé\nType d'extraction : Global\nMéthode de seuillage : Seuillage dur\nType de seuil : Visu"));
	                break;
	            case "Local_Doux_Bayésien": 
	                labelDenoised.setText(String.format("Résultat optimisé\nType d'extraction : Local\nMéthode de seuillage : Seuillage doux\nType de seuil : Bayésien"));
	                break;
	            case "Local_Dur_Bayésien":
	                labelDenoised.setText(String.format("Résultat optimisé\nType d'extraction : Local\nMéthode de seuillage : Seuillage dur\nType de seuil : Bayésien"));
	                break;
	            case "Local_Doux_Visu":
	                labelDenoised.setText(String.format("Résultat optimisé\nType d'extraction : Local\nMéthode de seuillage : Seuillage doux\nType de seuil : Visu"));
	                break;
	            case "Local_Dur_Visu":
	                labelDenoised.setText(String.format("Résultat optimisé\nType d'extraction : Local\nMéthode de seuillage : Seuillage dur\nType de seuil : Visu"));
	                break; 
	        }
	    
	        // Display of metrics 
	        metricsBox.getChildren().addAll(
	                createMetricBox(Main.minMse, "MSE"),
	                createMetricBox(Main.maxPsnr, "PSNR (dB)"),
	                createMetricBox(Main.maxAmelioration, "Amélioration")
	        );
	        
	    });
	    
	    // Listener for patch size slider
	    patchSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
	        patchs = newVal.intValue();
	        patchSizeLabel.setText(String.format("%dx%d pixels", newVal.intValue(), newVal.intValue()));
	    });

	    // Listener for noise (sigma) slider
	    noiseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
	        sigma = newVal.intValue();
	        noiseLabel.setText(String.format("σ = %d", sigma));
	        labelNoisy.setText(String.format("Bruitée (σ = %d)", sigma));
	    });

	    // Add elements to parameters panel
	    paramsPanel.getChildren().addAll(
	            createParamGroup("Niveau de bruit (σ)", noiseSlider, noiseLabel),
	            appliquerBruitage,
	            createParamGroup("Type d'extraction", extractionType),
	            createParamGroup("Taille des patchs", patchSizeSlider, patchSizeLabel),
	            createParamGroup("Méthode de seuillage", seuillageMethod),
	            createParamGroup("Type de seuil", seuilType),
	            appliquerDebruitage,
	            optimiserDebruitage
	    );

	    // Images section
	    VBox imageArea = new VBox(20);
	    imageArea.setPadding(new Insets(20));
	    imageArea.setStyle("-fx-background-color: white;");

	    // Images container
	    HBox imageContainer = new HBox(20);
	    imageContainer.setAlignment(Pos.CENTER);
	    imageContainer.getChildren().addAll(
	            createImageBoxImportable("Image originale", "Originale"),
	            createDynamicImageBoxNoised("Image bruitée", "Bruitée (σ=" + sigma + ")"),
	            createDynamicImageBoxDenoised("Image débruitée", "Résultat")
	    );
	    
	    // Metrics container
	    metricsBox = new HBox(30);
	    metricsBox.setAlignment(Pos.CENTER);
	    
	    // Add images and metrics 
	    imageArea.getChildren().addAll(imageContainer, metricsBox);
	    HBox content = new HBox();
	    content.getChildren().addAll(paramsPanel, imageArea);
	    HBox.setHgrow(imageArea, Priority.ALWAYS);
	    mainContainer.setCenter(content);

	    // Create and display main scene
	    Scene scene = new Scene(mainContainer);
	    primaryStage.setTitle("Débruitage ACP - JavaFX");
	    primaryStage.setScene(scene);
	    primaryStage.show();
    }

    /**
     * Creates a vertical box (VBox) containing a bold label and a control.
     * 
     * @param labelText the text for the label describing the control
     * @param control   the JavaFX control to be displayed below the label
     * @return a VBox containing the label and control vertically spaced
     */
    private VBox createParamGroup(String labelText, Control control) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #444;");
        VBox box = new VBox(8, label, control);
        return box;
    }

    /**
     * Creates a vertical box (VBox) containing a bold label, a slider control,
     * and a label displaying the slider's current value.
     * 
     * @param labelText  the text for the label describing the slider
     * @param slider     the slider control to adjust some parameter
     * @param valueLabel the label showing the current value of the slider
     * @return a VBox containing the label, slider, and value label arranged vertically
     */
    private VBox createParamGroup(String labelText, Slider slider, Label valueLabel) {
        VBox box = createParamGroup(labelText, slider);
        valueLabel.setStyle("-fx-text-fill: #616161;");
        valueLabel.setAlignment(Pos.CENTER);
        box.getChildren().add(valueLabel);
        return box;
    }
    
    /**
     * Creates a VBox containing an image container with placeholder text,
     * a button to import an image from file, and a caption label below.
     * This is used for importing the original image.
     * 
     * @param defaultText placeholder text shown when no image is loaded
     * @param caption     a descriptive label shown below the image area
     * @return a VBox containing the image import UI elements
     */
    private VBox createImageBoxImportable(String defaultText, String caption) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(220);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #bdbdbd; -fx-border-style: dashed; -fx-border-radius: 4px;");

        Label placeholderLabel = new Label(defaultText);
        placeholderLabel.setTextFill(Color.web("#757575"));

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(220);
        imageView.setVisible(false);

        imageContainer.getChildren().addAll(imageView, placeholderLabel);

        Button importButton = new Button("Importer");
        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
            );
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
            	pathOriginal = file.getAbsolutePath();
            	Image image = new Image("file:"+pathOriginal);
                imageView.setImage(image);
                imageView.setVisible(true);
                placeholderLabel.setVisible(false);
                importButton.setText("Changer");
            }
        });

        Label captionLabel = new Label(caption);
        captionLabel.setStyle("-fx-font-weight: 500;");

        box.getChildren().addAll(imageContainer, importButton, captionLabel);
        return box;
    }
    
    /**
     * Creates a VBox for displaying the noisy image with a placeholder label
     * shown when no image is present, along with a caption label.
     * 
     * @param defaultText placeholder text shown when no noisy image is loaded
     * @param caption     a descriptive label below the image area
     * @return a VBox containing the noisy image display UI
     */
    private VBox createDynamicImageBoxNoised(String defaultText, String caption) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(220);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #bdbdbd; -fx-border-style: dashed; -fx-border-radius: 4px;");

        placeholderLabelBruitee = new Label(defaultText);
        placeholderLabelBruitee.setTextFill(Color.web("#757575"));

        imageBruitee = new ImageView();
        imageBruitee.setPreserveRatio(true);
        imageBruitee.setFitHeight(220);
        imageBruitee.setVisible(false);

        imageContainer.getChildren().addAll(imageBruitee, placeholderLabelBruitee);

        bruiteeLabel = new Label(caption);
        bruiteeLabel.setStyle("-fx-font-weight: 500;");

        imageBruitee.getProperties().put("placeholder", placeholderLabelBruitee);

        box.getChildren().addAll(imageContainer, bruiteeLabel);
        return box;
    }
    
    /**
     * Creates a VBox for displaying the denoised image with a placeholder label
     * shown when no image is present, along with a caption label.
     * 
     * @param defaultText placeholder text shown when no denoised image is loaded
     * @param caption     a descriptive label below the image area
     * @return a VBox containing the denoised image display UI
     */
    private VBox createDynamicImageBoxDenoised(String defaultText, String caption) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(220);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #bdbdbd; -fx-border-style: dashed; -fx-border-radius: 4px;");

        placeholderLabelDebruitee = new Label(defaultText);
        placeholderLabelDebruitee.setTextFill(Color.web("#757575"));

        imageDebruitee = new ImageView();
        imageDebruitee.setPreserveRatio(true);
        imageDebruitee.setFitHeight(220);
        imageDebruitee.setVisible(false);

        // Ajouter au conteneur
        imageContainer.getChildren().addAll(imageDebruitee, placeholderLabelDebruitee);

        debruiteeLabel = new Label(caption);
        debruiteeLabel.setStyle("-fx-font-weight: 500;");

        // On stocke aussi le placeholder pour pouvoir le cacher plus tard
        imageDebruitee.getProperties().put("placeholder", placeholderLabelDebruitee);

        box.getChildren().addAll(imageContainer, debruiteeLabel);
        return box;
    }

    /**
     * Creates a VBox containing a formatted metric value and its label.
     * Used for displaying quantitative measures like MSE, PSNR, or improvement.
     * 
     * @param value the numeric metric value to display
     * @param label the name or description of the metric
     * @return a styled VBox containing the metric value and label
     */
        private VBox createMetricBox(double value, String label) {
        Label valueLabel = new Label();
        valueLabel.setText(String.format("%.2f", value));
        valueLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label nameLabel = new Label(label);

        VBox box = new VBox(5, valueLabel, nameLabel);
        box.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-alignment: center; -fx-padding: 15px; -fx-border-radius: 6px;");
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(150);
        return box;
    }

    /**
     * The main entry point of the JavaFX application.
     * 
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
