package com.example.qprintproject.Controller;

import com.example.qprintproject.DBConnect.DatabaseService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import net.sf.jasperreports.engine.JRException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MainProgramController implements Initializable, QueueController.PrintQueueCallback {

    @FXML private TextField docNameField;
    @FXML private ComboBox<String> docTypeCombo;
    @FXML private Spinner<Integer> copiesSpinner;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private TextArea notesArea;
    @FXML private Button selectFileButton;
    @FXML private Button addDocumentBtn;
    @FXML private Button openDocumentButton;
    @FXML private Button closeButton;

    @FXML private Label printerStatusLabel;
    @FXML private VBox currentJobBox;
    @FXML private Label currentJobLabel;
    @FXML private VBox queueContainer;
    @FXML private Button startPrintButton;
    @FXML private Button cancelSelectedQueueButton;

    @FXML private Label totalDocsLabel;
    @FXML private Label completedDocsLabel;
    @FXML private Label waitingDocsLabel;
    @FXML private Label totalPagesLabel;
    @FXML private Label monitorPrinterStatusLabel;
    @FXML private Label queueCountLabel;
    @FXML private Label estimatedWaitLabel;
    @FXML private Label urgentCountLabel;
    @FXML private Label highPriorityCountLabel;
    @FXML private Label normalPriorityCountLabel;
    @FXML private Label lowPriorityCountLabel;

    @FXML private VBox historyContainer;
    @FXML private Button clearHistoryBtn;
    @FXML private Button viewHistoryBtn;

    private File selectedFile;
    private Timeline statusUpdateTimer;
    private VBox selectedJobCard = null;
    private MulaiCetakController selectedJob = null;

    private QueueController queueController;
    private DatabaseService databaseService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.queueController = new QueueController(this);
            this.databaseService = new DatabaseService();
            this.queueController.setDatabaseService(databaseService);

            initializeComponents();
            startStatusUpdateTimer();
            updateAllDisplays();
        } catch (Exception e) {
            showErrorWithStackTrace("Gagal melakukan inisialisasi aplikasi.", e);
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        docTypeCombo.getItems().addAll("PDF", "DOCX", "XLSX");
        priorityCombo.getItems().addAll("Rendah", "Normal", "Tinggi", "Urgent");
        priorityCombo.setValue("Normal");

        copiesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        updatePrinterStatus("✅ Siap", Color.GREEN);
        currentJobBox.setVisible(false);
        openDocumentButton.setDisable(true);
        cancelSelectedQueueButton.setDisable(true);

        docNameField.setDisable(false);
        docTypeCombo.setDisable(false);
    }

    @FXML
    private void selectFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Dokumen");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                    new FileChooser.ExtensionFilter("Word Documents", "*.docx"),
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );

            Stage stage = (Stage) selectFileButton.getScene().getWindow();
            selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                updateFileSelection();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memilih file: " + e.getMessage());
        }
    }


    private void updateFileSelection() {
        String fileName = selectedFile.getName();

        selectFileButton.setText("📄 " + fileName);
        selectFileButton.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 15 30; -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, rgba(72,187,120,0.5), 12, 0, 0, 6); -fx-cursor: hand;");

        String nameWithoutExt = fileName.replaceFirst("[.][^.]+$", "");
        docNameField.setText(nameWithoutExt);

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf": docTypeCombo.setValue("PDF"); break;
            case "docx": docTypeCombo.setValue("DOCX"); break;
            case "xlsx": docTypeCombo.setValue("XLSX"); break;
        }

        docNameField.setDisable(true);
        docTypeCombo.setDisable(true);
        openDocumentButton.setDisable(false);
    }

    @FXML
    private void viewFullDocument() {
        if (selectedFile != null) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(selectedFile);
                } else {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Desktop tidak didukung untuk membuka file.");
                }
            } catch (IOException e) {
                showErrorWithStackTrace("Gagal membuka dokumen.", e);
            }
        }
    }

    @FXML
    private void addDocument() {
        try {
            if (!validateInput()) return;
            String notes = notesArea.getText() != null ? notesArea.getText().trim() : "";

            MulaiCetakController job = new MulaiCetakController(
                    "DOC-" + System.currentTimeMillis(),
                    docNameField.getText().trim(),
                    docTypeCombo.getValue(),
                    copiesSpinner.getValue(),
                    priorityCombo.getValue(),
                    notes,
                    LocalDateTime.now()
            );

            if (databaseService != null && databaseService.isConnected()) {
                try {
                    int dbId = databaseService.insertJob(job);
                    job.setDbId(dbId);
                } catch (SQLException e) {
                    showErrorWithStackTrace("Gagal menyimpan data ke database.", e);
                }
            }

            queueController.addJob(job);
            clearForm();
            updateAllDisplays();

            showModalAlert(Alert.AlertType.INFORMATION, "Berhasil", "Dokumen berhasil ditambahkan ke antrian!");
        } catch (Exception e) {
            showErrorWithStackTrace("Gagal menambahkan dokumen.", e);
        }
    }

    private void showModalAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage currentStage = (Stage) docNameField.getScene().getWindow();
        alert.initOwner(currentStage);
        alert.initModality(Modality.WINDOW_MODAL);

        alert.showAndWait();
    }


    private boolean validateInput() {
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Mohon pilih dokumen terlebih dahulu.");
            return false;
        }
        if (docNameField.getText() == null || docNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama dokumen harus diisi.");
            docNameField.requestFocus();
            return false;
        }
        if (docTypeCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Jenis dokumen harus dipilih.");
            docTypeCombo.requestFocus();
            return false;
        }
        if (priorityCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Prioritas harus dipilih.");
            priorityCombo.requestFocus();
            return false;
        }
        if (copiesSpinner.getValue() == null || copiesSpinner.getValue() < 1) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Jumlah salinan harus minimal 1.");
            copiesSpinner.requestFocus();
            return false;
        }
        return true;
    }


    @FXML
    private void startPrint() {
        if (queueController.getPrintQueue().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Antrian cetak kosong.");
            return;
        }
        if (queueController.isPrinting()) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Printer sedang sibuk.");
            return;
        }
        queueController.startNextPrint();
    }

    @FXML
    private void cancelSelectedQueue() {
        if (selectedJob == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih dokumen yang ingin dibatalkan.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Pembatalan");
        confirmAlert.setHeaderText("Batalkan Dokumen");
        confirmAlert.setContentText("Apakah Anda yakin ingin membatalkan dokumen \"" + selectedJob.getDocumentName() + "\"?");

        Stage currentStage = (Stage) cancelSelectedQueueButton.getScene().getWindow();
        confirmAlert.initOwner(currentStage);
        confirmAlert.initModality(Modality.WINDOW_MODAL);

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            queueController.cancelJob(selectedJob);
            selectedJob = null;
            selectedJobCard = null;
            cancelSelectedQueueButton.setDisable(true);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Dokumen berhasil dibatalkan.");
        }
    }

    @FXML
    private void cancelAllQueue() {
        if (queueController.getPrintQueue().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Antrian kosong.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi");
        confirm.setHeaderText("Batalkan Semua Antrian");
        confirm.setContentText("Batalkan semua dokumen dalam antrian?");

        Stage currentStage = (Stage) cancelSelectedQueueButton.getScene().getWindow();
        confirm.initOwner(currentStage);
        confirm.initModality(Modality.WINDOW_MODAL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            queueController.cancelAllQueue();
            selectedJob = null;
            selectedJobCard = null;
            cancelSelectedQueueButton.setDisable(true);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Semua dokumen dalam antrian dibatalkan.");
        }
    }

    @FXML
    private void cancelEverything() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi");
        confirm.setHeaderText("Batalkan Semua Proses");
        confirm.setContentText("Batalkan semua proses cetak termasuk yang sedang berjalan?");

        Stage currentStage = (Stage) cancelSelectedQueueButton.getScene().getWindow();
        confirm.initOwner(currentStage);
        confirm.initModality(Modality.WINDOW_MODAL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            queueController.cancelEverything();
            selectedJob = null;
            selectedJobCard = null;
            cancelSelectedQueueButton.setDisable(true);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Semua proses pencetakan dibatalkan.");
        }
    }

    @FXML
    private void clearHistory() {
        if (queueController.getCompletedJobs().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Riwayat kosong.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi");
        confirm.setHeaderText("Hapus Riwayat");
        confirm.setContentText("Hapus semua riwayat cetak?");

        Stage currentStage = (Stage) clearHistoryBtn.getScene().getWindow();
        confirm.initOwner(currentStage);
        confirm.initModality(Modality.WINDOW_MODAL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            queueController.clearHistory();
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Riwayat cetak berhasil dihapus.");
        }
    }

    @FXML
    private void viewHistory() {
        try {
            int completedJobsCount = queueController.getCompletedJobs().size();
            int totalJobsCount = queueController.getAllJobs().size();
            databaseService.generateReport(completedJobsCount, totalJobsCount);
        } catch (JRException e) {
            showErrorWithStackTrace("Gagal membuat laporan Jasper.", e);
        } catch (SQLException e) {
            showErrorWithStackTrace("Gagal mengakses data untuk laporan.", e);
        } catch (Exception e) {
            showErrorWithStackTrace("Terjadi kesalahan tak terduga saat menampilkan laporan.", e);
        }
    }

    @FXML
    private void closeApplication() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Penutupan");
        confirmAlert.setHeaderText("Tutup Aplikasi");
        confirmAlert.setContentText("Apakah Anda yakin ingin menutup aplikasi?");

        Stage currentStage = (Stage) closeButton.getScene().getWindow();
        confirmAlert.initOwner(currentStage);
        confirmAlert.initModality(Modality.WINDOW_MODAL);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (statusUpdateTimer != null) statusUpdateTimer.stop();
            if (queueController != null) queueController.shutdown();
            if (databaseService != null) databaseService.closeConnection();
            showThankYouPageSimple(currentStage);
        }
    }

    private void showThankYouPageSimple(Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/qprintproject/FXMLProgram/ThankYou.fxml"));
            Parent thankYouRoot = loader.load();

            ThankYouController thankYouController = loader.getController();
            thankYouController.setData(1, "01:30", "HIGH");

            Scene thankYouScene = new Scene(thankYouRoot);
            currentStage.setScene(thankYouScene);
            currentStage.setTitle("QPrint - Terima Kasih");

            thankYouRoot.setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), thankYouRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } catch (IOException e) {
            System.err.println("Error loading ThankYou page: " + e.getMessage());
            currentStage.close();
        }
    }

    private void clearForm() {
        docNameField.clear();
        docTypeCombo.getSelectionModel().clearSelection();
        copiesSpinner.getValueFactory().setValue(1);
        priorityCombo.setValue("Normal");
        if (notesArea != null) notesArea.clear();
        selectedFile = null;
        selectFileButton.setText("📁 Pilih Dokumen");
        selectFileButton.setStyle("-fx-background-color: #1e6091; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 15 30; -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 10, 0, 0, 5); -fx-cursor: hand;");
        openDocumentButton.setDisable(true);
        docNameField.setDisable(true);
        docTypeCombo.setDisable(true);
    }

    private void startStatusUpdateTimer() {
        statusUpdateTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateStats()));
        statusUpdateTimer.setCycleCount(Animation.INDEFINITE);
        statusUpdateTimer.play();
    }

    private void updateAllDisplays() {
        Platform.runLater(() -> {
            updateQueueDisplay();
            updateHistoryDisplay();
            updateStats();
        });
    }

    private void updateStats() {
        List<MulaiCetakController> allJobs = queueController.getAllJobs();
        List<MulaiCetakController> completedJobs = queueController.getCompletedJobs();
        Queue<MulaiCetakController> printQueue = queueController.getPrintQueue();

        int totalDocs = allJobs.size();
        int completedCount = completedJobs.size();
        int waitingCount = printQueue.size();
        int totalPages = allJobs.stream().mapToInt(MulaiCetakController::getCopies).sum();

        totalDocsLabel.setText(String.valueOf(totalDocs));
        completedDocsLabel.setText(String.valueOf(completedCount));
        waitingDocsLabel.setText(String.valueOf(waitingCount));
        totalPagesLabel.setText(String.valueOf(totalPages));
        queueCountLabel.setText(String.valueOf(waitingCount));

        int totalSeconds = queueController.getEstimatedWaitTimeSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String waitTimeText;
        if (totalSeconds > 0) {
            if (minutes > 0) waitTimeText = String.format("%d menit %d detik", minutes, seconds);
            else waitTimeText = String.format("%d detik", seconds);
        } else {
            waitTimeText = "Tidak ada antrian";
        }
        estimatedWaitLabel.setText(waitTimeText);

        urgentCountLabel.setText(String.valueOf(queueController.getPriorityCount("Urgent")));
        highPriorityCountLabel.setText(String.valueOf(queueController.getPriorityCount("Tinggi")));
        normalPriorityCountLabel.setText(String.valueOf(queueController.getPriorityCount("Normal")));
        lowPriorityCountLabel.setText(String.valueOf(queueController.getPriorityCount("Rendah")));
    }

    private void updateQueueDisplay() {
        queueContainer.getChildren().clear();
        Queue<MulaiCetakController> printQueue = queueController.getPrintQueue();

        if (printQueue.isEmpty()) {

        } else {
            int position = 1;
            for (MulaiCetakController job : printQueue) {
                VBox jobCard = createJobCard(job, position, false);
                queueContainer.getChildren().add(jobCard);
                position++;
            }
        }
    }

    private void updateHistoryDisplay() {
        historyContainer.getChildren().clear();
        List<MulaiCetakController> completedJobs = queueController.getCompletedJobs();
        if (completedJobs.isEmpty()) {

        } else {
            for (int i = completedJobs.size() - 1; i >= 0; i--) {
                MulaiCetakController job = completedJobs.get(i);
                VBox jobCard = createJobCard(job, 0, true);
                historyContainer.getChildren().add(jobCard);
            }
        }
    }

    private VBox createJobCard(MulaiCetakController job, int position, boolean isHistory) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                "-fx-border-color: #e9ecef; -fx-border-width: 2; -fx-border-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        card.setUserData(job);
        Label titleLabel = new Label((position > 0 ? position + ". " : "") + "📄 " + job.getDocumentName());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label detailsLabel = new Label(String.format("%s • %d salinan • %s",
                job.getDocumentType(), job.getCopies(), job.getPriority()));
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");
        card.getChildren().addAll(titleLabel, detailsLabel);

        if (isHistory && job.getCompletedTime() != null) {
            Label timeLabel = new Label("⏰ Selesai: " + job.getFormattedCompletedTime());
            timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #48bb78;");
            card.getChildren().add(timeLabel);
        }

        if (job.getNotes() != null && !job.getNotes().isEmpty()) {
            Label notesLabel = new Label("📝 " + job.getNotes());
            notesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
            card.getChildren().add(notesLabel);
        }

        if (!isHistory) {
            card.setOnMouseClicked(e -> {
                if (selectedJobCard != null) {
                    selectedJobCard.setStyle(selectedJobCard.getStyle().replace("-fx-border-color: #007bff;", "-fx-border-color: #e9ecef;"));
                }
                selectedJobCard = card;
                selectedJob = job;
                card.setStyle(card.getStyle().replace("-fx-border-color: #e9ecef;", "-fx-border-color: #007bff;"));
                cancelSelectedQueueButton.setDisable(false);
            });
        }
        return card;
    }

    private void updatePrinterStatus(String status, Color color) {
        Platform.runLater(() -> {
            printerStatusLabel.setText(status);
            printerStatusLabel.setTextFill(color);
            monitorPrinterStatusLabel.setText(status);
            monitorPrinterStatusLabel.setTextFill(color);
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        try {
            Stage currentStage = (Stage) docNameField.getScene().getWindow();
            alert.initOwner(currentStage);
            alert.initModality(Modality.WINDOW_MODAL);
        } catch (Exception e) {
            System.err.println("Could not set alert modality: " + e.getMessage());
        }

        alert.showAndWait();
    }

    private void showErrorWithStackTrace(String headerText, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Terjadi Error");
        alert.setHeaderText(headerText);
        alert.setContentText("Klik 'Show Details' untuk informasi teknis.");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Detail teknis (stack trace):");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        VBox expandableContent = new VBox();
        expandableContent.setSpacing(10);
        expandableContent.getChildren().addAll(label, textArea);

        alert.getDialogPane().setExpandableContent(expandableContent);
        alert.showAndWait();
    }

    @Override
    public void onPrintStarted(MulaiCetakController job) {
        Platform.runLater(() -> {
            updatePrinterStatus("🔄 Mencetak", Color.ORANGE);
            currentJobLabel.setText(job.getDocumentName() + " (" + job.getCopies() + " salinan)");
            currentJobBox.setVisible(true);
        });
    }

    @Override
    public void onPrintCompleted(MulaiCetakController job) {
        Platform.runLater(() -> {
            currentJobBox.setVisible(false);
            updatePrinterStatus("✅ Siap", Color.GREEN);
        });
    }

    @Override
    public void onPrintCanceled() {
        Platform.runLater(() -> {
            currentJobBox.setVisible(false);
            updatePrinterStatus("✅ Siap", Color.GREEN);
        });
    }

    @Override public void onQueueUpdated() {
        updateAllDisplays();
    }
    @Override public void onStatsUpdated() {
        updateStats();
    }
}