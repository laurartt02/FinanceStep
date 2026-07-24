package com.example.financestep;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class GuidaController {
    @FXML
    private void handleChiudi(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        Stage stage = (Stage) btn.getScene().getWindow();
        stage.close();
    }
}
