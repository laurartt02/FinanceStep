package com.example.financestep.model;

import java.time.LocalDate;

public abstract class Transazione {
    private double importo;
    private LocalDate data;
    private String descrizione;

    public Transazione(double importo, LocalDate data, String descrizione) {
        this.importo = importo;
        this.data = data;
        this.descrizione = descrizione;
    }

    // Getter e Setter
    public double getImporto() { return importo; }
    public void setImporto(double importo) { this.importo = importo; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    // Metodo getDettaglio() come dal tuo Class Diagram
    public String getDettaglio() {
        return data + " - " + descrizione + ": " + importo + "€";
    }
}
