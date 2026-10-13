package com.example.qprintproject.Controller;

import com.example.qprintproject.DBConnect.DatabaseService;
import javafx.application.Platform;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class QueueController {
    private final LinkedList<MulaiCetakController> printQueue = new LinkedList<>();
    private final List<MulaiCetakController> completedJobs = new ArrayList<>();
    private final List<MulaiCetakController> allJobs = new ArrayList<>();

    private boolean isPrinting = false;
    private MulaiCetakController currentJob = null;
    private ExecutorService printExecutor = Executors.newSingleThreadExecutor();

    public interface PrintQueueCallback {
        void onPrintStarted(MulaiCetakController job);
        void onPrintCompleted(MulaiCetakController job);
        void onPrintCanceled();
        void onQueueUpdated();
        void onStatsUpdated();
    }

    private PrintQueueCallback callback;
    private DatabaseService databaseService;

    public QueueController(PrintQueueCallback callback) {
        this.callback = callback;
    }

    public void setDatabaseService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void addJob(MulaiCetakController job) {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }

        try {
            allJobs.add(job);
            insertJobByPriority(job);
            notifyCallbacks();
        } catch (Exception e) {
            System.err.println("Error adding job to queue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertJobByPriority(MulaiCetakController job) {
        String priority = job.getPriority();
        int insertIndex = 0;

        switch (priority) {
            case "Urgent":
                for (MulaiCetakController qJob : printQueue) {
                    if (!"Urgent".equals(qJob.getPriority()))
                        break;
                    insertIndex++;
                }
                printQueue.add(insertIndex, job);
                break;

            case "Tinggi":
                for (MulaiCetakController qJob : printQueue) {
                    if ("Urgent".equals(qJob.getPriority())) {
                        insertIndex++;
                    } else if ("Tinggi".equals(qJob.getPriority())) {
                        insertIndex++;
                    } else {
                        break;
                    }
                }
                printQueue.add(insertIndex, job);
                break;

            case "Normal":
                for (MulaiCetakController qJob : printQueue) {
                    if ("Rendah".equals(qJob.getPriority())) {
                        break;
                    }
                    insertIndex++;
                }
                printQueue.add(insertIndex, job);
                break;

            default:
                printQueue.addLast(job);
                break;
        }
    }

    public void startNextPrint() {
        if (printQueue.isEmpty() || isPrinting) {
            return;
        }
        try {
            currentJob = printQueue.poll();
            isPrinting = true;
            currentJob.setStatus("Sedang Dicetak");
            updateJobStatusInDatabase(currentJob);
            if (callback != null) {
                callback.onPrintStarted(currentJob);
            }
            notifyCallbacks();

            printExecutor.submit(() -> {
                try {
                    int printTimeMs = currentJob.getEstimatedPrintTimeSeconds() * 1000;
                    Thread.sleep(printTimeMs);

                    Platform.runLater(this::completePrint);
                } catch (InterruptedException e) {
                    Platform.runLater(() -> {
                        if (currentJob != null) {
                            currentJob.setStatus("Dibatalkan");
                            updateJobStatusInDatabase(currentJob);
                        }
                        if (callback != null) {
                            callback.onPrintCanceled();
                        }
                        resetPrinterStatus();
                        notifyCallbacks();
                    });
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.err.println("Error during printing: " + e.getMessage());
                        if (currentJob != null) {
                            currentJob.setStatus("Gagal");
                            updateJobStatusInDatabase(currentJob);
                        }
                        resetPrinterStatus();
                        notifyCallbacks();
                    });
                }
            });
        } catch (Exception e) {
            System.err.println("Error starting print: " + e.getMessage());
            resetPrinterStatus();
            notifyCallbacks();
        }
    }

    private void completePrint() {
        try {
            if (currentJob != null) {
                currentJob.setStatus("Selesai");
                currentJob.setCompletedTime(LocalDateTime.now());
                completedJobs.add(currentJob);

                updateJobStatusInDatabase(currentJob);

                if (callback != null) {
                    callback.onPrintCompleted(currentJob);
                }
                resetPrinterStatus();
                notifyCallbacks();

                // Otomatis Cetak Dokumen Selanjutnya Jika Sebelumnya Sudah Selesai
                if (!printQueue.isEmpty()) {
                    startNextPrint();
                }
            }
        } catch (Exception e) {
            System.err.println("Error completing print: " + e.getMessage());
            resetPrinterStatus();
            notifyCallbacks();
        }
    }

    private void updateJobStatusInDatabase(MulaiCetakController job) {
        if (databaseService != null && databaseService.isConnected() && job.getDbId() > 0) {
            try {
                databaseService.updateJobStatus(job);
                System.out.println("Status job " + job.getDbId() + " updated to '" + job.getStatus() + "' in database.");
            } catch (SQLException e) {
                System.err.println("Failed to update job status in database for job ID " + job.getDbId() + ": " + e.getMessage());
            }
        }
    }

    public void cancelJob(MulaiCetakController job) {
        if (job == null) return;
        try {
            if (printQueue.remove(job)) {
                job.setStatus("Dibatalkan");
                updateJobStatusInDatabase(job);
                notifyCallbacks();
            }
        } catch (Exception e) {
            System.err.println("Error canceling job: " + e.getMessage());
        }
    }

    public void cancelCurrentPrint() {
        if (isPrinting && currentJob != null) {
            try {
                printExecutor.shutdownNow();
                printExecutor = Executors.newSingleThreadExecutor();
                currentJob.setStatus("Dibatalkan");
                updateJobStatusInDatabase(currentJob);
                if (callback != null) {
                    callback.onPrintCanceled();
                }

                resetPrinterStatus();
                notifyCallbacks();
            } catch (Exception e) {
                System.err.println("Error canceling current print: " + e.getMessage());
            }
        }
    }

    private void resetPrinterStatus() {
        isPrinting = false;
        currentJob = null;
    }

    private void notifyCallbacks() {
        if (callback != null) {
            try {
                callback.onQueueUpdated();
                callback.onStatsUpdated();
            } catch (Exception e) {
                System.err.println("Error notifying callbacks: " + e.getMessage());
            }
        }
    }

    public LinkedList<MulaiCetakController> getPrintQueue() {return new LinkedList<>(printQueue);}
    public List<MulaiCetakController> getCompletedJobs() {return new ArrayList<>(completedJobs);}
    public List<MulaiCetakController> getAllJobs() {return new ArrayList<>(allJobs);}
    public boolean isPrinting() {return isPrinting;}

    public void shutdown() {
        try {
            if (printExecutor != null && !printExecutor.isShutdown()) {
                printExecutor.shutdown();
                if (!printExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    printExecutor.shutdownNow();
                }
            }
        } catch (InterruptedException e) {
            printExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    public void cancelAllQueue() {
        try {
            for (MulaiCetakController job : new ArrayList<>(printQueue)) {
                job.setStatus("Dibatalkan");
                updateJobStatusInDatabase(job);
            }
            printQueue.clear();
            notifyCallbacks();
        } catch (Exception e) {
            System.err.println("Error canceling all queue: " + e.getMessage());
        }
    }

    public void cancelEverything() {
        try {
            cancelCurrentPrint();
            cancelAllQueue();
            notifyCallbacks();
        } catch (Exception e) {
            System.err.println("Error canceling everything: " + e.getMessage());
        }
    }

    public void clearHistory() {
        try {
            if (databaseService != null && databaseService.isConnected()) {
                databaseService.clearHistory();
            }
            completedJobs.clear();
            notifyCallbacks();
        } catch (Exception e) {
            System.err.println("Error clearing history: " + e.getMessage());
        }
    }

    public int getEstimatedWaitTimeSeconds() {
        return printQueue.stream()
                .mapToInt(MulaiCetakController::getEstimatedPrintTimeSeconds)
                .sum();
    }

    public int getPriorityCount(String priority) {
        return (int) printQueue.stream()
                .filter(job -> priority.equals(job.getPriority()))
                .count();
    }
}