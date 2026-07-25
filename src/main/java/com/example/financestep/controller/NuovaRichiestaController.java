package com.example.financestep.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.BiConsumer;

public class NuovaRichiestaController {

    @FXML private TextField txtImporto;
    @FXML private TextArea txtMotivazione;

    // Callback che restituisce (Importo, Motivazione)
    private BiConsumer<Double, String> onSalvaCallback;

    public void setOnSalvaCallback(BiConsumer<Double, String> callback) {
        this.onSalvaCallback = callback;
    }

    @FXML
    private void gestisciSalva() {
        String importoStr = txtImporto.getText().trim();
        String motivazione = txtMotivazione.getText().trim();

        if (importoStr.isEmpty() || motivazione.isEmpty()) {
            mostraAlert("Attenzione", "Compila sia l'importo che la motivazione.");
            return;
        }

        try {
            double importo = Double.parseDouble(importoStr.replace(",", "."));
            if (importo <= 0) {
                mostraAlert("Attenzione", "L'importo deve essere maggiore di zero.");
                return;
            }

            if (onSalvaCallback != null) {
                onSalvaCallback.accept(importo, motivazione);
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
