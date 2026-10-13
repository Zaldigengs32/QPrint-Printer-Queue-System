package com.example.qprintproject.DBConnect;

import com.example.qprintproject.Controller.MulaiCetakController;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class DatabaseService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DBConnectPrinter connection;

    public DatabaseService() throws Exception {
        this.connection = new DBConnectPrinter();
        if (!isConnected()) {
            throw new RuntimeException("Failed to establish database connection.");
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && connection.conn != null && !connection.conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public int insertJob(MulaiCetakController job) throws SQLException {
        String query = "INSERT INTO RiwayatAntrianPrinter (NamaDokumen, JenisDokumen, JumlahSalinan, Prioritas, Status, WaktuDibuat) OUTPUT INSERTED.ID VALUES (?, ?, ?, ?, ?, GETDATE())";
        int newId = -1;

        try (PreparedStatement pstat = connection.conn.prepareStatement(query)) {
            pstat.setString(1, job.getDocumentName());
            pstat.setString(2, job.getDocumentType());
            pstat.setInt(3, job.getCopies());
            pstat.setString(4, job.getPriority());
            pstat.setString(5, "Dibatalkan");

            try (ResultSet rs = pstat.executeQuery()) {
                if (rs.next()) {
                    newId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to save job, no ID generated.");
                }
            }
        }
        return newId;
    }

    public void updateJobStatus(MulaiCetakController job) throws SQLException {
        String query = "UPDATE RiwayatAntrianPrinter SET Status = ?, WaktuSelesai = GETDATE() WHERE ID = ?";

        try (PreparedStatement pstat = connection.conn.prepareStatement(query)) {
            pstat.setString(1, job.getStatus());
            pstat.setInt(2, job.getDbId());

            int rowsAffected = pstat.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No job found with ID: " + job.getDbId());
            }
        }
    }

    public void clearHistory() throws SQLException {
        String query = "DELETE FROM RiwayatAntrianPrinter WHERE Status IN ('COMPLETED', 'CANCELLED')";
        try (Statement stat = connection.conn.createStatement()) {
            stat.executeUpdate(query);
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.closeConnection();
            } catch (Exception e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    public void generateReport(int completedJobsCount, int totalJobsCount) throws JRException {
        if (!isConnected()) {
            throw new RuntimeException("Database connection not available");
        }

        try {
            InputStream jasperStream = getClass().getResourceAsStream("/report/QPRINT03.jasper");

            if (jasperStream == null) {
                throw new JRException("File laporan tidak ditemukan di classpath: /report/QPRINT03.jasper");
            }

            // 2. Siapkan parameter untuk laporan
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Laporan Riwayat Antrian Printer");
            parameters.put("GENERATED_DATE", LocalDateTime.now().format(DATE_FORMATTER));
            parameters.put("TOTAL_COMPLETED", completedJobsCount);
            parameters.put("TOTAL_DOCUMENTS", totalJobsCount);
            parameters.put("IS_EMPTY_DATA", completedJobsCount == 0);

            JasperPrint print = JasperFillManager.fillReport(jasperStream, parameters, connection.conn);

            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Laporan Riwayat Antrian Printer");
            viewer.setVisible(true);

        } catch (JRException e) {
            throw new JRException("Failed to generate report: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new JRException("An unexpected error occurred while generating the report: " + e.getMessage(), e);
        }
    }
}