package com.example.qprintproject.Controller;

import com.example.qprintproject.DBConnect.DatabaseService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LoadingController implements Initializable {

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Label percentageLabel;
    @FXML private Label detailLabel;
    @FXML private Label speedLabel;
    @FXML private Label timestampLabel;
    @FXML private Text mainTitle;
    @FXML private Rectangle backgroundRect;
    @FXML private Circle loadingDot1;
    @FXML private Circle loadingDot2;
    @FXML private Circle loadingDot3;

    private DatabaseService databaseService;
    private ParallelTransition dotAnimation;
    private Timeline backgroundAnimation;
    private MediaPlayer startupMediaPlayer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("LoadingController initialized");
        playStartupMusic();

        // Mengatur Waktu Kapan Login (Tanggal dan Jam)
        if (timestampLabel != null) {
            timestampLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        setupAnimations();
        startLoadingProcess();
    }

    private void setupAnimations() {
        setupDotAnimation();
        setupBackgroundAnimation();
        setupTitleAnimation();
    }

    private void setupDotAnimation() {
        if (loadingDot1 == null || loadingDot2 == null || loadingDot3 == null) {
            System.out.println("Warning: Loading dots not found in FXML");
            return;
        }

        ScaleTransition dot1Scale = new ScaleTransition(Duration.seconds(0.8), loadingDot1);
        dot1Scale.setFromX(0.8);
        dot1Scale.setFromY(0.8);
        dot1Scale.setToX(1.2);
        dot1Scale.setToY(1.2);
        dot1Scale.setAutoReverse(true);
        dot1Scale.setCycleCount(Timeline.INDEFINITE);

        ScaleTransition dot2Scale = new ScaleTransition(Duration.seconds(0.8), loadingDot2);
        dot2Scale.setFromX(0.8);
        dot2Scale.setFromY(0.8);
        dot2Scale.setToX(1.2);
        dot2Scale.setToY(1.2);
        dot2Scale.setAutoReverse(true);
        dot2Scale.setCycleCount(Timeline.INDEFINITE);
        dot2Scale.setDelay(Duration.seconds(0.2));

        ScaleTransition dot3Scale = new ScaleTransition(Duration.seconds(0.8), loadingDot3);
        dot3Scale.setFromX(0.8);
        dot3Scale.setFromY(0.8);
        dot3Scale.setToX(1.2);
        dot3Scale.setToY(1.2);
        dot3Scale.setAutoReverse(true);
        dot3Scale.setCycleCount(Timeline.INDEFINITE);
        dot3Scale.setDelay(Duration.seconds(0.4));

        FadeTransition dot1Fade = new FadeTransition(Duration.seconds(1.0), loadingDot1);
        dot1Fade.setFromValue(0.5);
        dot1Fade.setToValue(1.0);
        dot1Fade.setAutoReverse(true);
        dot1Fade.setCycleCount(Timeline.INDEFINITE);

        FadeTransition dot2Fade = new FadeTransition(Duration.seconds(1.0), loadingDot2);
        dot2Fade.setFromValue(0.5);
        dot2Fade.setToValue(1.0);
        dot2Fade.setAutoReverse(true);
        dot2Fade.setCycleCount(Timeline.INDEFINITE);
        dot2Fade.setDelay(Duration.seconds(0.3));

        FadeTransition dot3Fade = new FadeTransition(Duration.seconds(1.0), loadingDot3);
        dot3Fade.setFromValue(0.5);
        dot3Fade.setToValue(1.0);
        dot3Fade.setAutoReverse(true);
        dot3Fade.setCycleCount(Timeline.INDEFINITE);
        dot3Fade.setDelay(Duration.seconds(0.6));

        dotAnimation = new ParallelTransition(
                dot1Scale, dot2Scale, dot3Scale,
                dot1Fade, dot2Fade, dot3Fade
        );
        dotAnimation.play();
    }

    private void playStartupMusic() {
        try {
            URL musicFile = getClass().getResource("/audio/BackgroundAudio.mp3");
            if (musicFile != null) {
                Media media = new Media(musicFile.toString());
                startupMediaPlayer = new MediaPlayer(media);
                startupMediaPlayer.setVolume(0.5);
                startupMediaPlayer.setOnEndOfMedia(() -> {
                    startupMediaPlayer.dispose();
                });
                startupMediaPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("Error playing startup music: " + e.getMessage());
        }
    }

    private void setupBackgroundAnimation() {
        if (backgroundRect == null) {
            System.out.println("Warning: Background rectangle not found in FXML");
            return;
        }

        backgroundAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(backgroundRect.opacityProperty(), 0.9)
                ),
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(backgroundRect.opacityProperty(), 1.0)
                ),
                new KeyFrame(Duration.seconds(6),
                        new KeyValue(backgroundRect.opacityProperty(), 0.9)
                )
        );
        backgroundAnimation.setCycleCount(Timeline.INDEFINITE);
        backgroundAnimation.play();
    }

    private void setupTitleAnimation() {
        if (mainTitle == null) {
            System.out.println("Warning: Main title not found in FXML");
            return;
        }

        ScaleTransition titlePulse = new ScaleTransition(Duration.seconds(2), mainTitle);
        titlePulse.setFromX(1.0);
        titlePulse.setFromY(1.0);
        titlePulse.setToX(1.02);
        titlePulse.setToY(1.02);
        titlePulse.setAutoReverse(true);
        titlePulse.setCycleCount(Timeline.INDEFINITE);
        titlePulse.play();
    }

    private void startLoadingProcess() {
        System.out.println("Starting loading process...");
        Task<Void> loadingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Phase 1: Initialize services
                    updateProgress(0.0, 1.0);
                    updateMessage("Menginisialisasi layanan sistem...");
                    Platform.runLater(() -> {
                        if (detailLabel != null) detailLabel.setText("Memuat konfigurasi awal...");
                        if (speedLabel != null) speedLabel.setText("Starting up...");
                    });
                    Thread.sleep(1200);

                    updateProgress(0.15, 1.0);
                    updateMessage("Memeriksa dependensi...");
                    Platform.runLater(() -> {
                        if (detailLabel != null) detailLabel.setText("Validasi komponen sistem...");
                        if (speedLabel != null) speedLabel.setText("Checking dependencies...");
                    });
                    Thread.sleep(800);

                    // Phase 2: Database connection
                    updateProgress(0.3, 1.0);
                    updateMessage("Menghubungkan ke database...");
                    Platform.runLater(() -> {
                        if (detailLabel != null) detailLabel.setText("Establishing connection...");
                        if (speedLabel != null) speedLabel.setText("Connecting to DB...");
                    });

                    try {
                        databaseService = new DatabaseService();
                        Thread.sleep(1000);

                        if (databaseService.isConnected()) {
                            updateProgress(0.5, 1.0);
                            updateMessage("Koneksi database berhasil!");
                            Platform.runLater(() -> {
                                if (detailLabel != null) detailLabel.setText("Database siap digunakan");
                                if (speedLabel != null) speedLabel.setText("DB Connected");
                            });
                        } else {
                            updateMessage("Koneksi database gagal!");
                            Platform.runLater(() -> {
                                if (detailLabel != null) detailLabel.setText("Error: Tidak dapat terhubung ke database");
                                if (speedLabel != null) speedLabel.setText("DB Error");
                            });
                            throw new RuntimeException("Failed to connect to database.");
                        }
                    } catch (Exception e) {
                        System.err.println("Database connection error: " + e.getMessage());
                        Platform.runLater(() -> {
                            if (statusLabel != null) statusLabel.setText("Error koneksi database");
                            if (detailLabel != null) detailLabel.setText("Error: " + e.getMessage());
                            if (speedLabel != null) speedLabel.setText("Connection Failed");
                        });
                        throw e;
                    }
                    Thread.sleep(800);

                    // Phase 3: Load configurations
                    updateProgress(0.65, 1.0);
                    updateMessage("Memuat konfigurasi aplikasi...");
                    Platform.runLater(() -> {
                        if (detailLabel != null) detailLabel.setText("Mengkonfigurasi antarmuka...");
                        if (speedLabel != null) speedLabel.setText("Loading config...");
                    });
                    Thread.sleep(1000);

                    updateProgress(0.8, 1.0);
                    updateMessage("Menyiapkan antarmuka pengguna...");
                    Platform.runLater(() -> {
                        if (detailLabel != null) detailLabel.setText("Loading UI components...");
                        if (speedLabel != null) speedLabel.setText("Preparing UI...");
                    });
                    Thread.sleep(800);

                    // Phase 4: Final preparations
                    updateProgress(0.95, 1.0);
                    updateMessage("Finalisasi sistem...");
                    Platform.runLater(() -> {
                        if (detailLabel != null) detailLabel.setText("Hampir selesai...");
                        if (speedLabel != null) speedLabel.setText("Finalizing...");
                    });
                    Thread.sleep(600);

                    updateProgress(1.0, 1.0);
                    updateMessage("Sistem siap digunakan!");
                    Platform.runLater(() -> {
                        if (detailLabel != null) detailLabel.setText("Loading completed successfully");
                        if (speedLabel != null) speedLabel.setText("Ready!");
                    });
                    Thread.sleep(800);

                    System.out.println("Loading task completed successfully!");
                    return null;

                } catch (Exception e) {
                    System.err.println("Error in loading task: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        // Progress Bar dan Persentase Loading
        if (progressBar != null) {
            progressBar.progressProperty().bind(loadingTask.progressProperty());
        }

        loadingTask.progressProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                int percentage = (int) Math.round(newValue.doubleValue() * 100);
                if (percentageLabel != null) {
                    percentageLabel.setText(percentage + "%");
                }

                if (progressBar != null) {
                    if (percentage >= 100) {
                        progressBar.setStyle("-fx-accent: linear-gradient(to right, #27ae60, #2ecc71); -fx-background-color: transparent; -fx-background-radius: 8;");
                    } else if (percentage >= 75) {
                        progressBar.setStyle("-fx-accent: linear-gradient(to right, #3498db, #2ecc71); -fx-background-color: transparent; -fx-background-radius: 8;");
                    } else {
                        progressBar.setStyle("-fx-accent: linear-gradient(to right, #3498db, #5dade2, #85c1e9, #aed6f1); -fx-background-color: transparent; -fx-background-radius: 8;");
                    }
                }
            });
        });

        if (statusLabel != null) {
            statusLabel.textProperty().bind(loadingTask.messageProperty());
        }

        loadingTask.setOnSucceeded(successEvent -> {
            System.out.println("Loading succeeded, transitioning to login...");

            if (dotAnimation != null) {
                dotAnimation.stop();
            }
            if (backgroundAnimation != null) {
                backgroundAnimation.stop();
            }

            if (statusLabel != null) {
                ScaleTransition successScale = new ScaleTransition(Duration.seconds(0.3), statusLabel);
                successScale.setToX(1.1);
                successScale.setToY(1.1);
                successScale.setAutoReverse(true);
                successScale.setCycleCount(2);

                successScale.setOnFinished(finishEvent -> {
                    loadLoginPage();
                });
                successScale.play();
            } else {
                loadLoginPage();
            }
        });

        loadingTask.setOnFailed(failEvent -> {
            System.err.println("Loading task failed!");

            if (dotAnimation != null) {
                dotAnimation.stop();
            }
            if (backgroundAnimation != null) {
                backgroundAnimation.stop();
            }

            Throwable e = loadingTask.getException();
            if (e != null) {
                System.err.println("Loading task failed: " + e.getMessage());
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText("Gagal memuat aplikasi");
                }
                if (detailLabel != null) {
                    detailLabel.setText("Error: " + (e != null ? e.getMessage() : "Unknown error"));
                }
                if (percentageLabel != null) {
                    percentageLabel.setText("Error!");
                }
                if (speedLabel != null) {
                    speedLabel.setText("Failed");
                }

                if (statusLabel != null) {
                    Timeline errorBlink = new Timeline(
                            new KeyFrame(Duration.seconds(0.5),
                                    new KeyValue(statusLabel.textFillProperty(), Color.RED)
                            ),
                            new KeyFrame(Duration.seconds(1.0),
                                    new KeyValue(statusLabel.textFillProperty(), Color.WHITE)
                            )
                    );
                    errorBlink.setCycleCount(3);
                    errorBlink.play();
                }
            });
        });

        Thread loadingThread = new Thread(loadingTask);
        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    // Setelah Loading maka akan Membuka Login Page FXML nya
    private void loadLoginPage() {
        try {
            System.out.println("Loading login page...");
            Stage stage = (Stage) progressBar.getScene().getWindow();
            String[] possiblePaths = {"/com/example/qprintproject/FXMLProgram/Login.fxml", "/com/example/qprintproject/Login.fxml", "/Login.fxml", "Login.fxml"
            };

            URL loginFxmlUrl = null;
            for (String path : possiblePaths) {
                loginFxmlUrl = getClass().getResource(path);
                if (loginFxmlUrl != null) {
                    System.out.println("Found Login.fxml at: " + path);
                    break;
                }
            }

            if (loginFxmlUrl == null) {
                System.err.println("Login.fxml not found in any of the expected paths!");
                Platform.runLater(() -> {
                    if (statusLabel != null) statusLabel.setText("Error: Login.fxml tidak ditemukan");
                    if (detailLabel != null) detailLabel.setText("Periksa path file FXML");
                });
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(loginFxmlUrl);
            Scene scene = new Scene(fxmlLoader.load(), 1500, 900);

            System.out.println("Login scene created successfully");

            // Transisi dari Loading ke Login Page
            if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.0), stage.getScene().getRoot());
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(fadeEvent -> {
                    stage.setScene(scene);
                    stage.setTitle("SISTEM ANTRIAN PRINTER - Login");
                    stage.setFullScreen(true);
                    stage.setResizable(false);

                    FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), scene.getRoot());
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();

                    stage.show();
                    System.out.println("Login page loaded successfully!");
                });
                fadeOut.play();
            } else {
                stage.setScene(scene);
                stage.setTitle("SISTEM ANTRIAN PRINTER - Login");
                stage.setFullScreen(true);
                stage.setResizable(false);
                stage.show();
                System.out.println("Login page loaded successfully (direct)!");
            }

        } catch (IOException ex) {
            System.err.println("IOException loading login page: " + ex.getMessage());
            ex.printStackTrace();

            Platform.runLater(() -> {
                if (statusLabel != null) statusLabel.setText("Error: Gagal memuat halaman login");
                if (detailLabel != null) detailLabel.setText("IOException: " + ex.getMessage());
            });

        } catch (Exception ex) {
            System.err.println("Unexpected error loading login page: " + ex.getMessage());
            ex.printStackTrace();

            Platform.runLater(() -> {
                if (statusLabel != null) statusLabel.setText("Error: " + ex.getClass().getSimpleName());
                if (detailLabel != null) detailLabel.setText(ex.getMessage());
            });
        }
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }
}