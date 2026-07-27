package com.example.financestep.controller;

import com.example.financestep.DatabaseManager;
import com.example.financestep.model.Persona;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.prefs.Preferences;

public class LoginController {

    private static final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
    private static final String PREF_ULTIMO_USERNAME = "ultimo_username";

    @FXML
    private TextField txtNome;

    @FXML
    private PasswordField txtPassword;

    // mostra la password in chiaro
    @FXML
    private TextField txtPasswordVisibile;

    // bottone-occhio che alterna la visibilità
    @FXML
    private Button btnTogglePassword;

    // stato corrente
    private boolean passwordVisibile = false;

    @FXML
    public void initialize() {
        String ultimoUsername = prefs.get(PREF_ULTIMO_USERNAME, "");
        if (!ultimoUsername.isEmpty()) {
            txtNome.setText(ultimoUsername);
        }
        // comboRuolo è sparito da qui: il ruolo ora viene dal DB, non più scelto a mano

        // sincronizza il testo tra i due campi password
        txtPasswordVisibile.textProperty().bindBidirectional(txtPassword.textProperty());
    }

    // alterna visibilità tra PasswordField (mascherato) e TextField (in chiaro)
    // e cambia l'icona del bottone di conseguenza
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
    private void gestisciLogin(ActionEvent event) {
        String username = txtNome.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostraErrore("Inserisci username e password.");
            return;
        }

        Persona utenteLoggato = DatabaseManager.verificaUtente(username, password);

        if (utenteLoggato == null) {
            mostraErrore("Credenziali errate o utente non registrato, riprova.");
            return;
        }

        prefs.put(PREF_ULTIMO_USERNAME, username);

        apriSchermataPrincipale(utenteLoggato, event);
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore login");
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

            Stage stageLogin = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stageLogin.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
