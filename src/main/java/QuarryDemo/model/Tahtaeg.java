package QuarryDemo.model;

import java.time.LocalDate;

public class Tahtaeg {
    private int arvenumber;
    private String firma;
    private LocalDate kuupäev;
    private Double summa;

    public Tahtaeg(int arvenumber, String firma, String kuupäev, Double summa) {
        this.arvenumber = arvenumber;
        this.firma = firma;
        this.kuupäev = LocalDate.parse(kuupäev);
        this.summa = summa;
    }

    public String getFirma() {
        return firma;
    }

    public LocalDate getKuupäev() {
        return kuupäev;
    }

    public Double getSumma() {
        return summa;
    }

    public int getArvenumber() {
        return arvenumber;
    }
}
