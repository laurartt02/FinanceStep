package com.example.financestep.model;

import java.time.LocalDate;

public class Spesa extends Transazione {
    private String categoria;

    public Spesa(double importo, LocalDate data, String descrizione, String categoria){
        super(importo, data, descrizione);
        this.categoria=categoria;
    }

    public String getCategoria(){
        return categoria;
    }

    public void setCategoria(String categoria){
        this.categoria=categoria;
    }

    public String getDettaglio(){
        return "[SPESA - " + categoria + "] " + super.getDettaglio();
    }

}
