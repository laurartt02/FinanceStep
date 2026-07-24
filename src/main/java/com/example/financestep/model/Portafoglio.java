package com.example.financestep.model;

import java.util.ArrayList;
import java.util.List;

public class Portafoglio {
    private double saldoDisponibile;
    private double limite;
    private double totSpesa;
    private List<Transazione> transazioni;

    public Portafoglio(double saldoIniziale, double limite){
        this.saldoDisponibile = saldoIniziale;
        this.limite = limite;
        this.totSpesa = 0.0;
        this.transazioni = new ArrayList<>();
    }

    // Methods

    public void aggiungiImporto(Transazione t){
        transazioni.add(t);
        if(t instanceof Entrata){
            saldoDisponibile += t.getImporto();
        } else if(t instanceof Spesa){
            saldoDisponibile -= t.getImporto();
            totSpesa += t.getImporto();
        }
    }

    public void sottraiImporto(Transazione t){
        if(transazioni.remove(t)){
            if(t instanceof Entrata){
                saldoDisponibile -= t.getImporto();
            } else if (t instanceof Spesa) {
                saldoDisponibile += t.getImporto();
                totSpesa -= t.getImporto();
            }
        }
    }

    // Getter and Setter

    public double getSaldoDisponibile() {
        return saldoDisponibile;
    }
    public void setSaldoDisponibile(double saldoDisponibile) {
        this.saldoDisponibile = saldoDisponibile;
    }

    public double getLimite() {
        return limite;
    }
    public void setLimite(double limite) {
        this.limite = limite;
    }

    public double getTotSpesa() {
        return totSpesa;
    }

    public List<Transazione> getTransazioni() {
        return transazioni;
    }
}
