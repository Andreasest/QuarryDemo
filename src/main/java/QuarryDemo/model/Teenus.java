package QuarryDemo.model;

public class Teenus {
    private String materjal;
    private String kuupäevaVahemik;
    private String ühik;
    private Double kogus;
    private Double hind;

    public String getMaterjal() {
        return materjal;
    }

    public String getKuupäevaVahemik() {
        return kuupäevaVahemik;
    }

    public String getÜhik() {
        return ühik;
    }

    public Double getKogus() {
        return kogus;
    }

    public Double getHind() {
        return hind;
    }

    public Teenus(String materjal, String kuupäevaVahemik, String ühik, Double kogus, Double hind) {
        this.materjal = materjal;
        this.kuupäevaVahemik = kuupäevaVahemik;
        this.ühik = ühik;
        this.kogus = kogus;
        this.hind = hind;
    }

    public double getSumma() {
        return kogus*hind;
    }
}
