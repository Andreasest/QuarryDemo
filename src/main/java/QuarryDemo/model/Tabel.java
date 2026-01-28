package QuarryDemo.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Tabel {
    private final int id;
    private final SimpleStringProperty kuupaev;
    private final SimpleStringProperty numbrimark;
    private final SimpleStringProperty materjal;
    private final SimpleDoubleProperty kogus;

    public Tabel (int id,String kuupaev, String numbrimark, String materjal, double kogus) {
        this.id=id;
        this.kuupaev = new SimpleStringProperty(kuupaev);
        this.numbrimark = new SimpleStringProperty(numbrimark);
        this.materjal = new SimpleStringProperty(materjal);
        this.kogus = new SimpleDoubleProperty(kogus);
    }

    public String getKuupaev() { return kuupaev.get(); }
    public int getId() {
        return id;
    }
    public String getNumbrimark() { return numbrimark.get(); }
    public String getMaterjal() { return materjal.get(); }
    public double getKogus() { return kogus.get(); }
}
