package de.mf.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class QrGeneratorApp extends Application {

    private static final String APP_NAME = "QR-Code Generator";
    private static final String APP_FALLBACK_VERSION = "1.0.0";
    private static final String INVALID_STYLE_CLASS = "invalid-field";
    private static final String VALIDATION_SUCCESS_STYLE_CLASS = "validation-success";
    private static final String VALIDATION_ERROR_STYLE_CLASS = "validation-error";
    private static final int SVG_BASE_SIZE = 360;
    private static final int SVG_MODULE_SCALE = 4;

    private enum ContentType {
        TEXT("Text"),
        YOUTUBE("YouTube"),
        WEBSEITE("Webseite"),
        WLAN_LOGIN("WLAN"),
        STANDORT("Standort");

        private final String label;

        ContentType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final ComboBox<ContentType> typeBox = new ComboBox<>();
    private final TextField tfUrl = new TextField();
    private final TextArea taText = new TextArea();
    private final TextField tfAddress = new TextField();
    private final TextField tfLat = new TextField();
    private final TextField tfLon = new TextField();
    private final TextField tfSsid = new TextField();
    private final PasswordField pfWifi = new PasswordField();
    private final ComboBox<String> cbAuth = new ComboBox<>();

    private final ColorPicker cpQr = new ColorPicker(Color.web("#1E6BFF"));
    private final ColorPicker cpBg = new ColorPicker(Color.web("#F8FAFC"));
    private final CheckBox cbTransparentBg = new CheckBox("Transparent");

    private final CheckBox cbLogo = new CheckBox("Logo in der Mitte");
    private final Label lblLogoPath = new Label("Kein Logo ausgewählt");
    private File selectedLogo;

    private final Label lblUrl = new Label("URL:");
    private final Label lblText = new Label("Text:");
    private final Label lblLocationMode = new Label("Standort-Modus:");
    private final Label lblAddress = new Label("Adresse:");
    private final Label lblLat = new Label("Latitude:");
    private final Label lblLon = new Label("Longitude:");
    private final Label lblSsid = new Label("SSID:");
    private final Label lblPass = new Label("Passwort:");
    private final Label lblAuth = new Label("Auth:");

    private final RadioButton rbAddress = new RadioButton("Adresse");
    private final RadioButton rbCoordinates = new RadioButton("Koordinaten");
    private final ToggleGroup tgLocationMode = new ToggleGroup();
    private final HBox locationModeBox = new HBox(10, rbAddress, rbCoordinates);

    // Über-Button entfernt
    private final Button btnSave = new Button("QR-Code generieren & speichern");
    private final Label lblHelp = new Label();
    private final Label lblValidation = new Label();
    private final Label lblVersion = new Label();
    private final PauseTransition livePreviewDelay = new PauseTransition(Duration.millis(250));

    private final ImageView preview = new ImageView();

    @Override
    public void start(Stage stage) {
        stage.setTitle(APP_NAME);

        // Drag & Drop für Textfelder/TextArea
        installTextDragAndDrop(tfUrl);
        installTextDragAndDrop(taText);
        installTextDragAndDrop(tfAddress);
        installTextDragAndDrop(tfLat);
        installTextDragAndDrop(tfLon);
        installTextDragAndDrop(tfSsid);

        typeBox.getItems().addAll(ContentType.values());
        typeBox.setValue(ContentType.TEXT);

        tfUrl.setPromptText("https://www.youtube.com/watch?v=...");
        taText.setPromptText("Dein Text...");
        taText.setPrefRowCount(3);
        tfAddress.setPromptText("z. B. Bahnhofstrasse 1, Zürich");
        tfLat.setPromptText("z. B. 47.3769");
        tfLon.setPromptText("z. B. 8.5417");
        tfSsid.setPromptText("WLAN-Name (SSID)");
        pfWifi.setPromptText("WLAN-Passwort");
        cbAuth.getItems().addAll("WPA", "WEP", "nopass");
        cbAuth.setValue("WPA");
        cbTransparentBg.setSelected(false);
        cpBg.disableProperty().bind(cbTransparentBg.selectedProperty());

        rbAddress.setToggleGroup(tgLocationMode);
        rbCoordinates.setToggleGroup(tgLocationMode);
        rbAddress.setSelected(true);
        locationModeBox.setAlignment(Pos.CENTER_LEFT);

        livePreviewDelay.setOnFinished(e -> generatePreview());


        Button btnPickLogo = new Button("Logo auswählen");
        btnPickLogo.setOnAction(e -> pickLogo(stage));
        btnSave.setOnAction(e -> saveQr(stage));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(0, 12, 12, 12));

        int r = 0;
        form.add(lblUrl, 0, r);
        form.add(tfUrl, 1, r++);

        form.add(lblText, 0, r);
        form.add(taText, 1, r++);

        form.add(lblLocationMode, 0, r);
        form.add(locationModeBox, 1, r++);

        form.add(lblAddress, 0, r);
        form.add(tfAddress, 1, r++);

        form.add(lblLat, 0, r);
        form.add(tfLat, 1, r++);

        form.add(lblLon, 0, r);
        form.add(tfLon, 1, r++);

        form.add(lblSsid, 0, r);
        form.add(tfSsid, 1, r++);

        form.add(lblPass, 0, r);
        form.add(pfWifi, 1, r++);

        form.add(lblAuth, 0, r);
        form.add(cbAuth, 1, r++);

        form.add(new Label("QR-Farbe:"), 0, r);
        form.add(cpQr, 1, r++);

        form.add(new Label("Hintergrund:"), 0, r);
        HBox bgRow = new HBox(10, cpBg, cbTransparentBg);
        bgRow.setAlignment(Pos.CENTER_LEFT);
        form.add(bgRow, 1, r++);

        HBox logoRow = new HBox(10, cbLogo, btnPickLogo, lblLogoPath);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        form.add(new Label("Logo:"), 0, r);
        form.add(logoRow, 1, r++);

        HBox actionRow = new HBox(10, btnSave);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        HBox typeRow = new HBox(10, new Label("Typ:"), typeBox);
        typeRow.setAlignment(Pos.CENTER_LEFT);

        lblHelp.getStyleClass().add("help-label");
        lblHelp.setWrapText(true);

        lblVersion.setText("Version " + getAppVersion());
        lblVersion.getStyleClass().add("help-label");

        lblValidation.getStyleClass().add("validation-label");
        lblValidation.getStyleClass().add(VALIDATION_ERROR_STYLE_CLASS);
        lblValidation.setWrapText(true);

        preview.setFitWidth(280);
        preview.setFitHeight(280);
        preview.setPreserveRatio(true);

        VBox root = new VBox(8, typeRow, lblVersion, lblHelp, lblValidation, form, actionRow, new Label("Vorschau:"), preview);
        root.setPadding(new Insets(10));


        // Menüleiste mit Datei, About, Ansicht
        MenuBar menuBar = new MenuBar();
        Menu menuDatei = new Menu("Datei");
        MenuItem speichernUnter = new MenuItem("Speichern unter");
        speichernUnter.setOnAction(e -> saveQr(stage));
        menuDatei.getItems().add(speichernUnter);

        Menu menuAbout = new Menu("About");
        MenuItem aboutItem = new MenuItem("Über QR Generator");
        aboutItem.setOnAction(e -> showAboutDialog());
        menuAbout.getItems().add(aboutItem);

        Menu menuAnsicht = new Menu("Ansicht");
        CheckMenuItem darkModeMenuToggle = new CheckMenuItem("Darkmode");
        menuAnsicht.getItems().add(darkModeMenuToggle);

        menuBar.getMenus().addAll(menuDatei, menuAnsicht, menuAbout);

        // Rechts: App-Info und großer Darkmode-Button
        Label infoLabel = new Label(APP_NAME + " " + getAppVersion());
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B; -fx-padding: 0 10 0 0;");
        Button darkModeBtn = new Button("\u263E"); // ☾
        darkModeBtn.setFocusTraversable(false);
        darkModeBtn.setStyle("-fx-background-radius: 100; -fx-font-size: 22px; -fx-padding: 2 16 2 16; -fx-background-color: transparent; -fx-text-fill: #64748B;");

        HBox rightBox = new HBox(16, infoLabel, darkModeBtn);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setStyle("-fx-padding: 0 18 0 0;");

        // Menüleiste links, Info & Darkmode-Button rechts
        HBox menuBarRow = new HBox();
        menuBarRow.getChildren().addAll(menuBar);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        menuBarRow.getChildren().addAll(spacer, rightBox);
        menuBarRow.setAlignment(Pos.CENTER_LEFT);
        menuBarRow.setSpacing(0);
        menuBarRow.setStyle("-fx-background-color: transparent;");

        VBox mainLayout = new VBox(menuBarRow, root);

        typeBox.valueProperty().addListener((obs, oldV, newV) -> {
            updateFieldVisibility();
            updateValidationState();
            scheduleLivePreview();
        });
        tgLocationMode.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            updateFieldVisibility();
            updateValidationState();
            scheduleLivePreview();
        });
        installRealtimeValidation();
        installLivePreviewListeners();
        updateFieldVisibility();
        updateValidationState();
        scheduleLivePreview();

        Scene scene = new Scene(mainLayout, 760, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // ECHTER DARKMODE: StyleClass und Styles explizit setzen
        Runnable setDarkMode = () -> {
            boolean isDark = root.getStyleClass().contains("dark-mode");
            if (isDark) {
                if (!scene.getStylesheets().contains(getClass().getResource("/dark-mode.css").toExternalForm()))
                    scene.getStylesheets().add(getClass().getResource("/dark-mode.css").toExternalForm());
                darkModeBtn.setText("\u2600"); // ☀
                darkModeMenuToggle.setSelected(true);
                scene.setFill(javafx.scene.paint.Color.web("#23272F"));
                root.getStyleClass().add("dark-mode");
                mainLayout.getStyleClass().add("dark-mode");
                menuBar.getStyleClass().add("dark-mode");
                form.getStyleClass().add("dark-mode");
                taText.setStyle("-fx-control-inner-background: #23272F; -fx-text-fill: #E0E6F0;");
                tfUrl.setStyle("-fx-control-inner-background: #23272F; -fx-text-fill: #E0E6F0;");
                tfAddress.setStyle("-fx-control-inner-background: #23272F; -fx-text-fill: #E0E6F0;");
                tfLat.setStyle("-fx-control-inner-background: #23272F; -fx-text-fill: #E0E6F0;");
                tfLon.setStyle("-fx-control-inner-background: #23272F; -fx-text-fill: #E0E6F0;");
                tfSsid.setStyle("-fx-control-inner-background: #23272F; -fx-text-fill: #E0E6F0;");
                pfWifi.setStyle("-fx-control-inner-background: #23272F; -fx-text-fill: #E0E6F0;");
                menuBar.setStyle("-fx-background-color: #23272F; -fx-border-color: #23272F;");
                // Checkboxen und ComboBoxen explizit hell
                cbTransparentBg.setStyle("-fx-text-fill: #E0E6F0;");
                cbLogo.setStyle("-fx-text-fill: #E0E6F0;");
                typeBox.setStyle("-fx-background-color: #23272F; -fx-text-fill: #E0E6F0;");
                cbAuth.setStyle("-fx-background-color: #23272F; -fx-text-fill: #E0E6F0;");
            } else {
                scene.getStylesheets().remove(getClass().getResource("/dark-mode.css").toExternalForm());
                darkModeBtn.setText("\u263E"); // ☾
                darkModeMenuToggle.setSelected(false);
                scene.setFill(javafx.scene.paint.Color.web("#F8FAFC"));
                root.getStyleClass().remove("dark-mode");
                mainLayout.getStyleClass().remove("dark-mode");
                menuBar.getStyleClass().remove("dark-mode");
                form.getStyleClass().remove("dark-mode");
                taText.setStyle("");
                tfUrl.setStyle("");
                tfAddress.setStyle("");
                tfLat.setStyle("");
                tfLon.setStyle("");
                tfSsid.setStyle("");
                pfWifi.setStyle("");
                menuBar.setStyle("");
                cbTransparentBg.setStyle("");
                cbLogo.setStyle("");
                typeBox.setStyle("");
                cbAuth.setStyle("");
            }
        };

        darkModeBtn.setOnAction(e -> {
            if (!root.getStyleClass().contains("dark-mode")) {
                root.getStyleClass().add("dark-mode");
            } else {
                root.getStyleClass().remove("dark-mode");
            }
            setDarkMode.run();
        });
        darkModeMenuToggle.selectedProperty().addListener((obs, wasDark, isDark) -> {
            if (isDark && !root.getStyleClass().contains("dark-mode")) {
                root.getStyleClass().add("dark-mode");
            } else if (!isDark && root.getStyleClass().contains("dark-mode")) {
                root.getStyleClass().remove("dark-mode");
            }
            setDarkMode.run();
        });

        setDarkMode.run();
        stage.setScene(scene);
        stage.show();
    }

    private void updateFieldVisibility() {
        ContentType t = typeBox.getValue();

        boolean showUrl = t == ContentType.YOUTUBE || t == ContentType.WEBSEITE;
        boolean showText = t == ContentType.TEXT;
        boolean showStandort = t == ContentType.STANDORT;
        boolean useAddressForLocation = rbAddress.isSelected();
        boolean showWlan = t == ContentType.WLAN_LOGIN;

        setRowVisible(lblUrl, tfUrl, showUrl);
        setRowVisible(lblText, taText, showText);
        setRowVisible(lblLocationMode, locationModeBox, showStandort);
        setRowVisible(lblAddress, tfAddress, showStandort && useAddressForLocation);
        setRowVisible(lblLat, tfLat, showStandort && !useAddressForLocation);
        setRowVisible(lblLon, tfLon, showStandort && !useAddressForLocation);
        setRowVisible(lblSsid, tfSsid, showWlan);
        setRowVisible(lblPass, pfWifi, showWlan);
        setRowVisible(lblAuth, cbAuth, showWlan);

        if (t == ContentType.YOUTUBE) {
            tfUrl.setPromptText("https://www.youtube.com/watch?v=...");
        } else if (t == ContentType.WEBSEITE) {
            tfUrl.setPromptText("https://deine-webseite.de");
        }

        lblHelp.setText(buildHelpText(t));
    }

    private String buildHelpText(ContentType t) {
        return switch (t) {
            case YOUTUBE -> "Füge einen gültigen YouTube-Link ein (youtube.com oder youtu.be).";
            case WEBSEITE -> "Trage eine vollständige URL ein, z. B. https://deine-seite.de.";
            case TEXT -> "Gib einen beliebigen Text ein, den der QR-Code enthalten soll.";
            case STANDORT -> rbAddress.isSelected()
                    ? "Adressmodus: Gib eine Adresse ein. Der QR-Code öffnet Google Maps direkt mit dieser Suche."
                    : "Koordinatenmodus: Gib Latitude (-90 bis 90) und Longitude (-180 bis 180) ein.";
            case WLAN_LOGIN -> "WLAN-QR: SSID eingeben, Auth wählen und bei WPA/WEP das Passwort ergänzen.";
        };
    }

    private static void setRowVisible(Node label, Node input, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        input.setVisible(visible);
        input.setManaged(visible);
    }

    /**
     * Aktiviert Drag & Drop für Textfelder/TextArea (Text wird eingefügt).
     */
    private static void installTextDragAndDrop(TextInputControl control) {
        control.setOnDragOver(event -> {
            if (event.getGestureSource() != control && event.getDragboard().hasString()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        control.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                control.insertText(control.getCaretPosition(), db.getString());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void installRealtimeValidation() {
        tfUrl.textProperty().addListener((obs, oldV, newV) -> updateValidationState());
        taText.textProperty().addListener((obs, oldV, newV) -> updateValidationState());
        tfAddress.textProperty().addListener((obs, oldV, newV) -> {
            if (typeBox.getValue() == ContentType.STANDORT && newV != null && !newV.isBlank() && !rbAddress.isSelected()) {
                rbAddress.setSelected(true);
            }
            updateValidationState();
        });
        tfLat.textProperty().addListener((obs, oldV, newV) -> updateValidationState());
        tfLon.textProperty().addListener((obs, oldV, newV) -> updateValidationState());
        tfSsid.textProperty().addListener((obs, oldV, newV) -> updateValidationState());
        pfWifi.textProperty().addListener((obs, oldV, newV) -> updateValidationState());
        cbAuth.valueProperty().addListener((obs, oldV, newV) -> updateValidationState());
    }

    private void installLivePreviewListeners() {
        tfUrl.textProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        taText.textProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        tfAddress.textProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        tfLat.textProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        tfLon.textProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        tfSsid.textProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        pfWifi.textProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        cbAuth.valueProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        cpQr.valueProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        cpBg.valueProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        cbTransparentBg.selectedProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
        cbLogo.selectedProperty().addListener((obs, oldV, newV) -> scheduleLivePreview());
    }

    private void scheduleLivePreview() {
        livePreviewDelay.playFromStart();
    }

    private void updateValidationState() {
        ContentType t = typeBox.getValue();
        String msg = null;

        clearInvalidStyles();

        switch (t) {
            case YOUTUBE -> {
                String v = value(tfUrl.getText());
                if (isBlank(v)) {
                    msg = "Bitte YouTube-Link eingeben.";
                    setInvalid(tfUrl, true);
                } else if (!isYouTubeUrl(v)) {
                    msg = "Bitte einen gültigen YouTube-Link verwenden.";
                    setInvalid(tfUrl, true);
                }
            }
            case WEBSEITE -> {
                String v = value(tfUrl.getText());
                if (isBlank(v)) {
                    msg = "Bitte URL eingeben.";
                    setInvalid(tfUrl, true);
                } else if (!isHttpUrl(v)) {
                    msg = "Bitte eine gültige URL mit http/https eingeben.";
                    setInvalid(tfUrl, true);
                }
            }
            case TEXT -> {
                if (isBlank(taText.getText())) {
                    msg = "Bitte Text eingeben.";
                    setInvalid(taText, true);
                }
            }
            case STANDORT -> {
                if (rbAddress.isSelected()) {
                    if (isBlank(tfAddress.getText())) {
                        msg = "Bitte Adresse eingeben.";
                        setInvalid(tfAddress, true);
                    }
                } else {
                    String latRaw = value(tfLat.getText());
                    String lonRaw = value(tfLon.getText());

                    if (isBlank(latRaw)) {
                        msg = "Bitte Latitude eingeben.";
                        setInvalid(tfLat, true);
                    } else if (!isLatitude(latRaw)) {
                        msg = "Latitude muss zwischen -90 und 90 liegen.";
                        setInvalid(tfLat, true);
                    }

                    if (msg == null) {
                        if (isBlank(lonRaw)) {
                            msg = "Bitte Longitude eingeben.";
                            setInvalid(tfLon, true);
                        } else if (!isLongitude(lonRaw)) {
                            msg = "Longitude muss zwischen -180 und 180 liegen.";
                            setInvalid(tfLon, true);
                        }
                    }
                }
            }
            case WLAN_LOGIN -> {
                if (isBlank(tfSsid.getText())) {
                    msg = "Bitte SSID eingeben.";
                    setInvalid(tfSsid, true);
                } else if (!"nopass".equals(cbAuth.getValue()) && isBlank(pfWifi.getText())) {
                    msg = "Bitte WLAN-Passwort eingeben.";
                    setInvalid(pfWifi, true);
                }
            }
        }

        boolean valid = msg == null;
        btnSave.setDisable(!valid);
        setValidationState(valid, msg);
    }

    private void setValidationState(boolean valid, String errorMsg) {
        lblValidation.getStyleClass().removeAll(VALIDATION_SUCCESS_STYLE_CLASS, VALIDATION_ERROR_STYLE_CLASS);

        if (valid) {
            lblValidation.getStyleClass().add(VALIDATION_SUCCESS_STYLE_CLASS);
            lblValidation.setText("✓ Eingaben sind gültig.");
        } else {
            lblValidation.getStyleClass().add(VALIDATION_ERROR_STYLE_CLASS);
            lblValidation.setText(errorMsg == null ? "Bitte Eingaben prüfen." : errorMsg);
        }
    }

    private void clearInvalidStyles() {
        setInvalid(tfUrl, false);
        setInvalid(taText, false);
        setInvalid(tfAddress, false);
        setInvalid(tfLat, false);
        setInvalid(tfLon, false);
        setInvalid(tfSsid, false);
        setInvalid(pfWifi, false);
    }

    private static void setInvalid(Control control, boolean invalid) {
        if (invalid) {
            if (!control.getStyleClass().contains(INVALID_STYLE_CLASS)) {
                control.getStyleClass().add(INVALID_STYLE_CLASS);
            }
        } else {
            control.getStyleClass().remove(INVALID_STYLE_CLASS);
        }
    }

    private void pickLogo(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Logo auswählen");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            selectedLogo = file;
            lblLogoPath.setText(file.getName());
            scheduleLivePreview();
        }
    }

    private void generatePreview() {
        try {
            String content = buildContent();
            BufferedImage img = generateQrBuffered(
                    content,
                    900,
                    fxToAwt(cpQr.getValue()),
                    effectiveBgColor(),
                    (cbLogo.isSelected() && selectedLogo != null) ? selectedLogo.getAbsolutePath() : null
            );
            preview.setImage(toFxImage(img));
        } catch (Exception ex) {
            preview.setImage(null);
        }
    }

    private void saveQr(Stage stage) {
        try {
            String content = buildContent();
            FileChooser fc = new FileChooser();
            fc.setTitle("QR-Code speichern");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG Datei", "*.png"),
                    new FileChooser.ExtensionFilter("SVG Datei", "*.svg")
            );
            fc.setInitialFileName("qr_code.png");
            File out = fc.showSaveDialog(stage);
            if (out == null) return;

            boolean saveAsSvg = shouldSaveAsSvg(fc, out);
            out = ensureFileExtension(out, saveAsSvg ? ".svg" : ".png");

            if (saveAsSvg) {
                String svg = generateQrSvg(
                        content,
                    SVG_BASE_SIZE,
                        cpQr.getValue(),
                        cbTransparentBg.isSelected() ? null : cpBg.getValue(),
                    (cbLogo.isSelected() && selectedLogo != null) ? selectedLogo.getAbsolutePath() : null,
                    SVG_MODULE_SCALE
                );
                Files.writeString(out.toPath(), svg, StandardCharsets.UTF_8);
            } else {
                BufferedImage img = generateQrBuffered(
                        content,
                        1000,
                        fxToAwt(cpQr.getValue()),
                        effectiveBgColor(),
                        (cbLogo.isSelected() && selectedLogo != null) ? selectedLogo.getAbsolutePath() : null
                );

                ImageIO.write(img, "png", out);
            }

            showInfo("Fertig", "QR-Code gespeichert:\n" + out.getAbsolutePath());
        } catch (Exception ex) {
            showError("Fehler beim Speichern", ex.getMessage());
        }
    }

    private java.awt.Color effectiveBgColor() {
        return cbTransparentBg.isSelected()
                ? new java.awt.Color(0, 0, 0, 0)
                : fxToAwt(cpBg.getValue());
    }

    private static boolean shouldSaveAsSvg(FileChooser fc, File out) {
        String n = out.getName().toLowerCase();
        if (n.endsWith(".svg")) return true;
        if (n.endsWith(".png")) return false;

        FileChooser.ExtensionFilter selected = fc.getSelectedExtensionFilter();
        if (selected == null) return false;
        return selected.getExtensions().stream().anyMatch(e -> "*.svg".equalsIgnoreCase(e));
    }

    private static File ensureFileExtension(File out, String ext) {
        String n = out.getName().toLowerCase();
        if (n.endsWith(ext)) return out;
        return new File(out.getParentFile(), out.getName() + ext);
    }

        private static String generateQrSvg(
            String content,
            int size,
            Color qrColor,
            Color bgColor,
            String logoPath,
            int moduleScale
        ) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int scaledW = w * moduleScale;
        int scaledH = h * moduleScale;

        StringBuilder sb = new StringBuilder(1024 * 128);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
            .append(scaledW)
                .append(' ')
            .append(scaledH)
                .append("\" shape-rendering=\"crispEdges\">\n");

        if (bgColor != null) {
            sb.append("  <rect width=\"100%\" height=\"100%\" fill=\"")
                    .append(toHex(bgColor))
                    .append("\"");
            if (bgColor.getOpacity() < 1.0) {
                sb.append(" fill-opacity=\"").append(trimDouble(bgColor.getOpacity())).append("\"");
            }
            sb.append("/>\n");
        }

        sb.append("  <g fill=\"").append(toHex(qrColor)).append("\"");
        if (qrColor.getOpacity() < 1.0) {
            sb.append(" fill-opacity=\"").append(trimDouble(qrColor.getOpacity())).append("\"");
        }
        sb.append(">\n");

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (matrix.get(x, y)) {
                    sb.append("    <rect x=\"")
                            .append(x * moduleScale)
                            .append("\" y=\"")
                            .append(y * moduleScale)
                            .append("\" width=\"")
                            .append(moduleScale)
                            .append("\" height=\"")
                            .append(moduleScale)
                            .append("\"/>\n");
                }
            }
        }

        if (logoPath != null && !logoPath.isBlank()) {
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                String dataUri = toPngDataUri(logoFile);
                int logoSize = size / 5;
                int lx = (w - logoSize) / 2;
                int ly = (h - logoSize) / 2;
                int padding = 4;
                int cornerRadius = 8;

                int sx = lx * moduleScale;
                int sy = ly * moduleScale;
                int sLogoSize = logoSize * moduleScale;
                int sPadding = padding * moduleScale;
                int sCornerRadius = cornerRadius * moduleScale;

                sb.append("    <defs>\n")
                    .append("      <clipPath id=\"logoClip\">\n")
                    .append("        <rect x=\"")
                    .append(sx)
                    .append("\" y=\"")
                    .append(sy)
                    .append("\" width=\"")
                    .append(sLogoSize)
                    .append("\" height=\"")
                    .append(sLogoSize)
                    .append("\" rx=\"")
                    .append(sCornerRadius)
                    .append("\" ry=\"")
                    .append(sCornerRadius)
                    .append("\"/>\n")
                    .append("      </clipPath>\n")
                    .append("    </defs>\n");

                sb.append("    <rect x=\"")
                    .append(sx - sPadding)
                        .append("\" y=\"")
                    .append(sy - sPadding)
                        .append("\" width=\"")
                    .append(sLogoSize + sPadding * 2)
                        .append("\" height=\"")
                    .append(sLogoSize + sPadding * 2)
                        .append("\" rx=\"")
                    .append(sCornerRadius + (2 * moduleScale))
                        .append("\" ry=\"")
                    .append(sCornerRadius + (2 * moduleScale))
                        .append("\" fill=\"#FFFFFF\"/>")
                        .append("\n");

                sb.append("    <image x=\"")
                    .append(sx)
                        .append("\" y=\"")
                    .append(sy)
                        .append("\" width=\"")
                    .append(sLogoSize)
                        .append("\" height=\"")
                    .append(sLogoSize)
                        .append("\" clip-path=\"url(#logoClip)\" preserveAspectRatio=\"xMidYMid meet\" href=\"")
                        .append(dataUri)
                        .append("\"/>")
                        .append("\n");
            }
        }

        sb.append("  </g>\n</svg>\n");
        return sb.toString();
    }

    private static String toPngDataUri(File logoFile) throws Exception {
        BufferedImage logo = ImageIO.read(logoFile);
        if (logo == null) {
            throw new IllegalArgumentException("Logo-Datei konnte nicht gelesen werden.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(logo, "png", baos);
        String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        return "data:image/png;base64," + b64;
    }

    private static String toHex(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private static String trimDouble(double d) {
        String s = String.format("%.4f", d);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private String getAppVersion() {
        String implVersion = QrGeneratorApp.class.getPackage().getImplementationVersion();
        return (implVersion == null || implVersion.isBlank()) ? APP_FALLBACK_VERSION : implVersion;
    }

    // Info-Dialog für About-Menü
    private void showAboutDialog() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Über QR Generator");
        a.setHeaderText(APP_NAME + " " + getAppVersion());
        a.setContentText("Einfacher, datenschutzfreundlicher QR-Code Generator\n\nErstellt von MF\nLizenz: MIT\n\nGitHub: github.com/MF-swiss/QR-Code_Generator");
        a.showAndWait();
    }

    private String buildContent() {
        ContentType t = typeBox.getValue();
        return switch (t) {
            case YOUTUBE -> validateYouTubeUrl(requireNotBlank(tfUrl.getText(), "Bitte YouTube-Link eingeben."));
            case WEBSEITE -> requireNotBlank(tfUrl.getText(), "Bitte URL eingeben.");
            case TEXT -> requireNotBlank(taText.getText(), "Bitte Text eingeben.");
            case STANDORT -> {
                if (rbAddress.isSelected()) {
                    String address = requireNotBlank(tfAddress.getText(), "Bitte Adresse eingeben.");
                    yield "https://www.google.com/maps/search/?api=1&query="
                            + URLEncoder.encode(address, StandardCharsets.UTF_8);
                }

                double lat = parseDouble(requireNotBlank(tfLat.getText(), "Bitte Latitude eingeben."), "Latitude ist ungültig.");
                double lon = parseDouble(requireNotBlank(tfLon.getText(), "Bitte Longitude eingeben."), "Longitude ist ungültig.");
                yield "https://maps.google.com/?q=" + lat + "," + lon;
            }
            case WLAN_LOGIN -> {
                String ssid = requireNotBlank(tfSsid.getText(), "Bitte SSID eingeben.");
                String pass = cbAuth.getValue().equals("nopass") ? "" :
                        requireNotBlank(pfWifi.getText(), "Bitte WLAN-Passwort eingeben.");
                String auth = cbAuth.getValue();
                yield "WIFI:T:" + auth + ";S:" + escapeWifi(ssid) + ";P:" + escapeWifi(pass) + ";;";
            }
        };
    }

    private static String validateYouTubeUrl(String url) {
        String u = url.trim().toLowerCase();
        boolean ok = u.startsWith("https://www.youtube.com/")
                || u.startsWith("https://youtube.com/")
                || u.startsWith("https://m.youtube.com/")
                || u.startsWith("https://youtu.be/");
        if (!ok) {
            throw new IllegalArgumentException("Bitte einen gültigen YouTube-Link verwenden.");
        }
        return url.trim();
    }

    private static boolean isYouTubeUrl(String url) {
        try {
            validateYouTubeUrl(url);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static boolean isHttpUrl(String url) {
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean isLatitude(String v) {
        try {
            double lat = parseDouble(v, "Latitude ist ungültig.");
            return lat >= -90 && lat <= 90;
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean isLongitude(String v) {
        try {
            double lon = parseDouble(v, "Longitude ist ungültig.");
            return lon >= -180 && lon <= 180;
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private static String value(String v) {
        return v == null ? "" : v.trim();
    }

    private static BufferedImage generateQrBuffered(
            String content,
            int size,
            java.awt.Color qrColor,
            java.awt.Color bgColor,
            String logoPath
    ) throws Exception {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage qr = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = qr.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(bgColor);
        g.fillRect(0, 0, size, size);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (matrix.get(x, y)) {
                    qr.setRGB(x, y, qrColor.getRGB());
                }
            }
        }

        if (logoPath != null && !logoPath.isBlank()) {
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                BufferedImage logo = ImageIO.read(logoFile);

                int logoSize = size / 5;
                int lx = (size - logoSize) / 2;
                int ly = (size - logoSize) / 2;

                int padding = 12;
                Shape bg = new RoundRectangle2D.Float(
                        lx - padding, ly - padding,
                        logoSize + padding * 2f, logoSize + padding * 2f,
                        22, 22
                );
                g.setColor(java.awt.Color.WHITE);
                g.fill(bg);

                Image scaled = logo.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);
                g.drawImage(scaled, lx, ly, null);
            }
        }

        g.dispose();
        return qr;
    }

    private static String requireNotBlank(String v, String msg) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
        return v.trim();
    }

    private static double parseDouble(String v, String msg) {
        try {
            return Double.parseDouble(v.trim().replace(",", "."));
        } catch (Exception e) {
            throw new IllegalArgumentException(msg);
        }
    }

    private static String escapeWifi(String value) {
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace(":", "\\:");
    }

    private static java.awt.Color fxToAwt(Color c) {
        return new java.awt.Color(
                (float) c.getRed(),
                (float) c.getGreen(),
                (float) c.getBlue(),
                (float) c.getOpacity()
        );
    }

    private static WritableImage toFxImage(BufferedImage bimg) {
        WritableImage wr = new WritableImage(bimg.getWidth(), bimg.getHeight());
        javafx.scene.image.PixelWriter pw = wr.getPixelWriter();
        for (int y = 0; y < bimg.getHeight(); y++) {
            for (int x = 0; x < bimg.getWidth(); x++) {
                int argb = bimg.getRGB(x, y);
                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;
                pw.setColor(x, y, Color.rgb(r, g, b, a / 255.0));
            }
        }
        return wr;
    }

    private static void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
