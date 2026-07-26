package com.example.financestep.model;

import java.time.LocalDate;

public class RichiestaExtra {

    public LocalDate data;
    private double importo;
    private String motivazione;
    private String richiedente; // username dello Junior che ha fatto la richiesta
    private String concedente; // username del Tutor a cui è rivolta la richiesta
    public enum StatoRichiesta {
        IN_ATTESA, // Il Tutor non ha ancora visto la richiesta
        APPROVATA, // Il Tutor ha approvato la richiesta e mandato i soldi sul Portafoglio
        RIFIUTATA // Il Tutor ha rifiutato la richiesta, Junior non riceve niente
    }
    private int id; // -1 finché non salvato/caricato dal DB

    private StatoRichiesta stato;

    public RichiestaExtra(double importo, String motivazione, String richiedente, String concedente) {
        this.data=LocalDate.now();
        this.importo = importo;
        this.motivazione = motivazione;
        this.richiedente = richiedente;
        this.concedente = concedente;
        this.stato = StatoRichiesta.IN_ATTESA;
        this.id = -1;
    }

    // Getter and Setter

    public LocalDate getData() {
        return data;
    }
    public void setData(LocalDate data) {
        this.data = data;
    }

    public double getImporto() { return importo; }
    public void setImporto(double importo) { this.importo = importo; }

    public String getMotivazione() { return motivazione; }
    public void setMotivazione(String motivazione) { this.motivazione = motivazione; }

    public String getRichiedente() {
        return richiedente;
    }
    public void setRichiedente(String richiedente) {
        this.richiedente = richiedente;
    }

    public String getConcedente() {
        return concedente;
    }
    public void setConcedente(String concedente) {
        this.concedente = concedente;
    }

    public StatoRichiesta getStato() {
        return stato;
    }

    public void setStato(StatoRichiesta stato) {
        this.stato = stato;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Helper per mostrare lo stato nella tabella
    public String getStatoTesto(){
        switch(stato){
            case IN_ATTESA: return "In Attesa";
            case APPROVATA: return "Approvata dal Tutor (ricevi i soldi)";
            case RIFIUTATA: return "Rifiutata";
            default: return "";
        }
    }

}
