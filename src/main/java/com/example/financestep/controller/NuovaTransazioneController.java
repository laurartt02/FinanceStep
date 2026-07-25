package com.example.financestep.controller; // o il package corretto del controller

import com.example.financestep.model.Entrata;
import com.example.financestep.model.Spesa;
import com.example.financestep.model.Transazione;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;

public class NuovaTransazioneController {
    @FXML private TextField txtDescrizione;
    @FXML private TextField txtImporto;
    @FXML private TextField txtTipo;
    @FXML private TextField txtData;

    private Consumer<Transazione> onSalvaCallback;

    public void setOnSalvaCallback(Consumer<Transazione> callback) {
        this.onSalvaCallback = callback;
    }

    @FXML
    public void salvaTransazione() {
        try {
            String descrizione = txtDescrizione.getText();
            double importo = Double.parseDouble(txtImporto.getText().trim());
            String tipo = txtTipo.getText().trim();

            // Converte la data scritta nel formato YYYY-MM-DD (es. 2026-07-23)
            LocalDate data = LocalDate.parse(txtData.getText().trim());

            Transazione nuovaTransazione;

            // Distinguiamo se creare un'Entrata (+) o una Spesa (-)
            if (tipo.equalsIgnoreCase("Entrata")) {
                nuovaTransazione = new Entrata(Math.abs(importo), data, descrizione, tipo);
            } else {
                // Forziamo l'importo ad essere NEGATIVO per la Spesa
                nuovaTransazione = new Spesa(-Math.abs(importo), data, descrizione, tipo);
            }

            // Se il callback è stato impostato dal MainController, inviamo i dati
            if (onSalvaCallback != null) {
                onSalvaCallback.accept(nuovaTransazione);
            }

            // Chiudiamo la finestra
            chiudiFinestra();
        } catch (Exception e) {
            System.err.println("Errore nei dati inseriti: " + e.getMessage());
            // Qui potresti anche mostrare un alert di errore all'utente se la data o l'importo sono scritti male
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore di Inserimento");
            alert.setHeaderText("Dati non validi");
            alert.setContentText("Verifica di aver inserito correttamente i dati:\n" +
                    "- L'importo deve essere un numero (es. 25.50)\n" +
                    "- La data deve essere nel formato AAAA-MM-DD (es. 2026-07-23)");
            alert.showAndWait();
        }
    }

    // Carico dati esistenti
    public void caricaDatiTransazione(Transazione t) {
        txtDescrizione.setText(t.getDescrizione());
        // Convertiamo l'importo in valore assoluto (positivo) per la vista
        txtImporto.setText(String.valueOf(Math.abs(t.getImporto())));
        txtData.setText(t.getData().toString());

        // Identifica se è Spesa o Entrata
        if (t instanceof com.example.financestep.model.Entrata) {
            txtTipo.setText("Entrata");
        } else {
            txtTipo.setText("Spesa");
        }
    }

    @FXML
    public void chiudiFinestra() {
        Stage stage = (Stage) txtDescrizione.getScene().getWindow();
        stage.close();
    }
}



