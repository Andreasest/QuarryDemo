package QuarryDemo.model;

import javafx.beans.property.*;

public class HindRida {
    private final StringProperty materjal;
    private final DoubleProperty hind;
    private final StringProperty kogus;

    public HindRida(String materjal, Double hind, String kogus) {
        //Nime lihtsustus
        if (materjal.equals("Sõelutud täitepinnas")||materjal.equals("Sõelumata täitepinnas")) this.materjal= new SimpleStringProperty(materjal+" (muld)");
        else if (materjal.equals("Täitepinnas")) this.materjal=new SimpleStringProperty(materjal+" (savi)");
        else this.materjal = new SimpleStringProperty(materjal);

        this.hind = new SimpleDoubleProperty(hind);
        this.kogus = new SimpleStringProperty(kogus);
    }

    public String getMaterjal() {
        return materjal.get();
    }

    public StringProperty materjalProperty() {
        return materjal;
    }

    public Double getHind() {
        return hind.get();
    }

    public DoubleProperty hindProperty() {
        return hind;
    }

    public void setHind(Double hind) {
        this.hind.set(hind);
    }
    public String getKogus() {
        return kogus.get();
    }

    public StringProperty kogusProperty() {
        return kogus;
    }

}
