package com.example.financestep;


import com.example.financestep.model.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class MainController {

    private Persona utenteCorrente;

    private Salvadanaio salvadanaioCorrente;

    @FXML
    private Label lblPersonaCorrente;

    private javafx.collections.ObservableList<Transazione> listaTransazioni = javafx.collections.FXCollections.observableArrayList();
    private javafx.collections.ObservableList<Task> listaTask = javafx.collections.FXCollections.observableArrayList();

    private javafx.collections.ObservableList<RichiestaExtra> listaRichieste = javafx.collections.FXCollections.observableArrayList();

    @FXML
    private Label lblSaldoPortafoglio;
    @FXML
    private Label lblSalvadanaio;
    @FXML
    private Label lblObiettivo;

    @FXML private javafx.scene.control.Button btnModificaObiettivo;
    @FXML private javafx.scene.control.Button btnNuovoCompito;
    @FXML private javafx.scene.control.Button btnNuovaRichiesta;

    @FXML private javafx.scene.control.MenuItem menuSave;
    @FXML private javafx.scene.control.MenuItem menuReset;
    @FXML private javafx.scene.control.MenuItem menuEditDelete;
    @FXML private MenuItem menuEditDeseleziona;

    // Tabella Transazioni
    @FXML
    private TableView<Transazione> tableTransazioni;
    @FXML
    private TableColumn<Transazione, LocalDate> colData;
    @FXML
    private TableColumn<Transazione, String> colDescrizione;
    @FXML
    private TableColumn<Transazione, String> colTipo;

    @FXML
    private TableColumn<Transazione, Double> colImporto;

    // Tabella Compiti
    @FXML
    private TableView<Task> tableCompiti;
    @FXML
    private TableColumn<Task, String> colCompitoTitolo;
    @FXML
    private TableColumn<Task, Double> colCompitoPremio;
    @FXML
    private TableColumn<Task, LocalDate> colCompitoScadenza;
    @FXML
    private TableColumn<Task, String> colCompitoDestinatario;
    @FXML
    private TableColumn<Task, String> colCompitoStato;

    // Tabella Richieste
    @FXML
    private TableView<RichiestaExtra> tableRichieste;
    @FXML
    private TableColumn<RichiestaExtra, LocalDate> colRichiestaData;
    @FXML
    private TableColumn<RichiestaExtra, Double> colRichiestaImporto;
    @FXML
    private TableColumn<RichiestaExtra, String> colRichiestaMotivazione;
    @FXML
    private TableColumn<RichiestaExtra, String> colRichiestaRichiedente;
    @FXML
    private TableColumn<RichiestaExtra, String> colRichiestaStato;


    // Metodo chiamato al login per passare i dati dell'utente
    public void setUtenteCorrente(Persona persona) {
        this.utenteCorrente = persona;

        // Determiniamo il ruolo da mostrare tra parentesi
        boolean isTutor = (persona instanceof Tutor);
        String ruolo = isTutor ? "Tutor" : "Junior";

        // Impostiamo la Label: "Persona: Username (Ruolo)"
        lblPersonaCorrente.setText("Persona: " + persona.getUsername() + " (" + ruolo + ")");
        System.out.println("Benvenuto nell'app, " + persona.getUsername() + "!");

        // Aggiorna la visibilità dei permessi della MenuBar
        aggiornaPermessiUI(isTutor);

        // Imposta il valore del Salvadanaio
        salvadanaioCorrente = DatabaseManager.caricaSalvadanaio(utenteCorrente.getUsername());
        if (salvadanaioCorrente == null) {
            salvadanaioCorrente = new Salvadanaio("Obiettivo", 0.0);
        }

        // Lista delle transazioni da memorizzare
        listaTransazioni = FXCollections.observableArrayList(
                DatabaseManager.caricaTransazioni(utenteCorrente.getUsername())
        );
        aggiornaListaTransazioni();

        // Filtro compiti/task in base all'utente
        aggiornaListaCompiti();

        // Filtro richieste in base all'utente
        aggiornaListaRichieste();

        aggiornaSaldoPortafoglio();

        aggiornaVisteSalvadanaio();
        System.out.println(">>> DEBUG Salvadanaio caricato: obiettivo=" + salvadanaioCorrente.getSommaTarget() + " versato=" + salvadanaioCorrente.getSommaVersata());

    }

    private void aggiornaPermessiUI(boolean isTutor) {
        // 1. Modifica Obiettivo: attiva per TUTTI (Junior e Tutor)
        if (btnModificaObiettivo != null) {
            btnModificaObiettivo.setVisible(true);
            btnModificaObiettivo.setDisable(false);
        }

        // 2. Reset Dati: riservato al Tutor
        if (menuReset != null) {
            menuReset.setDisable(!isTutor);
            menuReset.setVisible(isTutor); // Nascosto agli Junior
        }

        // 3. Elimina Transazione: riservato al Tutor
        if (menuEditDelete != null) {
            menuEditDelete.setDisable(!isTutor);
            menuEditDelete.setVisible(isTutor); // Nascosto agli Junior
        }
    }

    private void aggiornaVisteSalvadanaio() {
        lblSalvadanaio.setText(String.format("%.2f", salvadanaioCorrente.getSommaVersata()).replace(",", "."));
        lblObiettivo.setText(String.format("%.2f", salvadanaioCorrente.getSommaTarget()).replace(",", "."));
    }

    private void aggiornaListaTransazioni(){
        tableTransazioni.setItems(listaTransazioni);
    }

    private void aggiornaListaCompiti() {
        boolean isTutor = (utenteCorrente instanceof Tutor);

        // 1. Gestione Visibilità Colonna Destinatario
        // Solo il Tutor può vedere a chi è stato assegnato il compito
        if (colCompitoDestinatario != null) {
            colCompitoDestinatario.setVisible(isTutor);
        }

        // 2. Filtro Dati per Tabella Compiti
        if (isTutor) {
            // Il Tutor vede TUTTI i compiti assegnati a qualunque utente
            tableCompiti.setItems(listaTask);
        } else if (utenteCorrente != null) {
            // Junior vede SOLO i compiti indirizzati specificamente a lui
            ObservableList<Task> compitiJunior = javafx.collections.FXCollections.observableArrayList();
            for (Task t : listaTask) {
                if (t.getDestinatario() != null && t.getDestinatario().equalsIgnoreCase(utenteCorrente.getUsername())) {
                    compitiJunior.add(t);
                }
            }
            tableCompiti.setItems(compitiJunior);
        }
    }

    private void aggiornaListaRichieste() {
        boolean isTutor = (utenteCorrente instanceof Tutor);

        // 1. Visibilità della colonna Richiedente: visible solo se Tutor
        if (colRichiestaRichiedente != null) {
            colRichiestaRichiedente.setVisible(isTutor);
        }

        // 2. Filtro dati
        if (isTutor) {
            // Il Tutor vede TUTTE le richieste inviate da chiunque
            tableRichieste.setItems(listaRichieste);
        } else if (utenteCorrente != null) {
            // Lo Junior vede SOLO le proprie richieste
            ObservableList<RichiestaExtra> richiesteJunior = javafx.collections.FXCollections.observableArrayList();
            for (RichiestaExtra r : listaRichieste) {
                if (r.getRichiedente() != null && r.getRichiedente().equalsIgnoreCase(utenteCorrente.getUsername())) {
                    richiesteJunior.add(r);
                }
            }
            tableRichieste.setItems(richiesteJunior);
        }
    }

    // Metodi per la MenuBar

    // FILE
    @FXML
    private void gestisciSave() {
        System.out.println("Salvataggio dati...");
    }

    @FXML
    private void gestisciReset() {
        // CONTROLLO PERMESSI: Se NON è Tutor, blocca tutto!
        if (!(utenteCorrente instanceof Tutor)) {
            mostraAvviso(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Accesso Negato",
                    "Solo il Tutor ha i permessi per resettare i dati dell'applicazione!"
            );
            return;
        }

        // --- CODICE DI RESET ESISTENTE ---
        // (I tuoi comandi per svuotare le liste, ripristinare i saldi ecc.)
        listaTransazioni.clear();
        lblSaldoPortafoglio.setText("0.00");
        lblSalvadanaio.setText("0.00");
        mostraAvviso(javafx.scene.control.Alert.AlertType.INFORMATION, "Reset", "Dati resettati con successo.");
    }

    @FXML
    private void gestisciLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Stage stageLogin = new Stage();
            stageLogin.setTitle("Accedi a FinanceStep");
            stageLogin.setScene(new Scene(loader.load(), 320, 280));
            stageLogin.show();

            Stage stagePrincipale = (Stage) lblPersonaCorrente.getScene().getWindow();
            stagePrincipale.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void gestisciClose() {
        javafx.application.Platform.exit();
    }

    // EDIT
    @FXML
    private void gestisciEditTransazione() {
        Transazione selezionata = tableTransazioni.getSelectionModel().getSelectedItem();

        if (selezionata == null) {
            mostraAvviso(javafx.scene.control.Alert.AlertType.WARNING,
                    "Nessuna Selezione",
                    "Seleziona prima una transazione dalla tabella per poterla modificare.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/financestep/nuova_transazione.fxml"));
            javafx.scene.Parent root = loader.load();

            NuovaTransazioneController controller = loader.getController();

            // 1. Precompiliamo la finestra con i dati correnti della transazione
            controller.caricaDatiTransazione(selezionata);

            // 2. Definizione del callback per il salvataggio della modifica
            controller.setOnSalvaCallback(transazioneModificata -> {
                int index = tableTransazioni.getItems().indexOf(selezionata);
                if (index >= 0) {
                    // Sostituiamo il vecchio elemento con quello modificato
                    tableTransazioni.getItems().set(index, transazioneModificata);
                    // Ricalcoliamo subito il saldo del portafoglio
                    aggiornaSaldoPortafoglio();
                }
            });

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Modifica Transazione");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore", "Impossibile aprire la finestra di modifica.");
        }
    }

    @FXML
    private void gestisciFiltra() {
        // 1. Creiamo la finestra di dialogo standard con la casella di testo
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Filtra Transazioni");
        dialog.setHeaderText("Ricerca nella tabella");
        dialog.setContentText("Inserisci testo da cercare (es. 'Pizza', 'Entrata', '2026'):");

        // 2. Creiamo il terzo bottone per azzerare il filtro
        javafx.scene.control.ButtonType btnAzzera = new javafx.scene.control.ButtonType("Azzera Filtro");

        // Aggiungiamo il nuovo bottone insieme a OK e Annulla
        dialog.getDialogPane().getButtonTypes().add(btnAzzera);

        // 3. Mostriamo la finestra e gestiamo la scelta dell'utente
        java.util.Optional<String> result = dialog.showAndWait();

        // Verifichiamo quale bottone è stato premuto
        if (dialog.getResult() != null) {
            String query = dialog.getEditor().getText().trim().toLowerCase();

            if (query.isEmpty()) {
                tableTransazioni.setItems(listaTransazioni);
            } else {
                javafx.collections.ObservableList<Transazione> filtrate = javafx.collections.FXCollections.observableArrayList();

                for (Transazione t : listaTransazioni) {
                    String desc = (t.getDescrizione() != null) ? t.getDescrizione().toLowerCase() : "";
                    String tipo = t.getClass().getSimpleName().toLowerCase();
                    String data = (t.getData() != null) ? t.getData().toString().toLowerCase() : "";
                    String importo = String.valueOf(t.getImporto()).toLowerCase();

                    if (desc.contains(query) || tipo.contains(query) || data.contains(query) || importo.contains(query)) {
                        filtrate.add(t);
                    }
                }

                if (filtrate.isEmpty()) {
                    mostraAvviso(javafx.scene.control.Alert.AlertType.INFORMATION, "Nessun Risultato", "Nessuna transazione corrisponde a: '" + dialog.getEditor().getText() + "'");
                } else {
                    tableTransazioni.setItems(filtrate);
                }
            }
        } else {
            // Se è stato premuto "Azzera Filtro" (oppure se il risultato del testo è null)
            // controlliamo se è stato cliccato proprio il bottone Azzera:
            tableTransazioni.setItems(listaTransazioni);
            System.out.println("Filtro azzerato: ripristinate tutte le tuple.");
        }
    }

    @FXML
    private void gestisciDeseleziona() {
        // Pulisce la selezione attiva nella tabelle relative (Transazioni, Compiti e Richieste)
        tableTransazioni.getSelectionModel().clearSelection();
        tableCompiti.getSelectionModel().clearSelection();
        tableRichieste.getSelectionModel().clearSelection();
        System.out.println("Selezione annullata.");
    }

    @FXML
    private void gestisciDelete() {
        // CONTROLLO PERMESSI: Se NON è Tutor, blocca tutto!
        if (!(utenteCorrente instanceof Tutor)) {
            mostraAvviso(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Accesso Negato",
                    "Solo il Tutor può eliminare le transazioni dalla tabella!"
            );
            return;
        }

        // --- CODICE DI ELIMINAZIONE ESISTENTE ---
        Transazione selezionata = tableTransazioni.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            mostraAvviso(javafx.scene.control.Alert.AlertType.WARNING, "Nessuna Selezione", "Seleziona prima una transazione da eliminare.");
            return;
        }

        listaTransazioni.remove(selezionata);
        mostraAvviso(javafx.scene.control.Alert.AlertType.INFORMATION, "Eliminata", "Transazione eliminata correttamente.");
    }

    // HELP
    @FXML
    private void gestisciGuida() {
        try {
            java.net.URL fxmlLocation = getClass().getResource("guida.fxml");
            if (fxmlLocation == null) {
                fxmlLocation = getClass().getResource("/com/example/financestep/guida.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Guida all'Uso - FinanceStep");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore", "Impossibile aprire la finestra della guida.");
        }
    }

    @FXML
    private void gestisciInfo() {
        mostraAvviso(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                "Informazioni su FinanceStep",
                "FinanceStep - Applicazione di Educazione Finanziaria v1.0\n" +
                        "Sviluppato per la gestione condivisa dei risparmi (Tutor & Junior).\n\n" +
                        "Anno: 2026"
        );
    }

    @FXML
    public void initialize() {

        // 0. CARICAMENTO DATI SALVATI DAL DATABASE
        if (listaRichieste != null) {
            listaRichieste.clear();
            listaRichieste.addAll(DatabaseManager.caricaRichieste());
        }

        if (listaTask != null) {
            listaTask.clear();
            listaTask.addAll(DatabaseManager.caricaTask());
        }

        // Valori iniziali di prova
        lblSaldoPortafoglio.setText("0.00");
        lblSalvadanaio.setText("0.00");
        lblObiettivo.setText("0.00");

        /* Quanto un elemento di una tabella non viene selezionato
        il pulsante "Annulla Selezione" di Edit viene nascosto
         */
        menuEditDeseleziona.disableProperty().bind(
                tableTransazioni.getSelectionModel().selectedItemProperty().isNull()
                        .and(tableCompiti.getSelectionModel().selectedItemProperty().isNull())
                        .and(tableRichieste.getSelectionModel().selectedItemProperty().isNull())
        );



        // CONFIGURAZIONE TABELLA TRANSAZIONI

        // Inizializza la lista per la tabella
        tableTransazioni.setItems(listaTransazioni);

        // Collega le colonne ai getter della classe Transazione
        colData.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getData())
        );

        colDescrizione.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescrizione())
        );

        colImporto.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getImporto())
        );

        colTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClass().getSimpleName())
        );

        // CONFIGURAZIONE TABELLA COMPITI

        if (colCompitoTitolo != null) {
            colCompitoTitolo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitolo()));
            colCompitoPremio.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPremio()));
            colCompitoScadenza.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getScadenza()));
            colCompitoDestinatario.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestinatario()));

            colCompitoStato.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().isCompletato() ? "Completato" : "In corso")
            );

            tableCompiti.setItems(listaTask);
        }

        // Tasto INVIO sulla tabella compiti per contrassegnare come completato
        if (tableCompiti != null) {
            tableCompiti.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    Task selezionato = tableCompiti.getSelectionModel().getSelectedItem();
                    if (selezionato != null) {
                        selezionato.confermaEsecuzione();

                        // AGGIORNAMENTO SU DATABASE
                        DatabaseManager.aggiornaStatoTask(selezionato);

                        tableCompiti.refresh();
                        mostraAvviso(
                                javafx.scene.control.Alert.AlertType.INFORMATION,
                                "Notifica Inviata!",
                                "Il task '" + selezionato.getTitolo() + "' è stato contrassegnato come COMPLETATO.\n" +
                                        "Notifica inviata con successo al Tutor!"
                        );
                    }
                }
            });
        }

        // CONFIGURAZIONE TABELLA RICHIESTE
        if (colRichiestaImporto != null) {
            colRichiestaData.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().getData()));

            colRichiestaImporto.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().getImporto()));

            colRichiestaMotivazione.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getMotivazione()));

            if (colRichiestaRichiedente != null) {
                colRichiestaRichiedente.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getRichiedente()));
            }

            colRichiestaStato.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStato()));

            tableRichieste.setItems(listaRichieste);
        }

        // Tasto INVIO sulla tabella richieste per approvare / confermare
                if (tableRichieste != null) {
                    tableRichieste.setOnKeyPressed(event -> {
                        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                            RichiestaExtra selezionata = tableRichieste.getSelectionModel().getSelectedItem();

                        if (selezionata != null) {
                            // Se è il Tutor a premere INVIO, approva la richiesta
                            if (utenteCorrente instanceof Tutor) {
                                selezionata.setStato("APPROVATA");

                                // AGGIORNAMENTO SU DATABASE
                                DatabaseManager.aggiornaStatoRichiesta(selezionata);

                                tableRichieste.refresh();

                                mostraAvviso(
                                        javafx.scene.control.Alert.AlertType.INFORMATION,
                                        "Richiesta Approvata!",
                                        "La richiesta per '" + selezionata.getMotivazione() + "' di € " +
                                                String.format("%.2f", selezionata.getImporto()) + " è stata approvata."
                                );
                            }
                            // Se è lo Junior, invia/notifica la richiesta al Tutor
                            else {
                                mostraAvviso(
                                        javafx.scene.control.Alert.AlertType.INFORMATION,
                                        "Richiesta Inviata!",
                                        "La richiesta per '" + selezionata.getMotivazione() + "' è in attesa di approvazione da parte del Tutor."
                                );
                            }
                        }
                }
            });
        }


    }

    private void aggiornaSaldoPortafoglio() {
        double totale = 0.0;

        for (Transazione t : tableTransazioni.getItems()) {
            // Se la classe Transazione gestisce entrate/uscite (es. getImporto() restituisce valori positivi/negativi)
            totale += t.getImporto();
        }

        // Aggiorna la Label a schermo formattando a due decimali
        lblSaldoPortafoglio.setText(String.format("%.2f", totale).replace(",", "."));
    }

    // Metodi per alternare la vista delle tabelle
    @FXML
    public void mostraTransazioni() {
        tableTransazioni.setVisible(true);
        tableCompiti.setVisible(false);
        tableRichieste.setVisible(false);

        // Nascondi tutti i bottoni in basso
        if (btnNuovoCompito != null) btnNuovoCompito.setVisible(false);
        if (btnNuovaRichiesta != null) btnNuovaRichiesta.setVisible(false);
    }

    @FXML
    public void mostraCompiti() {
        tableTransazioni.setVisible(false);
        tableCompiti.setVisible(true);
        tableRichieste.setVisible(false);

        boolean isTutor = (utenteCorrente instanceof Tutor);

        // "Nuovo Compito" visibile SOLO se siamo nei Compiti ED è un Tutor
        if (btnNuovoCompito != null) btnNuovoCompito.setVisible(isTutor);
        if (btnNuovaRichiesta != null) btnNuovaRichiesta.setVisible(false);
    }

    @FXML
    public void mostraRichieste() {
        tableTransazioni.setVisible(false);
        tableCompiti.setVisible(false);
        tableRichieste.setVisible(true);

        boolean isJunior = !(utenteCorrente instanceof Tutor);

        // "Nuova Richiesta" visibile SOLO nelle Richieste ED è un Junior
        if (btnNuovoCompito != null) btnNuovoCompito.setVisible(false);
        if (btnNuovaRichiesta != null) btnNuovaRichiesta.setVisible(isJunior);
    }

    // Gestione Salvadanaio

    @FXML
    private void modificaObiettivo() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Modifica Obiettivo");
        dialog.setHeaderText("Inserisci il nuovo obiettivo per il salvadanaio:");
        dialog.setContentText("Importo (€):");

        dialog.showAndWait().ifPresent(risultato -> {
            try {
                double nuovoObiettivo = Double.parseDouble(risultato.replace(",", "."));
                if (nuovoObiettivo < 0) {
                    mostraAvviso(javafx.scene.control.Alert.AlertType.WARNING, "Attenzione", "L'obiettivo non può essere negativo.");
                    return;
                }
                salvadanaioCorrente.setSommaTarget(nuovoObiettivo);
                aggiornaVisteSalvadanaio();

                // Salvataggio nel DB
                DatabaseManager.salvaSalvadanaio(salvadanaioCorrente, utenteCorrente.getUsername());
                System.out.println(">>> DEBUG Salvataggio eseguito per: " + utenteCorrente.getUsername());

                controllaObiettivoRaggiunto();
            } catch (NumberFormatException e) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore Formato", "Inserisci una cifra numerica valida.");
            }
        });
    }

    @FXML
    private void versaNelSalvadanaio() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Versa nel Salvadanaio");
        dialog.setHeaderText("Sposta risorse dal Portafoglio al Salvadanaio");
        dialog.setContentText("Importo (€):");

        dialog.showAndWait().ifPresent(risultato -> {
            try {
                double versamento = Double.parseDouble(risultato.replace(",", "."));
                if (versamento <= 0) {
                    mostraAvviso(javafx.scene.control.Alert.AlertType.WARNING, "Attenzione", "Inserisci un importo maggiore di zero.");
                    return;
                }

                double saldoPortafoglio = Double.parseDouble(lblSaldoPortafoglio.getText().replace(",", "."));

                // CONTROLLO COPERTURA: Hai abbastanza disponibilità nel portafoglio?
                if (versamento <= saldoPortafoglio) {
                    salvadanaioCorrente.versaQuota(versamento);
                    aggiornaVisteSalvadanaio();

                    Spesa s = new Spesa(-versamento, LocalDate.now(), "Versamento in Salvadanaio", "Risparmi");
                    tableTransazioni.getItems().add(s);

                    // Persistenza: transazione + salvadanaio aggiornato
                    DatabaseManager.salvaTransazione(s, utenteCorrente.getUsername());
                    DatabaseManager.salvaSalvadanaio(salvadanaioCorrente, utenteCorrente.getUsername());

                    aggiornaSaldoPortafoglio();
                    controllaObiettivoRaggiunto();
                } else {
                    mostraAvviso(javafx.scene.control.Alert.AlertType.WARNING, "Fondi Insufficienti", "Non hai abbastanza soldi nel portafoglio per effettuare questo versamento.");
                }
            } catch (NumberFormatException e) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore Formato", "Inserisci una cifra numerica valida.");
            }
        });
    }

    @FXML
    private void prelevaDalSalvadanaio() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Preleva dal Salvadanaio");
        dialog.setHeaderText("Sposta risorse dal Salvadanaio al Portafoglio");
        dialog.setContentText("Importo (€):");

        dialog.showAndWait().ifPresent(risultato -> {
            try {
                double prelievo = Double.parseDouble(risultato.replace(",", "."));
                if (prelievo <= 0) {
                    mostraAvviso(javafx.scene.control.Alert.AlertType.WARNING, "Attenzione", "Inserisci un importo maggiore di zero.");
                    return;
                }

                // CONTROLLO COPERTURA: Ci sono abbastanza soldi nel salvadanaio?
                if (prelievo <= salvadanaioCorrente.getSommaVersata()) {
                    salvadanaioCorrente.setSommaVersata(salvadanaioCorrente.getSommaVersata() - prelievo);
                    aggiornaVisteSalvadanaio();

                    Entrata e = new Entrata(prelievo, LocalDate.now(), "Prelievo da Salvadanaio", "Risparmi");
                    tableTransazioni.getItems().add(e);

                    // Persistenza: transazione + salvadanaio aggiornato
                    DatabaseManager.salvaTransazione(e, utenteCorrente.getUsername());
                    DatabaseManager.salvaSalvadanaio(salvadanaioCorrente, utenteCorrente.getUsername());

                    aggiornaSaldoPortafoglio();
                } else {
                    mostraAvviso(javafx.scene.control.Alert.AlertType.WARNING, "Fondi Insufficienti", "Non hai abbastanza fondi nel salvadanaio.");
                }
            } catch (NumberFormatException e) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore Formato", "Inserisci una cifra numerica valida.");
            }
        });
    }

    private void controllaObiettivoRaggiunto() {
        try {

            double saldo = salvadanaioCorrente.getSommaVersata();
            double obiettivo = salvadanaioCorrente.getSommaTarget();

            if (obiettivo > 0 && saldo >= obiettivo) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Obiettivo Raggiunto! 🎉");
                alert.setHeaderText("Complimenti!");
                alert.setContentText("Hai raggiunto o superato il tuo obiettivo di " + String.format("%.2f", obiettivo) + " €!");
                alert.showAndWait();
            }
        } catch (NumberFormatException e) {
            // Ignora eventuali errori di conversione temporanei
        }
    }

    // Creazione di una nuova Transazione, Compito o Richiesta

    @FXML
    public void apriNuovaTransizione() {
        System.out.println("Hai cliccato su Nuova Transazione!");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/financestep/nuova_transazione.fxml"));
            Parent root = fxmlLoader.load();

            NuovaTransazioneController controller = fxmlLoader.getController();

            controller.setOnSalvaCallback(nuovaTransazione -> {

                // 1. Salva la transazione nel Database SQLite
                DatabaseManager.salvaTransazione(nuovaTransazione, utenteCorrente.getUsername());

                // 2. Aggiunge la transazione alla tabella
                listaTransazioni.add(nuovaTransazione);

                // 3. Ricalcola subito il saldo totale del portafoglio!
                aggiornaSaldoPortafoglio();

                System.out.println("Aggiunta alla tabella: " + nuovaTransazione.getDettaglio());
            });

            Stage stage = new Stage();
            stage.setTitle("Nuova Transazione");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void apriNuovoTask() {
        // Controllo permessi: solo Tutor può creare Task
        if (!(utenteCorrente instanceof Tutor)) {
            mostraAvviso(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Accesso Negato",
                    "Solo il Tutor può creare ed assegnare nuovi task!"
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/financestep/nuovo_task.fxml"));
            Parent root = loader.load();

            NuovoTaskController controller = loader.getController();

            // Callback per aggiungere il task appena creato alla lista
            controller.setOnSalvaCallback(nuovoTask -> {
                // 1. Salva il task nel Database SQLite
                DatabaseManager.salvaTask(nuovoTask);

                //2. Aggiunge alla lista principale dei task
                listaTask.add(nuovoTask);
                aggiornaListaCompiti();
                mostraAvviso(
                        javafx.scene.control.Alert.AlertType.INFORMATION,
                        "Task Assegnato",
                        "Il task '" + nuovoTask.getTitolo() + "' è stato assegnato a " + nuovoTask.getDestinatario()
                );
            });

            Stage stage = new Stage();
            stage.setTitle("Nuovo Task - FinanceStep");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore", "Impossibile aprire la finestra di creazione task.");
        }
    }

    @FXML
    public void apriNuovaRichiesta() {
        if (utenteCorrente instanceof Tutor) {
            mostraAvviso(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Accesso Negato",
                    "Solo gli Junior possono inviare richieste di fondi extra!"
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/financestep/nuova_richiesta.fxml"));
            Parent root = loader.load();

            NuovaRichiestaController controller = loader.getController();

            // Callback quando lo Junior invia la richiesta
            controller.setOnSalvaCallback((importo, motivazione) -> {
                // 1. Crea l'oggetto RichiestaExtra
                RichiestaExtra nuovaRichiesta = new RichiestaExtra(importo, motivazione, utenteCorrente.getUsername());

                // 2. Salva la richiesta nel Database SQLite
                DatabaseManager.salvaRichiesta(nuovaRichiesta);

                // 3. Aggiunge alla lista principale delle richieste
                listaRichieste.add(nuovaRichiesta);

                // 4. Aggiorna la vista della tabella (filtra per l'utente e gestisce la colonna)
                aggiornaListaRichieste();

                // 5. Notifica all'utente
                mostraAvviso(
                        javafx.scene.control.Alert.AlertType.INFORMATION,
                        "Richiesta Inviata",
                        "Richiesta di " + String.format("%.2f", importo) + " € inviata con successo al Tutor!\n" +
                                "Motivazione: " + motivazione
                );
            });

            Stage stage = new Stage();
            stage.setTitle("Nuova Richiesta Extra - FinanceStep");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore", "Impossibile aprire la finestra di richiesta.");
        }
    }

    // Finestra per Avvisi
    private void mostraAvviso(javafx.scene.control.Alert.AlertType tipo, String titolo, String messaggio) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

}