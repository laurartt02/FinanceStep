module com.example.financestep {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.financestep to javafx.fxml;
    opens com.example.financestep.model to javafx.base;
    exports com.example.financestep;
}