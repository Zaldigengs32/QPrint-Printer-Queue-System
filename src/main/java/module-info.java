module com.example.qprintproject {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;
    requires jasperreports;
    requires java.desktop;
    requires javafx.media;
    
    opens com.example.qprintproject to javafx.fxml;
    exports com.example.qprintproject;
    exports com.example.qprintproject.Controller;
    opens com.example.qprintproject.Controller to javafx.fxml;
    exports com.example.qprintproject.DBConnect;
    opens com.example.qprintproject.DBConnect to javafx.fxml;
    exports com.example.qprintproject.MainProgram;
    opens com.example.qprintproject.MainProgram to javafx.fxml;
}