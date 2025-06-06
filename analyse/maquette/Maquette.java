import java.io.File;

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
	
	private String path;
	private ImageView imageBruitee;
	private Label placeholderLabelBruitee;

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
        Label patchSizeLabel = new Label("15x15 pixels");

        Slider noiseSlider = new Slider(0, 50, 20);
        Label noiseLabel = new Label("σ = 20");

        ComboBox<String> thresholdMethod = new ComboBox<>();
        thresholdMethod.getItems().addAll("Seuillage dur", "Seuillage doux");
        thresholdMethod.setValue("Seuillage dur");
        
        Button appliquerBruitage = new Button("Appliquer le bruitage");
        appliquerBruitage.setMaxWidth(Double.MAX_VALUE);
        appliquerBruitage.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-font-weight: 500;");
        appliquerBruitage.setOnAction(e -> {
        	try {
				Main.principal(path);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	imageBruitee.setImage(new Image("file:/home/cytech/Java/javafx/tp5/images/bruitees/image_noisy_sigma20.0.png"));
        	imageBruitee.setVisible(true);
            placeholderLabelBruitee.setVisible(false);

        });
        

        Button applyButton = new Button("Appliquer le débruitage");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-font-weight: 500;");

        patchSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            patchSizeLabel.setText(String.format("%dx%d pixels", newVal.intValue(), newVal.intValue()));
        });

        noiseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            noiseLabel.setText(String.format("σ = %d", newVal.intValue()));
        });

        paramsPanel.getChildren().addAll(
        		createParamGroup("Niveau de bruit (σ)", noiseSlider, noiseLabel),
        		appliquerBruitage,
                createParamGroup("Type d'extraction", extractionType),
                createParamGroup("Taille des patchs", patchSizeSlider, patchSizeLabel),
                createParamGroup("Méthode de seuillage", thresholdMethod),
                applyButton
        );

        // Image section
        VBox imageArea = new VBox(20);
        imageArea.setPadding(new Insets(20));
        imageArea.setStyle("-fx-background-color: white;");

        HBox imageContainer = new HBox(20);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.getChildren().addAll(
                createImageBoxImportable("Image originale", "Originale"),
                createDynamicImageBox("Image bruitée", "Bruitée (σ=20)"),
                createImageBox("Image débruitée", "Résultat")
        );

        HBox metricsBox = new HBox(20);
        metricsBox.setAlignment(Pos.CENTER);
        metricsBox.getChildren().addAll(
                createMetricBox("24.7", "MSE"),
                createMetricBox("34.2", "PSNR (dB)"),
                createMetricBox("78%", "Amélioration")
        );

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
            	path = file.getAbsolutePath();
            	Image image = new Image("file:"+path);
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
    
    private VBox createDynamicImageBox(String defaultText, String caption) {
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

        Label captionLabel = new Label(caption);
        captionLabel.setStyle("-fx-font-weight: 500;");

        // On stocke aussi le placeholder pour pouvoir le cacher plus tard
        imageBruitee.getProperties().put("placeholder", placeholderLabelBruitee);

        box.getChildren().addAll(imageContainer, captionLabel);
        return box;
    }



    private VBox createImageBox(String placeholder, String caption) {
        Label placeholderLabel = new Label(placeholder);
        placeholderLabel.setMinHeight(220);
        placeholderLabel.setAlignment(Pos.CENTER);
        placeholderLabel.setMaxWidth(Double.MAX_VALUE);
        placeholderLabel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #bdbdbd; -fx-border-style: dashed; -fx-border-radius: 4px; -fx-padding: 20px; -fx-text-fill: #757575;");
        
        Label captionLabel = new Label(caption);
        captionLabel.setStyle("-fx-font-weight: 500;");

        VBox box = new VBox(12, placeholderLabel, captionLabel);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");

        VBox.setVgrow(placeholderLabel, Priority.ALWAYS);
        return box;
    }

    private VBox createMetricBox(String value, String label) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        Label nameLabel = new Label(label);

        VBox box = new VBox(5, valueLabel, nameLabel);
        box.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white; -fx-alignment: center; -fx-padding: 15px; -fx-border-radius: 6px;");
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(100);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
