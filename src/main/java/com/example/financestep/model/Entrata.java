package com.example.financestep.model;

import java.time.LocalDate;

public class Entrata extends Transazione {
    private String sorgente;

    public Entrata(double importo, LocalDate data, String descrizione, String sorgente){
        super(importo, data, descrizione);
        this.sorgente=sorgente;
    }

    public String getSorgente(){
        return sorgente;
    }

    public void setSorgente(String sorgente){
        this.sorgente=sorgente;
    }

    public String getDettaglio(){
        return "[ENTRATA - " + sorgente + "] " + super.getDettaglio();
    }

}
