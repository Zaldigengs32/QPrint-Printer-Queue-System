package com.example.qprintproject.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MulaiCetakController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private String id;
    private int dbId;
    private String namaDokumen;
    private String jenisDokumen;
    private int jumlahSalinan;
    private int jumlahHalaman;
    private String prioritas;
    private String catatan;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime completedTime;

    public MulaiCetakController(String id, String namaDokumen, String jenisDokumen, int jumlahSalinan, String prioritas, String catatan, LocalDateTime createdTime) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.namaDokumen = Objects.requireNonNull(namaDokumen, "Document name cannot be null");
        this.jenisDokumen = Objects.requireNonNull(jenisDokumen, "Document type cannot be null");
        this.jumlahSalinan = Math.max(1, jumlahSalinan);
        this.jumlahHalaman = jumlahSalinan;
        this.prioritas = Objects.requireNonNull(prioritas, "Priority cannot be null");
        this.catatan = catatan != null ? catatan : "";
        this.createdTime = Objects.requireNonNull(createdTime, "Created time cannot be null");
        this.status = "Menunggu";
        this.dbId = 0;
    }

    public MulaiCetakController(String id, String namaDokumen, String jenisDokumen,
                                int jumlahSalinan, int jumlahHalaman, String prioritas,
                                String catatan, LocalDateTime createdTime) {
        this(id, namaDokumen, jenisDokumen, jumlahSalinan, prioritas, catatan, createdTime);
        this.jumlahHalaman = Math.max(1, jumlahHalaman);
    }

    public int getDbId() {return dbId;}
    public void setDbId(int dbId) {this.dbId = dbId;}
    public String getId() {return id;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = Objects.requireNonNull(status, "Status cannot be null");}
    public LocalDateTime getCreatedTime() {return createdTime;}
    public LocalDateTime getCompletedTime() {return completedTime;}
    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }

    public String getDocumentName() {return namaDokumen;}
    public String getDocumentType() {return jenisDokumen;}
    public int getCopies() {return jumlahSalinan;}
    public String getPriority() {return prioritas;}
    public String getNotes() {return catatan;}
    public int getTotalPages() {return jumlahHalaman * jumlahSalinan;}

    public boolean isCompleted() {return "Selesai".equals(status) || "COMPLETED".equalsIgnoreCase(status);}
    public boolean isWaiting() {return "Menunggu".equals(status) || "WAITING".equalsIgnoreCase(status);}
    public String getFormattedCreatedTime() {return createdTime.format(DATE_FORMATTER);}
    public String getFormattedCompletedTime() {return completedTime != null ? completedTime.format(DATE_FORMATTER) : "Belum selesai";}
    public int getEstimatedPrintTimeSeconds() {return getTotalPages() * 10;}
    public String getFormattedEstimatedTime() {
        int seconds = getEstimatedPrintTimeSeconds();
        return formatTimeSeconds(seconds);
    }

    public long getActualPrintTimeSeconds() {
        if (completedTime != null && createdTime != null) {
            return java.time.Duration.between(createdTime, completedTime).getSeconds();
        }
        return 0;
    }

    public String getFormattedActualTime() {
        long seconds = getActualPrintTimeSeconds();
        return formatTimeSeconds((int) seconds);
    }

    private String formatTimeSeconds(int seconds) {
        if (seconds <= 0) {
            return "Tidak diketahui";
        }

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        StringBuilder timeStr = new StringBuilder();

        if (hours > 0) {timeStr.append(hours).append(" jam ");}
        if (minutes > 0) {timeStr.append(minutes).append(" menit ");}
        if (remainingSeconds > 0 || timeStr.length() == 0) {timeStr.append(remainingSeconds).append(" detik");}
        return timeStr.toString().trim();
    }

    public MulaiCetakController clone() {
        MulaiCetakController clone = new MulaiCetakController(
                this.id, this.namaDokumen, this.jenisDokumen,
                this.jumlahSalinan, this.jumlahHalaman, this.prioritas,
                this.catatan, this.createdTime
        );
        clone.setDbId(this.dbId);
        clone.setStatus(this.status);
        clone.setCompletedTime(this.completedTime);
        return clone;
    }

    @Override
    public String toString() {
        return String.format("MulaiCetakController{id='%s', dbId=%d, name='%s', type='%s', copies=%d, pages=%d, priority='%s', status='%s'}",
                id, dbId, namaDokumen, jenisDokumen, jumlahSalinan, jumlahHalaman, prioritas, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MulaiCetakController that = (MulaiCetakController) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}