package QuarryDemo.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import QuarryDemo.model.MaterjaliSummaRida;
import QuarryDemo.service.MüügiKäiveViewModel;
import QuarryDemo.util.NumberSõnadeks;

import java.util.List;
import java.util.Map;

public class MüügiKäiveViewController {
    @FXML private Accordion accordion;

    private MüügiKäiveViewModel model=new MüügiKäiveViewModel();

    private static final String[] kuuNimed = {
            "Jaanuar", "Veebruar", "Märts", "Aprill", "Mai", "Juuni",
            "Juuli", "August", "September", "Oktoober", "November", "Detsember"
    };

    public void initialize() {
        populate();
    }
    private void populate(){
        Map<String, List<MaterjaliSummaRida>> müügid=model.getMüügiKäive();
        int i=0;
        for (String kuu: müügid.keySet()){
            TableView<MaterjaliSummaRida> table=new TableView<>();
            // veerud
            TableColumn<MaterjaliSummaRida,String> materjalCol=new TableColumn<>("Materjal");
            materjalCol.setCellValueFactory(new PropertyValueFactory<>("materjal"));
            TableColumn<MaterjaliSummaRida, String> summaCol = new TableColumn<>("Summa");
            summaCol.setCellValueFactory(new PropertyValueFactory<>("summa"));
            table.getColumns().addAll(materjalCol,summaCol);

            //read
            ObservableList<MaterjaliSummaRida> read= FXCollections.observableArrayList(müügid.get(kuu));
            table.setItems(read);
            double summa=0;
            for (MaterjaliSummaRida rida: müügid.get(kuu)){
                summa+= rida.getSum();
            }

            //pealkiri
            TitledPane pane=new TitledPane(kuuNimi(kuu)+" - "+ NumberSõnadeks.formatDouble(summa)+" €",table);
            accordion.getPanes().add(pane);
            if(i== müügid.size()-1){
                accordion.setExpandedPane(pane);
            }
            i++;
        }
    }

    private String kuuNimi(String kuu){
        String[] osad = kuu.split("-");
        int kuuNr = Integer.parseInt(osad[1]);
        return osad[0] + " " + kuuNimed[kuuNr - 1];
    }

}
