package com.example.qprintproject.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordVisibilityButton;
    @FXML private Button exitButton;
    @FXML private Button loginButton;
    @FXML private Text eyeIcon;
    @FXML private Label errorMessageLabel;

    private static final String VALID_USERNAME = "QPrintProject";
    private static final String VALID_PASSWORD = "Kelompok3";
    private boolean passwordIsVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal && !newVal && passwordIsVisible) {
                passwordField.setText(passwordVisibleField.getText());
            }
        });

        passwordVisibleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal && !newVal && !passwordIsVisible) {
                passwordVisibleField.setText(passwordField.getText());
            }
        });

        addHoverEffects();
        addEnterKeySupport();
    }

    private void addHoverEffects() {
        // Animasi Hover untuk Tombol Login
        if (loginButton != null) {
            loginButton.setOnMouseEntered(e -> {
                loginButton.setStyle("-fx-background-color: linear-gradient(to right, #198350, #21bd71); -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);");
            });

            loginButton.setOnMouseExited(e -> {
                loginButton.setStyle("-fx-background-color: linear-gradient(to right, #1e6091, #266ba3); -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
            });
        }

        // Animasi Hover untuk Tombol Keluar
        if (exitButton != null) {
            exitButton.setOnMouseEntered(e -> {
                exitButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-font-size: 14px; -fx-border-color: #e74c3c; -fx-border-radius: 10; " +
                        "-fx-background-radius: 10; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
            });

            exitButton.setOnMouseExited(e -> {
                exitButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-weight: bold; " +
                        "-fx-font-size: 14px; -fx-border-color: #e74c3c; -fx-border-radius: 10; " +
                        "-fx-background-radius: 10; -fx-cursor: hand;");
            });
        }
    }

    private void addEnterKeySupport() {
        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
        passwordVisibleField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordIsVisible = !passwordIsVisible;
        if (passwordIsVisible) {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            eyeIcon.setText("\uD83D\uDE48");

            Platform.runLater(() -> {
                passwordVisibleField.requestFocus();
                passwordVisibleField.positionCaret(passwordVisibleField.getText().length());
            });
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            eyeIcon.setText("👁");

            Platform.runLater(() -> {
                passwordField.requestFocus();
                passwordField.positionCaret(passwordField.getText().length());
            });
        }
    }

    @FXML
    private void handleLogin() {
        errorMessageLabel.setText("");

        String password = passwordIsVisible ? passwordVisibleField.getText() : passwordField.getText();
        String username = usernameField.getText().trim();

        // Validasi Inputan Login
        if (username.isEmpty()) {
            errorMessageLabel.setText("Username tidak boleh kosong!");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            errorMessageLabel.setText("Password tidak boleh kosong!");
            if (passwordIsVisible) {
                passwordVisibleField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
            return;
        }
        if (username.equals(VALID_USERNAME) && password.equals(VALID_PASSWORD)) {
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/qprintproject/FXMLProgram/TampilanUtamaPrinter.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                stage.setScene(scene);
                stage.setTitle("Loading...");
                stage.setFullScreen(true);
                stage.setResizable(false);
            } catch (IOException e) {
                errorMessageLabel.setText("Error loading application: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            errorMessageLabel.setText("Username atau password salah!");
            passwordField.clear();
            passwordVisibleField.clear();
            usernameField.requestFocus();
        }
    }

    @FXML
    private void handleExit() {
        // Konfirmasi Tombol Keluar
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Keluar");
        alert.setHeaderText("Keluar dari Aplikasi");
        alert.setContentText("Apakah Anda yakin ingin keluar dari aplikasi?");

        Stage currentStage = (Stage) usernameField.getScene().getWindow();
        alert.initOwner(currentStage);

        ButtonType buttonTypeYes = new ButtonType("Ya");
        ButtonType buttonTypeNo = new ButtonType("Tidak");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            Platform.exit();
            System.exit(0);
        }
    }
}