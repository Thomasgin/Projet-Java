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

public class Maquette extends Application {
	
	private String pathOriginal;
	private String pathNoisy;
	private String pathDenoised;
	private ImageView imageBruitee;
	private ImageView imageDebruitee;
	private Label placeholderLabelBruitee;
	private Label placeholderLabelDebruitee;
	private int patchs;
	private int sigma;
	private Label bruiteeLabel;
	private Label debruiteeLabel;
	private HBox metricsBox;

    @Override
    public void start(Stage primaryStage) {
        BorderPane mainContainer = new BorderPane();
        mainContainer.setPrefSize(900, 650);
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Header
        Label header = new Label("Débruitage d'images par ACP");
        header.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 18px 25px;");
        HBox headerBox = new HBox(header);
        mainContainer.setTop(headerBox);

        // Left panel (params)
        VBox paramsPanel = new VBox(20);
        paramsPanel.setPadding(new Insets(20));
        paramsPanel.setPrefWidth(280);
        paramsPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 1px 0 0;");

        ComboBox<String> extractionType = new ComboBox<>();
        extractionType.getItems().addAll("Global PCA", "Local PCA");
        extractionType.setValue("Global PCA");

        Slider patchSizeSlider = new Slider(5, 30, 15);
        patchs = 15;
        Label patchSizeLabel = new Label("15x15 pixels");

        Slider noiseSlider = new Slider(0, 50, 20);
        sigma = 20;
        Label noiseLabel = new Label("σ = " + sigma);

        ComboBox<String> seuillageMethod = new ComboBox<>();
        seuillageMethod.getItems().addAll("Seuillage dur", "Seuillage doux");
        seuillageMethod.setValue("Seuillage dur");
        
        ComboBox<String> seuilType = new ComboBox<>();
        seuilType.getItems().addAll("Visu", "Bayésien");
        seuilType.setValue("Visu");
        
        
        Button appliquerBruitage = new Button("Appliquer le bruitage");
        appliquerBruitage.setMaxWidth(Double.MAX_VALUE);
        appliquerBruitage.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-font-weight: 500;");
        appliquerBruitage.setOnAction(e -> {
        	try {
				Main.bruitage(pathOriginal, sigma);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	pathNoisy = "images/bruitees/image_noisy_sigma" + sigma + ".jpeg";
        	imageBruitee.setImage(new Image("file:" + pathNoisy));
        	imageBruitee.setVisible(true);
            placeholderLabelBruitee.setVisible(false);

        });
        

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
        	imageDebruitee.setImage(new Image("file:" + pathDenoised));
        	imageDebruitee.setVisible(true);
            placeholderLabelDebruitee.setVisible(false);
            
            debruiteeLabel.setText(String.format("Type d'extraction : " + extractionType.getValue() + "\nMéthode de Seuillage : " + seuillageMethod.getValue() + "\nType de Seuil : " + seuilType.getValue()));
        
            metricsBox.getChildren().addAll(
                    createMetricBox(Main.mse, "MSE"),
                    createMetricBox(Main.psnr, "PSNR (dB)"),
                    createMetricBox(Main.amelioration, "Amélioration")
            );
        });
        
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
        	imageDebruitee.setImage(new Image("file:" + pathDenoised));
        	imageDebruitee.setVisible(true);
            placeholderLabelDebruitee.setVisible(false);
            
            switch(liste.get(1)) {
            	case "Global_Doux_Bayésien": 
            		debruiteeLabel.setText(String.format("Résultat optimisé\nType d'extraction : Global\nMéthode de seuillage : Seuillage doux\nType de seuil : Bayésien"));
            		break;
            	case "Global_Dur_Bayésien":
            		debruiteeLabel.setText(String.format("Résultat optimisé\nType d'extraction : Global\nMéthode de seuillage : Seuillage dur\nType de seuil : Bayésien"));
            		break;
            	case "Global_Doux_Visu":
            		debruiteeLabel.setText(String.format("Résultat optimisé\nType d'extraction : Global\nMéthode de seuillage : Seuillage doux\nType de seuil : Visu"));
            		break;
            	case "Global_Dur_Visu":
            		debruiteeLabel.setText(String.format("Résultat optimisé\nType d'extraction : Global\nMéthode de seuillage : Seuillage dur\nType de seuil : Visu"));
            		break;
            	case "Local_Doux_Bayésien": 
            		debruiteeLabel.setText(String.format("Résultat optimisé\nType d'extraction : Local\nMéthode de seuillage : Seuillage doux\nType de seuil : Bayésien"));
            		break;
            	case "Local_Dur_Bayésien":
            		debruiteeLabel.setText(String.format("Résultat optimisé\nType d'extraction : Local\nMéthode de seuillage : Seuillage dur\nType de seuil : Bayésien"));
            		break;
            	case "Local_Doux_Visu":
            		debruiteeLabel.setText(String.format("Résultat optimisé\nType d'extraction : Local\nMéthode de seuillage : Seuillage doux\nType de seuil : Visu"));
            		break;
            	case "Local_Dur_Visu":
            		debruiteeLabel.setText(String.format("Résultat optimisé\nType d'extraction : Local\nMéthode de seuillage : Seuillage dur\nType de seuil : Visu"));
            		break; 
            }
        
            metricsBox.getChildren().addAll(
                    createMetricBox(Main.minMse, "MSE"),
                    createMetricBox(Main.maxPsnr, "PSNR (dB)"),
                    createMetricBox(Main.maxAmelioration, "Amélioration")
            );
        	
        });
        
        patchSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
        	patchs = newVal.intValue();
            patchSizeLabel.setText(String.format("%dx%d pixels", newVal.intValue(), newVal.intValue()));
        });

        noiseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
        	sigma = newVal.intValue();
            noiseLabel.setText(String.format("σ = %d", sigma));
            bruiteeLabel.setText(String.format("Bruitée (σ = %d)", sigma));
        });

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

        // Image section
        VBox imageArea = new VBox(20);
        imageArea.setPadding(new Insets(20));
        imageArea.setStyle("-fx-background-color: white;");

        HBox imageContainer = new HBox(20);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.getChildren().addAll(
                createImageBoxImportable("Image originale", "Originale"),
                createDynamicImageBoxNoised("Image bruitée", "Bruitée (σ=" + sigma + ")"),
                createDynamicImageBoxDenoised("Image débruitée", "Résultat")
        );
        
        metricsBox = new HBox(30);
        metricsBox.setAlignment(Pos.CENTER);

        imageArea.getChildren().addAll(imageContainer, metricsBox);

        HBox content = new HBox();
        content.getChildren().addAll(paramsPanel, imageArea);
        HBox.setHgrow(imageArea, Priority.ALWAYS);

        mainContainer.setCenter(content);

        // Button bar
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(15, 25, 15, 25));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 1px 0 0 0;");

        Button exportButton = new Button("Exporter les résultats");
        Button resetButton = new Button("Réinitialiser");
        Button saveButton = new Button("Enregistrer");

        for (Button b : new Button[]{exportButton, resetButton}) {
            b.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #424242;");
        }

        saveButton.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white;");

        buttonBar.getChildren().addAll(exportButton, resetButton, saveButton);
        mainContainer.setBottom(buttonBar);

        Scene scene = new Scene(mainContainer);
        primaryStage.setTitle("Débruitage ACP - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createParamGroup(String labelText, Control control) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #444;");
        VBox box = new VBox(8, label, control);
        return box;
    }

    private VBox createParamGroup(String labelText, Slider slider, Label valueLabel) {
        VBox box = createParamGroup(labelText, slider);
        valueLabel.setStyle("-fx-text-fill: #616161;");
        valueLabel.setAlignment(Pos.CENTER);
        box.getChildren().add(valueLabel);
        return box;
    }
    
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

        // Ajouter au conteneur
        imageContainer.getChildren().addAll(imageBruitee, placeholderLabelBruitee);

        bruiteeLabel = new Label(caption);
        bruiteeLabel.setStyle("-fx-font-weight: 500;");

        // On stocke aussi le placeholder pour pouvoir le cacher plus tard
        imageBruitee.getProperties().put("placeholder", placeholderLabelBruitee);

        box.getChildren().addAll(imageContainer, bruiteeLabel);
        return box;
    }
    
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

    
    

    public static void main(String[] args) {
        launch(args);
    }
}
