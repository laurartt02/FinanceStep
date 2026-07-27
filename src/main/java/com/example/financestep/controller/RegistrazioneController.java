package com.example.financestep.controller;

import com.example.financestep.DatabaseManager;
import com.example.financestep.model.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrazioneController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    // campo "gemello" testuale per mostrare la password in chiaro
    @FXML
    private TextField txtPasswordVisibile;

    // bottone-occhio che alterna la visibilità
    @FXML
    private Button btnTogglePassword;

    @FXML
    private ComboBox<String> comboRuolo;

    // stato corrente (mostrata sì/no)
    private boolean passwordVisibile = false;

    @FXML
    public void initialize() {
        comboRuolo.getItems().addAll("Tutor", "Junior");
        comboRuolo.getSelectionModel().selectFirst();

        // sincronizza il testo tra i due campi password
        txtPasswordVisibile.textProperty().bindBidirectional(txtPassword.textProperty());
    }

    // alterna visibilità tra PasswordField (mascherato) e TextField (in chiaro)
    @FXML
    private void toggleMostraPassword() {
        passwordVisibile = !passwordVisibile;

        txtPasswordVisibile.setVisible(passwordVisibile);
        txtPasswordVisibile.setManaged(passwordVisibile);
        txtPassword.setVisible(!passwordVisibile);
        txtPassword.setManaged(!passwordVisibile);

        btnTogglePassword.setText(passwordVisibile ? "🙈" : "👁");
    }

    @FXML
    private void handleConfermaRegistrazione(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String ruolo = comboRuolo.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            mostraErrore("Compila tutti i campi.");
            return;
        }

        if (DatabaseManager.utenteEsiste(username)) {
            mostraErrore("Username già in uso.");
            return;
        }

        boolean successo = DatabaseManager.salvaUtente(username, password, ruolo);

        if (!successo) {
            mostraErrore("Errore durante la registrazione, riprova.");
            return;
        }

        Persona utenteRegistrato;
        if ("Tutor".equalsIgnoreCase(ruolo)) {
            utenteRegistrato = new Tutor(username);
        } else {
            utenteRegistrato = new Junior(username);
        }

        Alert benvenuto = new Alert(Alert.AlertType.INFORMATION);
        benvenuto.setTitle("Registrazione completata");
        benvenuto.setHeaderText(null);
        benvenuto.setContentText("Benvenut* su FinanceStep! :)");
        benvenuto.showAndWait();

        apriSchermataPrincipale(utenteRegistrato, event);
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore registrazione");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        com.example.financestep.IconUtil.applica(alert);
        alert.showAndWait();
    }

    private void apriSchermataPrincipale(Persona persona, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/financestep/hello-view.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUtenteCorrente(persona);

            Stage stagePrincipale = new Stage();
            stagePrincipale.setTitle("FinanceStep - " + persona.getUsername());
            stagePrincipale.setScene(new Scene(root, 900, 600));
            com.example.financestep.IconUtil.applica(stagePrincipale);
            stagePrincipale.setMaximized(true);
            stagePrincipale.show();

            Stage stageRegistrazione = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stageRegistrazione.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
