<div align="center">

# 🖨️ QPrint — Sistem Antrian Printer

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-1E6BA3?style=for-the-badge&logo=java&logoColor=white)
![SQL Server](https://img.shields.io/badge/SQL_Server-2022-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JasperReports](https://img.shields.io/badge/JasperReports-6.21.3-darkgreen?style=for-the-badge)

**Aplikasi manajemen antrian printer berbasis desktop dengan sistem priority queue, real-time monitoring, dan laporan otomatis.**

</div>

---

## ✨ Fitur Unggulan

| Fitur | Deskripsi |
|-------|-----------|
| 🔐 **Login System** | Autentikasi sebelum masuk ke sistem |
| 📄 **Tambah Dokumen** | Support file PDF, DOCX, dan XLSX |
| ⚡ **Priority Queue** | Antrian berdasarkan prioritas: `Urgent > Tinggi > Normal > Rendah` |
| 🖨️ **Auto Print** | Setelah satu job selesai, otomatis lanjut ke berikutnya |
| 📊 **Real-time Stats** | Monitoring jumlah dokumen, estimasi waktu, & status printer |
| ❌ **Cancel Fleksibel** | Bisa cancel satu job, semua antrian, atau seluruh proses |
| 🗂️ **Riwayat Cetak** | Histori lengkap semua job yang sudah diproses |
| 📑 **Laporan PDF** | Generate laporan otomatis dengan JasperReports |
| 🎵 **Background Audio** | Musik latar saat aplikasi berjalan |

---

## 🛠️ Tech Stack

- **Language:** Java 21
- **UI Framework:** JavaFX 21.0.2 + FXML
- **Database:** Microsoft SQL Server
- **Report Engine:** JasperReports 6.21.3
- **Build Tool:** Apache Maven
- **IDE:** IntelliJ IDEA

---

## 🔄 Alur Aplikasi

```
🟢 Loading Screen
      ↓
🔐 Login Screen
      ↓
📋 Main Program (Manajemen Antrian)
      ↓
👋 Thank You Screen
```

---

## 🚀 Cara Menjalankan

### Prerequisites
- Java JDK 21+
- Apache Maven 3.x
- Microsoft SQL Server (running)
- IntelliJ IDEA (rekomendasi)

### Setup Database
Buat tabel berikut di SQL Server kamu:

```sql
CREATE TABLE RiwayatAntrianPrinter (
    ID          INT IDENTITY(1,1) PRIMARY KEY,
    NamaDokumen VARCHAR(255)  NOT NULL,
    JenisDokumen VARCHAR(10)  NOT NULL,
    JumlahSalinan INT         NOT NULL,
    Prioritas   VARCHAR(20)   NOT NULL,
    Status      VARCHAR(50)   NOT NULL,
    WaktuDibuat DATETIME      DEFAULT GETDATE(),
    WaktuSelesai DATETIME     NULL
);
```

### Konfigurasi Koneksi Database
Edit file `DBConnectPrinter.java` dan sesuaikan:
```java
// Ganti dengan konfigurasi SQL Server kamu
String url = "jdbc:sqlserver://localhost:1433;databaseName=NamaDatabase";
String user = "username";
String password = "password";
```

### Run Aplikasi
```bash
# Clone repo
git clone https://github.com/Zaldigengs32/QPrint-Printer-Queue-System.git
cd QPrint-Printer-Queue-System

# Build & Run
mvn javafx:run
```

---

## 📁 Struktur Project

```
QPrintProject/
├── src/
│   └── main/
│       ├── java/com/example/qprintproject/
│       │   ├── MainProgram/
│       │   │   └── MainProgram.java        # Entry point
│       │   ├── Controller/
│       │   │   ├── LoadingController.java  # Loading screen
│       │   │   ├── LoginController.java    # Autentikasi
│       │   │   ├── MainProgramController.java  # Controller utama
│       │   │   ├── MulaiCetakController.java   # Model job cetak
│       │   │   ├── QueueController.java        # Logic antrian
│       │   │   ├── TampilanController.java
│       │   │   └── ThankYouController.java
│       │   └── DBConnect/
│       │       ├── DBConnectPrinter.java   # Koneksi database
│       │       └── DatabaseService.java   # CRUD & laporan
│       └── resources/
│           ├── Audio/                     # File musik
│           └── com/example/qprintproject/
│               └── FXMLProgram/           # File FXML (UI)
└── pom.xml
```

---

<div align="center">
<sub>⭐ Kalau project ini bermanfaat, jangan lupa kasih bintang!</sub>
</div>
