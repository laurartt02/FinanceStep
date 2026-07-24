package com.example.financestep.model;

import java.time.LocalDate;

public class RichiestaExtra {

    public LocalDate data;
    private double importo;
    private String motivazione;

    private String richiedente;
    private String stato; // es. "IN_ATTESA", "APPROVATA", "RIFIUTATA"

    public RichiestaExtra(double importo, String motivazione, String richiedente) {
        this.data=LocalDate.now();
        this.importo = importo;
        this.motivazione = motivazione;
        this.richiedente = richiedente;
        this.stato = "IN_ATTESA";
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

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }
}
