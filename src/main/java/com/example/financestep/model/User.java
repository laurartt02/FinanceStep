package com.example.financestep.model;

import java.util.ArrayList;
import java.util.List;

public class User extends Persona {
    private int rating;
    private Portafoglio portafoglio;
    private List<Salvadanaio> salvadanai;

    // Costruttore semplificato per il Login
    public User(String username) {
        super(1, username, "", ""); // id=1, password e email vuote
        this.rating = 0;
        this.portafoglio = null;
        this.salvadanai = new ArrayList<>();
    }

    // Methods

    public void aggiungiSpesa(Spesa spesa) {
        if (portafoglio != null) {
            portafoglio.aggiungiImporto(spesa);
        }
    }

    public void richiediBudget() {
        // Logica per inviare una richiesta extra al Tutor
    }

    public void visualizzaReport() {
        // Logica per report/statistiche
    }

    public void aggiungiSalvadaio(Salvadanaio s) {
        this.salvadanai.add(s);
    }

    // Getter and Setter

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public Portafoglio getPortafoglio() { return portafoglio; }
    public void setPortafoglio(Portafoglio portafoglio) { this.portafoglio = portafoglio; }

    public List<Salvadanaio> getSalvadanai() { return salvadanai; }

}
