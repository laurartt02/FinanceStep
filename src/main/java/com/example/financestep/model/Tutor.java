package com.example.financestep.model;

public class Tutor extends Persona {
    private int codiceFam;

    // Costruttore semplificato per il Login
    public Tutor(String username) {
        super(1, username, ""); // id=1, password  vuote
        this.codiceFam = 0;
    }

    // Methods

    public void approvaBudget(RichiestaExtra richiesta) {
        if (richiesta != null) {
            richiesta.setStato(RichiestaExtra.StatoRichiesta.APPROVATA);
        }
    }

    public void assegnaTask(Task task, Junior junior) {
        // Logica per collegare la task all'utente
    }

    public void modificaLimiti(Portafoglio p, double nuovoLimite) {
        if (p != null) {
            p.setLimite(nuovoLimite);
        }
    }

    // Getter and Setter

    public int getCodiceFam() { return codiceFam; }
    public void setCodiceFam(int codiceFam) { this.codiceFam = codiceFam; }

}
