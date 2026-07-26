package com.example.financestep.controller;

import com.example.financestep.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;

public class NuovoTaskController {

    @FXML private TextField txtTitolo;
    @FXML private TextField txtPremio;
    @FXML private DatePicker dpScadenza;
    @FXML private TextField txtDestinatario;

    private Consumer<Task> onSalvaCallback;
    private String mittente; // username del Tutor loggato, impostato dal MainController

    public void setMittente(String mittente) {
        this.mittente = mittente;
    }

    public void setOnSalvaCallback(Consumer<Task> callback) {
        this.onSalvaCallback = callback;
    }

    @FXML
    public void initialize() {
        dpScadenza.setValue(LocalDate.now().plusDays(7)); // Scadenza predefinita tra 1 settimana
    }

    @FXML
    private void gestisciSalva() {
        String titolo = txtTitolo.getText().trim();
        String premioStr = txtPremio.getText().trim();
        LocalDate scadenza = dpScadenza.getValue();
        String destinatario = txtDestinatario.getText().trim();

        if (titolo.isEmpty() || premioStr.isEmpty() || scadenza == null || destinatario.isEmpty()) {
            mostraAlert("Attenzione", "Compila tutti i campi prima di proseguire.");
            return;
        }

        try {
            double premio = Double.parseDouble(premioStr.replace(",", "."));
            if (premio <= 0) {
                mostraAlert("Attenzione", "Il premio deve essere maggiore di 0.");
                return;
            }

            // Crea il nuovo Task
            Task nuovoTask = new Task(titolo, premio, scadenza, destinatario, mittente);

            // Passa il nuovo task al MainController tramite il callback
            if (onSalvaCallback != null) {
                onSalvaCallback.accept(nuovoTask);
            }

            // Chiudi la finestra
            chiudiFinestra();

        } catch (NumberFormatException e) {
            mostraAlert("Errore Formato", "Inserisci una cifra numerica valida per il premio.");
        }
    }

    @FXML
    private void gestisciAnnulla() {
        chiudiFinestra();
    }

    private void chiudiFinestra() {
        Stage stage = (Stage) txtTitolo.getScene().getWindow();
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
