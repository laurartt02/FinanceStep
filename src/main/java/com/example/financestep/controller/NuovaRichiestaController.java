package com.example.financestep.controller;

import com.example.financestep.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class NuovaRichiestaController {

    @FXML private TextField txtImporto;
    @FXML private TextField txtTutor;
    @FXML private TextArea txtMotivazione;

    // Callback che restituisce (Importo, Concedente/Tutor, Motivazione)
    public interface RichiestaCallback {
        void accept(double importo, String concedente, String motivazione);
    }
    private RichiestaCallback onSalvaCallback;

    public void setOnSalvaCallback(RichiestaCallback callback) {
        this.onSalvaCallback = callback;
    }

    @FXML
    private void gestisciSalva() {
        String importoStr = txtImporto.getText().trim();
        String concedente = txtTutor.getText().trim();
        String motivazione = txtMotivazione.getText().trim();

        if (importoStr.isEmpty() || concedente.isEmpty() || motivazione.isEmpty()) {
            mostraAlert("Attenzione", "Compila sia l'importo, sia il concedente che la motivazione.");
            return;
        }

        // Verifica che il Tutor scelto esista davvero e sia effettivamente un Tutor
        String ruolo = DatabaseManager.recuperaRuolo(concedente);
        if (ruolo == null) {
            mostraAlert("Errore", "Il Tutor '" + concedente + "' non esiste.");
            return;
        }
        if (!"Tutor".equalsIgnoreCase(ruolo)) {
            mostraAlert("Errore", "'" + concedente + "' non è un Tutor.");
            return;
        }

        try {
            double importo = Double.parseDouble(importoStr.replace(",", "."));
            if (importo <= 0) {
                mostraAlert("Attenzione", "L'importo deve essere maggiore di zero.");
                return;
            }

            if (onSalvaCallback != null) {
                onSalvaCallback.accept(importo, concedente, motivazione);
            }

            chiudiFinestra();

        } catch (NumberFormatException e) {
            mostraAlert("Errore Formato", "Inserisci un numero valido per l'importo.");
        }
    }

    @FXML
    private void gestisciAnnulla() {
        chiudiFinestra();
    }

    private void chiudiFinestra() {
        Stage stage = (Stage) txtImporto.getScene().getWindow();
        stage.close();
    }

    private void mostraAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
