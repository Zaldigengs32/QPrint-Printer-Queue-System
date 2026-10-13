package com.example.qprintproject.Controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;

public class ThankYouController implements Initializable {

    @FXML private Label thankYouLabel;
    @FXML private Label messageLabel;
    @FXML private Label subMessageLabel;
    @FXML private Label pagesCountLabel;
    @FXML private Label timeLabel;
    @FXML private Label qualityLabel;
    @FXML private Button closeButton;

    // Nilai default diubah untuk mencerminkan status "tidak ada cetakan"
    private int totalPages = 0;
    private String printTime = "00:00";
    private String printQuality = "---";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::enableFullScreen);
        startEntranceAnimations();
        setupButtonEffects();
        updateDisplayData(); // Metode ini akan menampilkan nilai default yang baru
    }

    private void enableFullScreen() {
        try {
            // Cari stage dari salah satu node yang ada
            Stage stage = null;
            if (thankYouLabel != null) {
                stage = (Stage) thankYouLabel.getScene().getWindow();
            }

            if (stage != null) {
                stage.setResizable(true);
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setTitle("QPrint - Pencetakan Selesai");
                javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                if (!stage.isFullScreen()) {
                    stage.setX(bounds.getMinX());
                    stage.setY(bounds.getMinY());
                    stage.setWidth(bounds.getWidth());
                    stage.setHeight(bounds.getHeight());
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting full screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setData(int pages, String time, String quality) {
        this.totalPages = pages;
        this.printTime = time;
        this.printQuality = quality;
        updateDisplayData();
    }

    private void updateDisplayData() {

        Platform.runLater(() -> {
            if (pagesCountLabel != null) {
                pagesCountLabel.setText(String.valueOf(totalPages));
            }
            if (timeLabel != null) {
                timeLabel.setText(printTime);
            }
            if (qualityLabel != null) {
                qualityLabel.setText(printQuality);
            }
        });
    }

    private void startEntranceAnimations() {
        if (thankYouLabel != null) {
            FadeTransition fadeThankYou = new FadeTransition(Duration.millis(1000), thankYouLabel);
            fadeThankYou.setFromValue(0.0);
            fadeThankYou.setToValue(1.0);
            fadeThankYou.play();

            ScaleTransition scaleThankYou = new ScaleTransition(Duration.millis(800), thankYouLabel);
            scaleThankYou.setFromX(0.5);
            scaleThankYou.setFromY(0.5);
            scaleThankYou.setToX(1.0);
            scaleThankYou.setToY(1.0);
            scaleThankYou.play();
        }

        if (messageLabel != null) {
            FadeTransition fadeMessage = new FadeTransition(Duration.millis(800), messageLabel);
            fadeMessage.setFromValue(0.0);
            fadeMessage.setToValue(1.0);
            fadeMessage.setDelay(Duration.millis(500));
            fadeMessage.play();
        }

        if (subMessageLabel != null) {
            FadeTransition fadeSubMessage = new FadeTransition(Duration.millis(800), subMessageLabel);
            fadeSubMessage.setFromValue(0.0);
            fadeSubMessage.setToValue(1.0);
            fadeSubMessage.setDelay(Duration.millis(800));
            fadeSubMessage.play();
        }

        if (closeButton != null) {
            FadeTransition fadeCloseBtn = new FadeTransition(Duration.millis(600), closeButton);
            fadeCloseBtn.setFromValue(0.0);
            fadeCloseBtn.setToValue(1.0);
            fadeCloseBtn.setDelay(Duration.millis(1400));
            fadeCloseBtn.play();
        }
    }

    private void setupButtonEffects() {
        // Efek hover untuk tombol Close
        if (closeButton != null) {
            closeButton.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), closeButton);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.play();
            });

            closeButton.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), closeButton);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        // Animasi fade out sebelum menutup
        Node source = (Node) event.getSource();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), source.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setFullScreen(false);
            stage.close();
        });
        fadeOut.play();
    }
}