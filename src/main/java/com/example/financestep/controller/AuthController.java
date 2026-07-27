package com.example.financestep.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {

    @FXML
    private void handleRegistrati(ActionEvent event) {
        apriFinestra(event, "registrazione.fxml", "FinanceStep - Registrazione");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        apriFinestra(event, "login.fxml", "FinanceStep - Login");
    }

    private void apriFinestra(ActionEvent event, String fxml, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/financestep/" + fxml));
            Parent root = loader.load();

            Stage nuovaStage = new Stage();
            nuovaStage.setTitle(titolo);
            nuovaStage.setScene(new Scene(root));
            com.example.financestep.IconUtil.applica(nuovaStage);
            nuovaStage.show();

            // Chiudiamo la finestra di scelta iniziale (auth.fxml)
            Stage stageAttuale = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stageAttuale.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
