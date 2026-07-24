package com.example.financestep.model;

import java.time.LocalDate;

public class Task {
    private String titolo;
    private double premio;
    private boolean completato;
    private LocalDate scadenza;
    private String destinatario; // username dello User a cui è destinato il task

    public Task(String titolo, double premio, LocalDate scadenza, String destinatario) {
        this.titolo = titolo;
        this.premio = premio;
        this.scadenza = scadenza;
        this.completato = false;
        this.destinatario = destinatario;
    }

    // Method
    public void confermaEsecuzione() {
        this.completato = true;
    }

    // Getter and Setter
    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public double getPremio() { return premio; }
    public void setPremio(double premio) { this.premio = premio; }

    public boolean isCompletato() { return completato; }
    public void setCompletato(boolean completato) { this.completato = completato; }

    public LocalDate getScadenza() { return scadenza; }
    public void setScadenza(LocalDate scadenza) { this.scadenza = scadenza; }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    // Helper method per mostrare lo stato in tabella ("Completato" / "In corso")
    public String getStatoTesto() {
        return completato ? "Completato" : "In corso";
    }
}
