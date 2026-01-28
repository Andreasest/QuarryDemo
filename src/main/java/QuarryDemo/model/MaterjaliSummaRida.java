package QuarryDemo.model;

import QuarryDemo.util.NumberSõnadeks;

public class MaterjaliSummaRida {
    private final String materjal;
    private final double summa;

    public String getMaterjal() {
        return materjal;
    }

    public String getSumma() {
        return NumberSõnadeks.formatDouble(summa)+" €";
    }

    public MaterjaliSummaRida(String materjal, double summa) {
        this.materjal = materjal;
        this.summa = summa;
    }

    public double getSum() {
        return summa;
    }
}
