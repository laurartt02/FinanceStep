package com.example.financestep.model;

public class Salvadanaio {
    private String nomeObiettivo;
    private double sommaTarget;
    private double sommaVersata;

    public Salvadanaio(String nomeObiettivo, double sommaTarget) {
        this.nomeObiettivo = nomeObiettivo;
        this.sommaTarget = sommaTarget;
        this.sommaVersata = 0.0;
    }

    // Methods

    public void versaQuota(double importo) {
        if (importo > 0) {
            this.sommaVersata += importo;
        }
    }

    public double getPercentuale() {
        if (sommaTarget <= 0) return 0.0;
        double percentuale = (sommaVersata / sommaTarget) * 100;
        return Math.min(percentuale, 100.0); // Cap al 100%
    }

    // Getter and Setter

    public String getNomeObiettivo() { return nomeObiettivo; }
    public void setNomeObiettivo(String nomeObiettivo) { this.nomeObiettivo = nomeObiettivo; }

    public double getSommaTarget() { return sommaTarget; }
    public void setSommaTarget(double sommaTarget) { this.sommaTarget = sommaTarget; }

    public double getSommaVersata() { return sommaVersata; }
    public void setSommaVersata(double sommaVersata) { this.sommaVersata = sommaVersata; }
}
