package com.example.financestep.controller;


import com.example.financestep.DatabaseManager;
import com.example.financestep.model.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MainController {

    private Persona utenteCorrente;

    private Salvadanaio salvadanaioCorrente;

    @FXML
    private Label lblPersonaCorrente;

    private javafx.collections.ObservableList<Transazione> listaTransazioni = javafx.collections.FXCollections.observableArrayList();
    private javafx.collections.ObservableList<Task> listaTask = javafx.collections.FXCollections.observableArrayList();

    private javafx.collections.ObservableList<RichiestaExtra> listaRichieste = javafx.collections.FXCollections.observableArrayList();

    @FXML private VBox rootVBox;
    @FXML
    private Label lblSaldoPortafoglio;
    @FXML
    private Label lblSalvadanaio;
    @FXML
    private Label lblObiettivo;
    @FXML
    private Label badgeCompiti; // pallino di notifica Compiti da fare
    @FXML
    private Label badgeRichieste;

    @FXML private javafx.scene.control.Button btnModificaObiettivo;
    @FXML private javafx.scene.control.Button btnNuovoCompito;
    @FXML
    private javafx.scene.control.Button btnNuovaRichiesta;
    @FXML
    private javafx.scene.control.Button btnMonitoraJunior;

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
    private TableColumn<Task, String> colCompitoMittente;
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
    private TableColumn<RichiestaExtra, String> colRichiestaConcedente;
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

        // Aggiorna eventuali task scaduti PRIMA di mostrare la tabella e le notifiche
        controllaScadenzeTask();

        // Filtro compiti/task in base all'utente
        aggiornaListaCompiti();

        if (!isTutor) {
            javafx.application.Platform.runLater(() -> {
                mostraNotificheNuoviCompiti();
                mostraNotifichePremiRicevuti();
                mostraNotificheEsitoRichieste();
                mostraNotificheTaskScadutiJunior();
            });
        }

        if (isTutor) {
            javafx.application.Platform.runLater(() -> {
                mostraNotifichePagamentiTask();
                mostraNotificheRichiesteRicevute();
                mostraNotificheTaskScaduti();
            });
        }

        // Filtro richieste in base all'utente
        aggiornaListaRichieste();

        aggiornaSaldoPortafoglio();

        aggiornaVisteSalvadanaio();
        System.out.println(">>> DEBUG Salvadanaio caricato: obiettivo=" + salvadanaioCorrente.getSommaTarget() + " versato=" + salvadanaioCorrente.getSommaVersata());

        mostraTransazioni();
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

        // 4. Monitora Junior: riservato al Tutor
        if (btnMonitoraJunior != null) {
            btnMonitoraJunior.setVisible(isTutor);
            btnMonitoraJunior.setDisable(!isTutor);
        }
    }

    private void aggiornaVisteSalvadanaio() {
        lblSalvadanaio.setText(String.format("%.2f", salvadanaioCorrente.getSommaVersata()).replace(",", "."));
        lblObiettivo.setText(String.format("%.2f", salvadanaioCorrente.getSommaTarget()).replace(",", "."));
    }

    private void aggiornaListaTransazioni() {
        tableTransazioni.setItems(listaTransazioni);
    }

    private void aggiornaListaCompiti() {
        boolean isTutor = (utenteCorrente instanceof Tutor);

        // 1. Gestione Visibilità Colonna Destinatario
        // Solo il Tutor può vedere a chi è stato assegnato il compito
        if (colCompitoDestinatario != null) {
            colCompitoDestinatario.setVisible(isTutor);
        }

        // 2. Gestione Visibilità Colonna Mittente
        // Solo lo Junior può vedere a chi è stato assegnato il compito
        if (colCompitoMittente != null) {
            colCompitoMittente.setVisible(!isTutor);
        }

        // 3. Filtro Dati per Tabella Compiti
        if (isTutor) {
            // Il Tutor vede TUTTI i compiti assegnati a qualunque utente
            listaTask.sort(java.util.Comparator.comparingInt(Task::getId).reversed());
            tableCompiti.setItems(listaTask);
        } else if (utenteCorrente != null) {
            // Junior vede SOLO i compiti indirizzati specificamente a lui
            ObservableList<Task> compitiJunior = javafx.collections.FXCollections.observableArrayList();
            for (Task t : listaTask) {
                if (t.getDestinatario() != null && t.getDestinatario().equalsIgnoreCase(utenteCorrente.getUsername())) {
                    compitiJunior.add(t);
                }
            }
            compitiJunior.sort(java.util.Comparator.comparingInt(Task::getId).reversed());
            tableCompiti.setItems(compitiJunior);
        }
        aggiornaBadgeCompiti();
    }

    private void mostraNotificheNuoviCompiti() {
        int ultimoNotificato = DatabaseManager.getUltimoIdNotificato(utenteCorrente.getUsername());
        int massimoId = ultimoNotificato;

        List<Task> nuovi = new java.util.ArrayList<>();
        for (Task t : listaTask) {
            if (t.getDestinatario() != null
                    && t.getDestinatario().equalsIgnoreCase(utenteCorrente.getUsername())
                    && t.getId() > ultimoNotificato) {

                // Aggiorniamo comunque il tracciamento dell'ID più alto visto
                if (t.getId() > massimoId) {
                    massimoId = t.getId();
                }

                // NOTIFICA ESCLUSIVA: mostra l'alert "Nuovi Compiti" SOLO se il compito è ancora IN_CORSO
                if (t.getStato() == Task.StatoTask.IN_CORSO) {
                    nuovi.add(t);
                }
            }
        }

        // Se non ci sono nuovi compiti IN_CORSO, aggiorniamo comunque l'ID nel DB per evitare che rimanga indietro
        if (nuovi.isEmpty()) {
            if (massimoId > ultimoNotificato) {
                DatabaseManager.aggiornaUltimoIdNotificato(utenteCorrente.getUsername(), massimoId);
            }
            return;
        }

        nuovi.sort(java.util.Comparator.comparingInt(Task::getId));

        StringBuilder dettaglio = new StringBuilder();
        for (Task t : nuovi) {
            dettaglio.append("- ").append(t.getTitolo())
                    .append(" (scadenza: ").append(t.getScadenza())
                    .append(", da: ").append(t.getMittente())
                    .append(")\n");

            if (t.getId() > massimoId) {
                massimoId = t.getId();
            }
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Nuovi Compiti");
        alert.setHeaderText("Hai " + nuovi.size() + " nuov" + (nuovi.size() == 1 ? "o compito" : "i compiti") + " da fare!");
        alert.setContentText(dettaglio.toString());
        com.example.financestep.IconUtil.applica(alert);
        alert.showAndWait();

        DatabaseManager.aggiornaUltimoIdNotificato(utenteCorrente.getUsername(), massimoId);
    }

    private void aggiornaBadgeCompiti() {
        if (badgeCompiti == null || utenteCorrente == null) {
            if (badgeCompiti != null) badgeCompiti.setVisible(false);
            return;
        }

        boolean isTutor = (utenteCorrente instanceof Tutor);
        int conteggio = 0;

        if (isTutor) {
            // Task che i Junior gli hanno segnalato come completati, in attesa che invii il premio
            for (Task t : listaTask) {
                if (t.getMittente() != null
                        && t.getMittente().equalsIgnoreCase(utenteCorrente.getUsername())
                        && t.getStato() == Task.StatoTask.COMPLETATO) {
                    conteggio++;
                }
            }
        }
        else {
            // Task ancora da completare
            for (Task t : listaTask) {
                if (t.getDestinatario() != null
                        && t.getDestinatario().equalsIgnoreCase(utenteCorrente.getUsername())
                        && t.getStato() == Task.StatoTask.IN_CORSO) {
                    conteggio++;
                }
            }
        }

        if (conteggio > 0) {
            badgeCompiti.setText(String.valueOf(conteggio));
            badgeCompiti.setVisible(true);
        } else {
            badgeCompiti.setVisible(false);
        }
    }

    private void mostraNotifichePremiRicevuti() {
        int ultimoNotificato = DatabaseManager.getUltimoIdPremioNotificato(utenteCorrente.getUsername());
        int massimoId = ultimoNotificato;

        List<Task> pagati = new java.util.ArrayList<>();
        for (Task t : listaTask) {
            if (t.getDestinatario() != null
                    && t.getDestinatario().equalsIgnoreCase(utenteCorrente.getUsername())
                    && t.getStato() == Task.StatoTask.PAGATO
                    && t.getId() > ultimoNotificato) {
                pagati.add(t);
            }
        }

        if (pagati.isEmpty()) {
            return;
        }

        pagati.sort(java.util.Comparator.comparingInt(Task::getId));

        StringBuilder dettaglio = new StringBuilder();
        double totale = 0.0;
        for (Task t : pagati) {
            dettaglio.append("• ").append(t.getTitolo())
                    .append(" → €").append(String.format("%.2f", t.getPremio()))
                    .append("\n");
            totale += t.getPremio();

            if (t.getId() > massimoId) {
                massimoId = t.getId();
            }
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Premio Ricevuto! 🎉");
        alert.setHeaderText("Complimenti! Hai ricevuto €" + String.format("%.2f", totale) + " di premio");
        alert.setContentText(dettaglio.toString() + "\nL'importo è stato versato nel tuo Salvadanaio.");
        com.example.financestep.IconUtil.applica(alert);
        alert.showAndWait();

        DatabaseManager.aggiornaUltimoIdPremioNotificato(utenteCorrente.getUsername(), massimoId);
    }

    private void mostraNotifichePagamentiTask() {
        List<Task> daPagare = new java.util.ArrayList<>();
        for (Task t : listaTask) {
            if (t.getMittente() != null
                    && t.getMittente().equalsIgnoreCase(utenteCorrente.getUsername())
                    && t.getStato() == Task.StatoTask.COMPLETATO) {
                daPagare.add(t);
            }
        }
        daPagare.sort(java.util.Comparator.comparingInt(Task::getId));

        for (Task t : daPagare) {
            chiediInvioPremio(t);
        }
    }

    private void controllaScadenzeTask() {
        LocalDate oggi = LocalDate.now();
        for (Task t : listaTask) {
            if (t.getStato() == Task.StatoTask.IN_CORSO && t.getScadenza() != null && t.getScadenza().isBefore(oggi)) {
                t.setStato(Task.StatoTask.SCADUTO);
                DatabaseManager.aggiornaStatoTask(t);
            }
        }
    }

    private void mostraNotificheTaskScaduti() {
        int ultimoNotificato = DatabaseManager.getUltimoIdScadutoNotificato(utenteCorrente.getUsername());
        int massimoId = ultimoNotificato;

        List<Task> scaduti = new java.util.ArrayList<>();
        for (Task t : listaTask) {
            if (t.getMittente() != null
                    && t.getMittente().equalsIgnoreCase(utenteCorrente.getUsername())
                    && t.getStato() == Task.StatoTask.SCADUTO
                    && t.getId() > ultimoNotificato) {
                scaduti.add(t);
            }
        }

        if (scaduti.isEmpty()) {
            return;
        }

        scaduti.sort(java.util.Comparator.comparingInt(Task::getId));

        StringBuilder dettaglio = new StringBuilder();
        for (Task t : scaduti) {
            dettaglio.append("• ").append(t.getDestinatario())
                    .append(" non ha completato '").append(t.getTitolo())
                    .append("' entro la scadenza (").append(t.getScadenza()).append(")\n");

            if (t.getId() > massimoId) {
                massimoId = t.getId();
            }
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Compiti Scaduti");
        alert.setHeaderText("Alcuni compiti sono scaduti senza essere completati");
        alert.setContentText(dettaglio.toString());
        com.example.financestep.IconUtil.applica(alert);
        alert.showAndWait();

        DatabaseManager.aggiornaUltimoIdScadutoNotificato(utenteCorrente.getUsername(), massimoId);
    }

    private void mostraNotificheTaskScadutiJunior() {
        int ultimoNotificato = DatabaseManager.getUltimoIdScadutoNotificato(utenteCorrente.getUsername());
        int massimoId = ultimoNotificato;

        List<Task> scaduti = new java.util.ArrayList<>();
        for (Task t : listaTask) {
            if (t.getDestinatario() != null
                    && t.getDestinatario().equalsIgnoreCase(utenteCorrente.getUsername())
                    && t.getStato() == Task.StatoTask.SCADUTO
                    && t.getId() > ultimoNotificato) {
                scaduti.add(t);
            }
        }

        if (scaduti.isEmpty()) {
            return;
        }

        scaduti.sort(java.util.Comparator.comparingInt(Task::getId));

        StringBuilder dettaglio = new StringBuilder();
        for (Task t : scaduti) {
            dettaglio.append("• ").append(t.getTitolo())
                    .append(" (scadenza: ").append(t.getScadenza()).append(")\n");

            if (t.getId() > massimoId) {
                massimoId = t.getId();
            }
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Compiti Scaduti");
        alert.setHeaderText("Non hai completato in tempo " + (scaduti.size() == 1 ? "questo compito" : "questi compiti"));
        alert.setContentText(dettaglio.toString() + "\nNon puoi più completarlo/i.");
        com.example.financestep.IconUtil.applica(alert);
        alert.showAndWait();

        DatabaseManager.aggiornaUltimoIdScadutoNotificato(utenteCorrente.getUsername(), massimoId);
    }

    private void mostraNotificheRichiesteRicevute() {
        List<RichiestaExtra> daGestire = new java.util.ArrayList<>();
        for (RichiestaExtra r : listaRichieste) {
            if (r.getConcedente() != null
                    && r.getConcedente().equalsIgnoreCase(utenteCorrente.getUsername())
                    && r.getStato() == RichiestaExtra.StatoRichiesta.IN_ATTESA) {
                daGestire.add(r);
            }
        }
        daGestire.sort(java.util.Comparator.comparingInt(RichiestaExtra::getId));

        for (RichiestaExtra r : daGestire) {
            chiediGestioneRichiesta(r);
        }
    }



    private void aggiornaListaRichieste() {
        boolean isTutor = (utenteCorrente instanceof Tutor);

        // 1. Visibilità della colonna Richiedente: visible solo se Tutor
        if (colRichiestaRichiedente != null) {
            colRichiestaRichiedente.setVisible(isTutor);
        }

        // 2. Visibilità della colonna Concedente: visibile solo se Junior
        if (colRichiestaConcedente != null) {
            colRichiestaConcedente.setVisible(!isTutor);
        }

        // 3. Filtro dati
        if (isTutor) {
            // Il Tutor vede TUTTE le richieste inviate da chiunque
            listaRichieste.sort(java.util.Comparator.comparingInt(RichiestaExtra::getId).reversed());
            tableRichieste.setItems(listaRichieste);
        } else if (utenteCorrente != null) {
            // Lo Junior vede SOLO le proprie richieste
            ObservableList<RichiestaExtra> richiesteJunior = javafx.collections.FXCollections.observableArrayList();
            for (RichiestaExtra r : listaRichieste) {
                if (r.getRichiedente() != null && r.getRichiedente().equalsIgnoreCase(utenteCorrente.getUsername())) {
                    richiesteJunior.add(r);
                }
            }
            richiesteJunior.sort(java.util.Comparator.comparingInt(RichiestaExtra::getId).reversed());
            tableRichieste.setItems(richiesteJunior);
        }
        aggiornaBadgeRichieste();
    }

    private void aggiornaBadgeRichieste() {
        if (badgeRichieste == null || utenteCorrente == null) {
            if (badgeRichieste != null) badgeRichieste.setVisible(false);
            return;
        }

        boolean isTutor = (utenteCorrente instanceof Tutor);
        if (!isTutor) {
            badgeRichieste.setVisible(false);
            return;
        }

        int conteggio = 0;
        for (RichiestaExtra r : listaRichieste) {
            if (r.getConcedente() != null
                    && r.getConcedente().equalsIgnoreCase(utenteCorrente.getUsername())
                    && r.getStato() == RichiestaExtra.StatoRichiesta.IN_ATTESA) {
                conteggio++;
            }
        }

        if (conteggio > 0) {
            badgeRichieste.setText(String.valueOf(conteggio));
            badgeRichieste.setVisible(true);
        } else {
            badgeRichieste.setVisible(false);
        }
    }

    private void mostraNotificheEsitoRichieste() {
        int ultimoNotificato = DatabaseManager.getUltimoIdRichiestaNotificata(utenteCorrente.getUsername());
        int massimoId = ultimoNotificato;

        List<RichiestaExtra> risolte = new java.util.ArrayList<>();
        for (RichiestaExtra r : listaRichieste) {
            if (r.getRichiedente() != null
                    && r.getRichiedente().equalsIgnoreCase(utenteCorrente.getUsername())
                    && r.getStato() != RichiestaExtra.StatoRichiesta.IN_ATTESA
                    && r.getId() > ultimoNotificato) {
                risolte.add(r);
            }
        }

        risolte.sort(java.util.Comparator.comparingInt(RichiestaExtra::getId));

        for (RichiestaExtra r : risolte) {
            boolean accettata = (r.getStato() == RichiestaExtra.StatoRichiesta.APPROVATA);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(accettata ? "Richiesta Accettata" : "Richiesta Rifiutata");
            alert.setContentText("Richiesta per '" + r.getMotivazione() + "' "
                    + (accettata ? "accettata 🙂" : "rifiutata 🙁")
                    + (accettata ? ("\nImporto accreditato: €" + String.format("%.2f", r.getImporto())) : ""));
            com.example.financestep.IconUtil.applica(alert);
            alert.showAndWait();

            if (r.getId() > massimoId) {
                massimoId = r.getId();
            }
        }

        if (massimoId > ultimoNotificato) {
            DatabaseManager.aggiornaUltimoIdRichiestaNotificata(utenteCorrente.getUsername(), massimoId);
        }
    }

    // Metodi per la MenuBar

    // FILE

    // Generazione e Download Report Transazioni Tabellare
    @FXML
    private void gestisciSave() {
        if (utenteCorrente == null) {
            mostraAvviso(javafx.scene.control.Alert.AlertType.WARNING, "Attenzione", "Nessun utente attualmente connesso.");
            return;
        }

        // 1. Configurazione del FileChooser per far scegliere all'utente dove salvare
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Salva Report Transazioni");

        // Nome del file proposto di default con username e timestamp
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomePredefinito = "Report_Transazioni_" + utenteCorrente.getUsername() + "_" + timestamp + ".txt";
        fileChooser.setInitialFileName(nomePredefinito);

        // Filtro per salvare solo come file .txt
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Documenti di Testo (*.txt)", "*.txt")
        );

        // Recuperiamo la finestra (Stage) corrente per ancorare la finestra di dialogo
        javafx.stage.Stage stage = (javafx.stage.Stage) lblPersonaCorrente.getScene().getWindow();
        java.io.File fileReport = fileChooser.showSaveDialog(stage);

        // Se l'utente chiude la finestra o clicca su "Annulla"
        if (fileReport == null) {
            System.out.println("Salvataggio del report annullato dall'utente.");
            return;
        }

        // 2. Scrittura del Report Tabellare sul file selezionato
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(fileReport, java.nio.charset.StandardCharsets.UTF_8))) {

            // Intestazione
            writer.println("+-------------------------------------------------------------------------------+");
            writer.println("|                       FINANCESTEP - REPORT TRANSAZIONI                        |");
            writer.println("+-------------------------------------------------------------------------------+");
            writer.println(String.format("| Utente: %-69s |", utenteCorrente.getUsername()));
            writer.println(String.format("| Data Generazione: %-59s |", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
            writer.println(String.format("| Saldo Portafoglio: € %-57s |", lblSaldoPortafoglio.getText()));
            writer.println(String.format("| Salvadanaio Versato: € %-55s |", lblSalvadanaio.getText()));
            writer.println(String.format("| Obiettivo Salvadanaio: € %-53s |", lblObiettivo.getText()));
            writer.println("+-------------------------------------------------------------------------------+");
            writer.println();

            List<Transazione> transazioni = tableTransazioni.getItems();
            if (transazioni.isEmpty()) {
                writer.println("+-------------------------------------------------------------------------------+");
                writer.println("|                  NESSUNA TRANSAZIONE PRESENTE IN ARCHIVIO                     |");
                writer.println("+-------------------------------------------------------------------------------+");
            } else {
                // Intestazione Tabella
                writer.println("+------------+------------+---------------+-------------------------------------+");
                writer.println("| DATA       | TIPO       | IMPORTO       | DESCRIZIONE                         |");
                writer.println("+------------+------------+---------------+-------------------------------------+");

                double totaleEntrate = 0.0;
                double totaleUscite = 0.0;

                for (Transazione t : transazioni) {
                    String tipo = t.getClass().getSimpleName();
                    double importo = t.getImporto();
                    String dataStr = t.getData() != null ? t.getData().toString() : "N/D";
                    String desc = t.getDescrizione() != null ? t.getDescrizione() : "";

                    if (desc.length() > 35) {
                        desc = desc.substring(0, 32) + "...";
                    }

                    if (importo >= 0) {
                        totaleEntrate += importo;
                    } else {
                        totaleUscite += Math.abs(importo);
                    }

                    writer.println(String.format("| %-10s | %-10s | € %-11.2f | %-35s |",
                            dataStr,
                            tipo,
                            importo,
                            desc
                    ));
                }

                writer.println("+------------+------------+---------------+-------------------------------------+");

                // Totali finali
                writer.println(String.format("| TOTALE ENTRATE: € %-57.2f |", totaleEntrate));
                writer.println(String.format("| TOTALE USCITE:  € %-57.2f |", totaleUscite));
                writer.println("+-------------------------------------------------------------------------------+");
            }

            writer.println();
            writer.println("+-------------------------------------------------------------------------------+");
            writer.println("|                          FINE REPORT - FINANCESTEP                            |");
            writer.println("+-------------------------------------------------------------------------------+");

            // Notifica di conferma
            mostraAvviso(
                    javafx.scene.control.Alert.AlertType.INFORMATION,
                    "Report Scaricato",
                    "Il report delle transazioni è stato salvato con successo in:\n\n" + fileReport.getAbsolutePath()
            );

        } catch (IOException e) {
            e.printStackTrace();
            mostraAvviso(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Errore Salvataggio",
                    "Impossibile scaricare il report delle transazioni: " + e.getMessage()
            );
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/financestep/login.fxml"));
            Stage stageLogin = new Stage();
            stageLogin.setTitle("Accedi a FinanceStep");
            stageLogin.setScene(new Scene(loader.load(), 320, 280));
            com.example.financestep.IconUtil.applica(stageLogin);
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
                    // Riordina anche dopo una modifica, in caso la data sia cambiata
                    aggiornaListaTransazioni();
                    // Ricalcoliamo subito il saldo del portafoglio
                    aggiornaSaldoPortafoglio();
                }
            });

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Modifica Transazione");
            stage.setScene(new javafx.scene.Scene(root));
            com.example.financestep.IconUtil.applica(stage);
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

        com.example.financestep.IconUtil.applica(dialog);

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
            java.net.URL fxmlLocation = getClass().getResource("/com/example/financestep/guida.fxml");
            if (fxmlLocation == null) {
                fxmlLocation = getClass().getResource("/com/example/financestep/guida.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Guida all'Uso - FinanceStep");
            stage.setScene(new Scene(root));
            com.example.financestep.IconUtil.applica(stage);
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

        // Quando clicchi sullo sfondo, sposta il focus sul VBox principale (togliendolo dai bottoni/tabelle)
        rootVBox.setOnMouseClicked(event -> {
            rootVBox.requestFocus();
        });

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
            colCompitoMittente.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMittente()));

            colCompitoStato.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStatoTesto())
            );

            tableCompiti.setItems(listaTask);

        }

        // Tasto INVIO sulla tabella compiti per contrassegnare come completato
        if (tableCompiti != null) {
            tableCompiti.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    Task selezionato = tableCompiti.getSelectionModel().getSelectedItem();
                    if (selezionato == null) return;

                    boolean isTutor = (utenteCorrente instanceof Tutor);

                    if (!isTutor && selezionato.getStato() == Task.StatoTask.IN_CORSO) {

                        javafx.scene.control.Alert conferma = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                        conferma.setTitle("Compito Completato");
                        conferma.setHeaderText("Hai completato il compito '" + selezionato.getTitolo() + "'?");
                        conferma.setContentText("Se confermi, il Tutor " + selezionato.getMittente() + " riceverà una notifica e potrà inviarti il premio.");

                        javafx.scene.control.ButtonType btnRiferisci = new javafx.scene.control.ButtonType("Riferisci");
                        javafx.scene.control.ButtonType btnAnnulla = new javafx.scene.control.ButtonType("Annulla", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                        conferma.getButtonTypes().setAll(btnRiferisci, btnAnnulla);

                        com.example.financestep.IconUtil.applica(conferma);

                        conferma.showAndWait().ifPresent(risposta -> {
                            if (risposta == btnRiferisci) {
                                selezionato.confermaEsecuzione();

                                // AGGIORNAMENTO SU DATABASE
                                DatabaseManager.aggiornaStatoTask(selezionato);

                                tableCompiti.refresh();
                                aggiornaBadgeCompiti();

                                mostraAvviso(
                                        javafx.scene.control.Alert.AlertType.INFORMATION,
                                        "Notifica Inviata!",
                                        "Il task '" + selezionato.getTitolo() + "' è stato contrassegnato come COMPLETATO.\n" +
                                                "Notifica inviata con successo al Tutor!"
                                );
                            }
                            // Se preme Annulla, non facciamo nulla: il task resta IN_CORSO

                        });
                    } else if (isTutor && selezionato.getStato() == Task.StatoTask.COMPLETATO) {
                        chiediInvioPremio(selezionato);
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

            if (colRichiestaConcedente != null) {
                colRichiestaConcedente.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getConcedente()));
            }

            colRichiestaStato.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStato().name()));

            tableRichieste.setItems(listaRichieste);

        }

        // Tasto INVIO sulla tabella richieste per approvare / confermare
        if (tableRichieste != null) {
            tableRichieste.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    RichiestaExtra selezionata = tableRichieste.getSelectionModel().getSelectedItem();
                    if (selezionata == null) return;

                    if (utenteCorrente instanceof Tutor) {
                        if (selezionata.getStato() == RichiestaExtra.StatoRichiesta.IN_ATTESA) {
                            chiediGestioneRichiesta(selezionata);
                        }
                    } else {
                        mostraAvviso(
                                javafx.scene.control.Alert.AlertType.INFORMATION,
                                "Richiesta Inviata!",
                                "La richiesta per '" + selezionata.getMotivazione() + "' è in attesa di approvazione da parte del Tutor."
                        );
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

        boolean isTutor = (utenteCorrente instanceof Tutor);

        // Nascondi tutti i bottoni in basso
        if (btnNuovoCompito != null) btnNuovoCompito.setVisible(false);
        if (btnNuovaRichiesta != null) btnNuovaRichiesta.setVisible(false);
        // "MonitoraJunior" visibile SOLO nella tab Transazioni E se è un Tutor
        if (btnMonitoraJunior != null) btnMonitoraJunior.setVisible(isTutor);
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
        // fuori dalla tab Transazioni, il bottone non deve comparire
        if (btnMonitoraJunior != null) btnMonitoraJunior.setVisible(false);

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
        // fuori dalla tab Transazioni, il bottone non deve comparire
        if (btnMonitoraJunior != null) btnMonitoraJunior.setVisible(false);
    }

    // Gestione Salvadanaio

    @FXML
    private void modificaObiettivo() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Modifica Obiettivo");
        dialog.setHeaderText("Inserisci il nuovo obiettivo per il salvadanaio:");
        dialog.setContentText("Importo (€):");
        com.example.financestep.IconUtil.applica((javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow());

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

        com.example.financestep.IconUtil.applica(dialog);

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
                    tableTransazioni.getItems().add(0, s);

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

        com.example.financestep.IconUtil.applica(dialog);

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
                    tableTransazioni.getItems().add(0, e);

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

    // Metodo che gestisce l'invio del premio
    private void inviaPremioTask(Task task) {
        double saldoPortafoglio = 0.0;
        try {
            saldoPortafoglio = Double.parseDouble(lblSaldoPortafoglio.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            saldoPortafoglio = 0.0;
        }
        double saldoSalvadanaio = salvadanaioCorrente.getSommaVersata();
        double premio = task.getPremio();

        boolean usaPortafoglio;
        if (saldoPortafoglio >= saldoSalvadanaio) {
            usaPortafoglio = (saldoPortafoglio >= premio);
            if (!usaPortafoglio && saldoSalvadanaio < premio) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Fondi Insufficienti",
                        "Non hai abbastanza fondi né nel Portafoglio né nel Salvadanaio per inviare questo premio.");
                return;
            }
        } else {
            usaPortafoglio = !(saldoSalvadanaio >= premio);
            if (usaPortafoglio && saldoPortafoglio < premio) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Fondi Insufficienti",
                        "Non hai abbastanza fondi né nel Portafoglio né nel Salvadanaio per inviare questo premio.");
                return;
            }
        }

        // 1. Sottrai il premio dalla fonte scelta
        if (usaPortafoglio) {
            Spesa s = new Spesa(-premio, LocalDate.now(), "Premio task: " + task.getTitolo(), "Compiti");
            tableTransazioni.getItems().add(0, s);
            DatabaseManager.salvaTransazione(s, utenteCorrente.getUsername());
            aggiornaListaTransazioni();
            aggiornaSaldoPortafoglio();
        } else {
            salvadanaioCorrente.setSommaVersata(salvadanaioCorrente.getSommaVersata() - premio);
            DatabaseManager.salvaSalvadanaio(salvadanaioCorrente, utenteCorrente.getUsername());
            aggiornaVisteSalvadanaio();
        }

        // 2. Accredita il premio sul Salvadanaio dello Junior destinatario
        Salvadanaio salvadanaioJunior = DatabaseManager.caricaSalvadanaio(task.getDestinatario());
        if (salvadanaioJunior == null) {
            salvadanaioJunior = new Salvadanaio("Obiettivo", 0.0);
        }
        salvadanaioJunior.setSommaVersata(salvadanaioJunior.getSommaVersata() + premio);
        DatabaseManager.salvaSalvadanaio(salvadanaioJunior, task.getDestinatario());

        // Registra il movimento anche nella tabella Transazioni dello Junior, per tracciabilità
        Entrata premioRicevuto = new Entrata(premio, LocalDate.now(), "Premio task: " + task.getTitolo(), "Compiti");
        DatabaseManager.salvaTransazione(premioRicevuto, task.getDestinatario());

        // 3. Aggiorna lo stato del task a PAGATO
        task.confermaPagamento();
        DatabaseManager.aggiornaStatoTask(task);
        tableCompiti.refresh();
        aggiornaBadgeCompiti();

        mostraAvviso(javafx.scene.control.Alert.AlertType.INFORMATION, "Premio Inviato",
                "Premio di €" + String.format("%.2f", premio) + " inviato a " + task.getDestinatario() +
                        " per il task '" + task.getTitolo() + "'.");
    }

    // Metodo che chiede conferma per un task completato, con "Invia Denaro" o "Annulla"
    private void chiediInvioPremio(Task task) {
        boolean inRitardo = LocalDate.now().isAfter(task.getScadenza());

        javafx.scene.control.Alert conferma = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        conferma.setTitle(inRitardo ? "Sollecito: Premio in Sospeso" : "Task Completato");
        conferma.setHeaderText((inRitardo ? "⚠ In ritardo — " : "") + task.getDestinatario() + " ha completato il task '" + task.getTitolo() + "'");
        conferma.setContentText("Premio da inviare: €" + String.format("%.2f", task.getPremio())
                + (inRitardo ? "\nLa scadenza (" + task.getScadenza() + ") è passata: invia il premio quanto prima." : ""));

        javafx.scene.control.ButtonType btnInvia = new javafx.scene.control.ButtonType("Invia Denaro");
        javafx.scene.control.ButtonType btnAnnulla = new javafx.scene.control.ButtonType("Annulla", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        conferma.getButtonTypes().setAll(btnInvia, btnAnnulla);

        conferma.showAndWait().ifPresent(risposta -> {
            if (risposta == btnInvia) {
                inviaPremioTask(task);
            }
            // Su Annulla non facciamo nulla: il task resta COMPLETATO
        });
    }


    // Accetta Richiesta
    private void accettaRichiesta(RichiestaExtra r) {
        double saldoPortafoglio;
        try {
            saldoPortafoglio = Double.parseDouble(lblSaldoPortafoglio.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            saldoPortafoglio = 0.0;
        }
        double saldoSalvadanaio = salvadanaioCorrente.getSommaVersata();
        double importo = r.getImporto();

        boolean usaPortafoglio;
        if (saldoPortafoglio >= saldoSalvadanaio) {
            if (saldoPortafoglio < importo && saldoSalvadanaio < importo) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Fondi Insufficienti",
                        "Non hai abbastanza fondi né nel Portafoglio né nel Salvadanaio per accettare questa richiesta.");
                return;
            }
            usaPortafoglio = saldoPortafoglio >= importo;
        } else {
            if (saldoPortafoglio < importo && saldoSalvadanaio < importo) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Fondi Insufficienti",
                        "Non hai abbastanza fondi né nel Portafoglio né nel Salvadanaio per accettare questa richiesta.");
                return;
            }
            usaPortafoglio = !(saldoSalvadanaio >= importo);
        }

        // 1. Sottrai dalla fonte scelta (Tutor)
        if (usaPortafoglio) {
            Spesa s = new Spesa(-importo, LocalDate.now(), "Richiesta extra: " + r.getMotivazione(), "Richieste");
            tableTransazioni.getItems().add(0, s);
            DatabaseManager.salvaTransazione(s, utenteCorrente.getUsername());
            aggiornaListaTransazioni();
            aggiornaSaldoPortafoglio();
        } else {
            salvadanaioCorrente.setSommaVersata(salvadanaioCorrente.getSommaVersata() - importo);
            DatabaseManager.salvaSalvadanaio(salvadanaioCorrente, utenteCorrente.getUsername());
            aggiornaVisteSalvadanaio();
        }

        // 2. Accredita sul Portafoglio dello Junior (non sul Salvadanaio, a differenza del premio task)
        Entrata entrata = new Entrata(importo, LocalDate.now(), "Richiesta extra accettata: " + r.getMotivazione(), "Richieste");
        DatabaseManager.salvaTransazione(entrata, r.getRichiedente());

        // 3. Aggiorna stato
        r.setStato(RichiestaExtra.StatoRichiesta.APPROVATA);
        DatabaseManager.aggiornaStatoRichiesta(r);
        tableRichieste.refresh();
        aggiornaBadgeRichieste();

        mostraAvviso(javafx.scene.control.Alert.AlertType.INFORMATION, "Richiesta Accettata",
                "Hai accettato la richiesta di " + r.getRichiedente() + " per €" + String.format("%.2f", importo) + ".");
    }

    // Rifiuta Richiesta
    private void rifiutaRichiesta(RichiestaExtra r) {
        r.setStato(RichiestaExtra.StatoRichiesta.RIFIUTATA);
        DatabaseManager.aggiornaStatoRichiesta(r);
        tableRichieste.refresh();
        aggiornaBadgeRichieste();

        mostraAvviso(javafx.scene.control.Alert.AlertType.INFORMATION, "Richiesta Rifiutata",
                "Hai rifiutato la richiesta di " + r.getRichiedente() + ".");
    }

    private void chiediGestioneRichiesta(RichiestaExtra r) {
        javafx.scene.control.Alert conferma = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Nuova Richiesta");
        conferma.setHeaderText("Richiesta €" + String.format("%.2f", r.getImporto()) + " per '" + r.getMotivazione() + "' da " + r.getRichiedente());

        javafx.scene.control.ButtonType btnAccetta = new javafx.scene.control.ButtonType("Accetta");
        javafx.scene.control.ButtonType btnRifiuta = new javafx.scene.control.ButtonType("Rifiuta");
        javafx.scene.control.ButtonType btnRifletti = new javafx.scene.control.ButtonType("Rifletti", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        conferma.getButtonTypes().setAll(btnAccetta, btnRifiuta, btnRifletti);

        com.example.financestep.IconUtil.applica(conferma);

        conferma.showAndWait().ifPresent(risposta -> {
            if (risposta == btnAccetta) {
                accettaRichiesta(r);
            } else if (risposta == btnRifiuta) {
                rifiutaRichiesta(r);
            }
            // Rifletti: nessuna azione, resta IN_ATTESA
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
                com.example.financestep.IconUtil.applica(alert);
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
                listaTransazioni.add(0, nuovaTransazione);

                // 3. Riordina la lista per data (più recente in alto)
                aggiornaListaTransazioni();

                // 4. Ricalcola subito il saldo totale del portafoglio!
                aggiornaSaldoPortafoglio();

                System.out.println("Aggiunta alla tabella: " + nuovaTransazione.getDettaglio());
            });

            Stage stage = new Stage();
            stage.setTitle("Nuova Transazione");
            stage.setScene(new Scene(root));
            com.example.financestep.IconUtil.applica(stage);
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
            controller.setMittente(utenteCorrente.getUsername());

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
            com.example.financestep.IconUtil.applica(stage);
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
            controller.setOnSalvaCallback((importo, concedente, motivazione) -> {
                // 1. Crea l'oggetto RichiestaExtra
                RichiestaExtra nuovaRichiesta = new RichiestaExtra(importo, motivazione, utenteCorrente.getUsername(), concedente);

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
            com.example.financestep.IconUtil.applica(stage);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore", "Impossibile aprire la finestra di richiesta.");
        }
    }

    // Monitoraggio Transazioni di uno Junior da parte di un Tutor
    @FXML
    public void apriMonitoraJunior() {
        // Controllo permessi: solo il Tutor può monitorare uno Junior
        if (!(utenteCorrente instanceof Tutor)) {
            mostraAvviso(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Accesso Negato",
                    "Solo il Tutor può monitorare le transazioni di uno Junior!"
            );
            return;
        }

        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Monitora Transazioni di uno Junior");
        dialog.setHeaderText(null);
        dialog.setContentText("Scrivi nome utente Junior da osservare:");

        com.example.financestep.IconUtil.applica(dialog);

        dialog.showAndWait().ifPresent(nomeInserito -> {
            String nomeJunior = nomeInserito.trim();

            if (nomeJunior.isEmpty()) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.WARNING, "Attenzione", "Inserisci un nome utente.");
                return;
            }

            String ruolo = DatabaseManager.recuperaRuolo(nomeJunior);

            if (ruolo == null) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore",
                        "Lo Junior " + nomeJunior + " non esiste.");
                return;
            }

            if (!"Junior".equalsIgnoreCase(ruolo)) {
                mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore",
                        "L'utente " + nomeJunior + " non è uno Junior.");
                return;
            }

            apriFinestraMonitoraggio(nomeJunior);
        });
    }

    private void apriFinestraMonitoraggio(String nomeJunior) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/financestep/monitora_transazioni.fxml"));
            Parent root = loader.load();

            MonitoraTransazioniController controller = loader.getController();
            controller.caricaTransazioniDi(nomeJunior);

            Stage stage = new Stage();
            stage.setTitle("Transazioni di " + nomeJunior);
            stage.setScene(new Scene(root));
            com.example.financestep.IconUtil.applica(stage);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostraAvviso(javafx.scene.control.Alert.AlertType.ERROR, "Errore", "Impossibile aprire la finestra di monitoraggio.");
        }
    }

    /* Finestra per Avvisi */
    private void mostraAvviso(javafx.scene.control.Alert.AlertType tipo, String titolo, String messaggio) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        com.example.financestep.IconUtil.applica(alert);
        alert.showAndWait();
    }

}