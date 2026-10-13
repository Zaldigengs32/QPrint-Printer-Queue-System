package com.example.qprintproject.Controller;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TampilanController {
    public static VBox createEmptyQueueLabel() {
        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setStyle("-fx-padding: 40; " +
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12;");

        Label iconLabel = new Label("📋");
        iconLabel.setStyle("-fx-font-size: 48; -fx-text-fill: #6c757d;");
        Label messageLabel = new Label("Antrian Kosong");
        messageLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        messageLabel.setStyle("-fx-text-fill: #495057;");
        Label subMessageLabel = new Label("Belum ada dokumen dalam antrian cetak");
        subMessageLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14;");
        emptyBox.getChildren().addAll(iconLabel, messageLabel, subMessageLabel);
        return emptyBox;
    }

    public static VBox createEmptyHistoryLabel() {
        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setStyle("-fx-padding: 40; " +
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12;");

        Label iconLabel = new Label("📜");
        iconLabel.setStyle("-fx-font-size: 48; -fx-text-fill: #6c757d;");
        Label messageLabel = new Label("Riwayat Kosong");
        messageLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        messageLabel.setStyle("-fx-text-fill: #495057;");
        Label subMessageLabel = new Label("Belum ada dokumen yang selesai dicetak");
        subMessageLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14;");
        emptyBox.getChildren().addAll(iconLabel, messageLabel, subMessageLabel);
        return emptyBox;
    }
}