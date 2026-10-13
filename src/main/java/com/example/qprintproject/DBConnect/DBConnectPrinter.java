package com.example.qprintproject.DBConnect;

import java.sql.*;

public class DBConnectPrinter {
    public Connection conn;
    public Statement stat;
    public ResultSet rs;
    public PreparedStatement pstat;

    public DBConnectPrinter() {
        try {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=SistemAntrianPrinter03;user=sa;password=123;encrypt=true;trustServerCertificate=true";
            conn = DriverManager.getConnection(url);
            stat = conn.createStatement();
        } catch (SQLException e) {
            System.out.println("Error saat connect database: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (rs != null) rs.close();
            if (stat != null) stat.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.out.println("Error saat menutup koneksi: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        DBConnectPrinter db = new DBConnectPrinter();
        System.out.println("Connect database berhasil");
    }
}