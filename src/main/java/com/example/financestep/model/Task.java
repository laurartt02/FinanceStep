package com.example.financestep.model;

import java.time.LocalDate;

public class Task {

    public enum StatoTask {
        IN_CORSO, // Junior non ha ancora finito
        COMPLETATO, // Junior ha riferito, in attesa che il Tutor invii il premio nel Salvadanaio
        PAGATO, // Tutor ha inviato il premio, Junior ha confermato
        SCADUTO // Junior non ha completato il task entro la scadenza, non più completabile
    }
    private int id; // -1 finché non salvato/caricato dal DB
    private String titolo;
    private double premio;
    private StatoTask stato;
    private LocalDate scadenza;
    private String destinatario; // username dello Junior a cui è destinato il task
    private String mittente; // username del Tutor che ha creato il task


    public Task(String titolo, double premio, LocalDate scadenza, String destinatario, String mittente) {
        this.id = -1; // -1 finché non è salvato/caricato dal DB
        this.titolo = titolo;
        this.premio = premio;
        this.scadenza = scadenza;
        this.stato = StatoTask.IN_CORSO;
        this.destinatario = destinatario;
        this.mittente = mittente;
    }

    // Methods

    // Junior preme "Riferisci"
    public void confermaEsecuzione() {
        this.stato = StatoTask.COMPLETATO;
    }

    // Tutor preme "Invia Denaro"
    public void confermaPagamento() {
        this.stato = StatoTask.PAGATO;
    }

    // Getter and Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public double getPremio() {
        return premio;
    }
    public void setPremio(double premio) { this.premio = premio; }

    public StatoTask getStato() { return stato; }
    public void setStato(StatoTask stato) { this.stato = stato; }

    public LocalDate getScadenza() { return scadenza; }
    public void setScadenza(LocalDate scadenza) { this.scadenza = scadenza; }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getMittente() { return mittente; }

    public void setMittente(String mittente) { this.mittente = mittente; }

    // Compatibilità con codice esistente che usava isCompletato()
    public boolean isCompletato() { return stato != StatoTask.IN_CORSO; }

    // Helper per mostrare lo stato in tabella
    public String getStatoTesto() {
        switch (stato) {
            case IN_CORSO: return "In corso";
            case COMPLETATO: return "Completato (in attesa premio)";
            case PAGATO: return "Pagato";
            case SCADUTO: return "Scaduto";
            default: return "";
        }
    }

}
