package com.example.financestep;

import com.example.financestep.model.Persona;
import com.example.financestep.model.Tutor;
import com.example.financestep.model.Junior;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtNome;

    @FXML
    private ComboBox<String> comboRuolo;

    @FXML
    public void initialize() {
        comboRuolo.getItems().addAll("Tutor", "Junior");
        comboRuolo.getSelectionModel().selectFirst();
    }

    @FXML
    private void gestisciLogin() {
        String username = txtNome.getText().trim();
        String ruoloSelezionato = comboRuolo.getValue();

        if (username.isEmpty()) {
            System.out.println("Inserisci un nome!");
            return;
        }

        // Creiamo l'oggetto corretto in base alla scelta
        Persona utenteLoggato;
        if ("Tutor".equalsIgnoreCase(ruoloSelezionato)) {
            utenteLoggato = new Tutor(username);
        } else {
            utenteLoggato = new Junior(username);
        }

        apriSchermataPrincipale(utenteLoggato);
    }

    private void apriSchermataPrincipale(Persona persona) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();

            // Passiamo l'utente loggato al MainController
            MainController mainController = loader.getController();
            mainController.setUtenteCorrente(persona);

            // Apriamo la finestra principale
            Stage stagePrincipale = new Stage();
            stagePrincipale.setTitle("FinanceStep - " + persona.getUsername());
            stagePrincipale.setScene(new Scene(root, 900, 600));
            stagePrincipale.show();

            // Chiudiamo la finestra di login
            Stage stageLogin = (Stage) txtNome.getScene().getWindow();
            stageLogin.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
