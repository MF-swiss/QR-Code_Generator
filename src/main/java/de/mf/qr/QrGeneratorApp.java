package de.mf.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class QrGeneratorApp extends Application {

    private enum ContentType {
        YOUTUBE, WEBSEITE, TEXT, WLAN_LOGIN, STANDORT
    }

    private final ComboBox<ContentType> typeBox = new ComboBox<>();
    private final TextField tfUrl = new TextField();
    private final TextArea taText = new TextArea();
    private final TextField tfLat = new TextField();
    private final TextField tfLon = new TextField();
    private final TextField tfSsid = new TextField();
    private final PasswordField pfWifi = new PasswordField();
    private final ComboBox<String> cbAuth = new ComboBox<>();

    private final ColorPicker cpQr = new ColorPicker(Color.web("#1E6BFF"));
    private final ColorPicker cpBg = new ColorPicker(Color.web("#F8FAFC"));

    private final CheckBox cbLogo = new CheckBox("Logo in der Mitte");
    private final Label lblLogoPath = new Label("Kein Logo ausgewählt");
    private File selectedLogo;

    private final Label lblUrl = new Label("URL:");
    private final Label lblText = new Label("Text:");
    private final Label lblLat = new Label("Latitude:");
    private final Label lblLon = new Label("Longitude:");
    private final Label lblSsid = new Label("SSID:");
    private final Label lblPass = new Label("Passwort:");
    private final Label lblAuth = new Label("Auth:");

    private final ImageView preview = new ImageView();

    @Override
    public void start(Stage stage) {
        stage.setTitle("QR-Code Generator");

        typeBox.getItems().addAll(ContentType.values());
        typeBox.setValue(ContentType.YOUTUBE);

        tfUrl.setPromptText("https://www.youtube.com/watch?v=...");
        taText.setPromptText("Dein Text...");
        taText.setPrefRowCount(3);
        tfLat.setPromptText("z. B. 47.3769");
        tfLon.setPromptText("z. B. 8.5417");
        tfSsid.setPromptText("WLAN-Name (SSID)");
        pfWifi.setPromptText("WLAN-Passwort");
        cbAuth.getItems().addAll("WPA", "WEP", "nopass");
        cbAuth.setValue("WPA");

        Button btnPickLogo = new Button("Logo auswählen");
        btnPickLogo.setOnAction(e -> pickLogo(stage));

        Button btnPreview = new Button("Vorschau");
        btnPreview.setOnAction(e -> generatePreview());

        Button btnSave = new Button("QR-Code generieren & speichern");
        btnSave.setOnAction(e -> saveQr(stage));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(12));

        int r = 0;
        form.add(new Label("Typ:"), 0, r);
        form.add(typeBox, 1, r++);

        form.add(lblUrl, 0, r);
        form.add(tfUrl, 1, r++);

        form.add(lblText, 0, r);
        form.add(taText, 1, r++);

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
        form.add(cpBg, 1, r++);

        HBox logoRow = new HBox(10, cbLogo, btnPickLogo, lblLogoPath);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        form.add(new Label("Logo:"), 0, r);
        form.add(logoRow, 1, r++);

        HBox actionRow = new HBox(10, btnPreview, btnSave);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        preview.setFitWidth(280);
        preview.setFitHeight(280);
        preview.setPreserveRatio(true);

        VBox root = new VBox(10, form, actionRow, new Label("Vorschau:"), preview);
        root.setPadding(new Insets(10));

        typeBox.valueProperty().addListener((obs, oldV, newV) -> updateFieldVisibility());
        updateFieldVisibility();

        Scene scene = new Scene(root, 780, 840);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void updateFieldVisibility() {
        ContentType t = typeBox.getValue();

        boolean showUrl = t == ContentType.YOUTUBE || t == ContentType.WEBSEITE;
        boolean showText = t == ContentType.TEXT;
        boolean showStandort = t == ContentType.STANDORT;
        boolean showWlan = t == ContentType.WLAN_LOGIN;

        setRowVisible(lblUrl, tfUrl, showUrl);
        setRowVisible(lblText, taText, showText);
        setRowVisible(lblLat, tfLat, showStandort);
        setRowVisible(lblLon, tfLon, showStandort);
        setRowVisible(lblSsid, tfSsid, showWlan);
        setRowVisible(lblPass, pfWifi, showWlan);
        setRowVisible(lblAuth, cbAuth, showWlan);

        if (t == ContentType.YOUTUBE) {
            tfUrl.setPromptText("https://www.youtube.com/watch?v=...");
        } else if (t == ContentType.WEBSEITE) {
            tfUrl.setPromptText("https://deine-webseite.de");
        }
    }

    private static void setRowVisible(Control label, Control input, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        input.setVisible(visible);
        input.setManaged(visible);
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
        }
    }

    private void generatePreview() {
        try {
            String content = buildContent();
            BufferedImage img = generateQrBuffered(
                    content,
                    900,
                    fxToAwt(cpQr.getValue()),
                    fxToAwt(cpBg.getValue()),
                    (cbLogo.isSelected() && selectedLogo != null) ? selectedLogo.getAbsolutePath() : null
            );
            preview.setImage(toFxImage(img));
        } catch (Exception ex) {
            showError("Fehler bei Vorschau", ex.getMessage());
        }
    }

    private void saveQr(Stage stage) {
        try {
            String content = buildContent();
            FileChooser fc = new FileChooser();
            fc.setTitle("QR-Code speichern");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Datei", "*.png"));
            fc.setInitialFileName("qr_code.png");
            File out = fc.showSaveDialog(stage);
            if (out == null) return;

            BufferedImage img = generateQrBuffered(
                    content,
                    1000,
                    fxToAwt(cpQr.getValue()),
                    fxToAwt(cpBg.getValue()),
                    (cbLogo.isSelected() && selectedLogo != null) ? selectedLogo.getAbsolutePath() : null
            );

            ImageIO.write(img, "png", out);
            showInfo("Fertig", "QR-Code gespeichert:\n" + out.getAbsolutePath());
        } catch (Exception ex) {
            showError("Fehler beim Speichern", ex.getMessage());
        }
    }

    private String buildContent() {
        ContentType t = typeBox.getValue();
        return switch (t) {
            case YOUTUBE -> validateYouTubeUrl(requireNotBlank(tfUrl.getText(), "Bitte YouTube-Link eingeben."));
            case WEBSEITE -> requireNotBlank(tfUrl.getText(), "Bitte URL eingeben.");
            case TEXT -> requireNotBlank(taText.getText(), "Bitte Text eingeben.");
            case STANDORT -> {
                double lat = parseDouble(tfLat.getText(), "Latitude ist ungültig.");
                double lon = parseDouble(tfLon.getText(), "Longitude ist ungültig.");
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
