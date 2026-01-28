package QuarryDemo.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import QuarryDemo.model.Tahtaeg;
import QuarryDemo.service.TahtajadViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TahtajadViewController {

    @FXML private TreeView<String> treeView;
    private TahtajadViewModel model=new TahtajadViewModel();

    @FXML
    public void initialize() {
        //treeView
        uuendaTreeView();
        treeView.setShowRoot(false);

        //Listener
        treeView.setOnKeyPressed(e->{
            if (e.getCode()== KeyCode.DELETE){
                TreeItem<String> rida=treeView.getSelectionModel().getSelectedItem();
                String value = rida.getValue();
                if (value.equals("Pole midagi näidata...")) return;
                if (value.contains(" - ")) {
                    try {
                        int arveNr = Integer.parseInt(value.split(" - ")[0].trim());
                        model.kustutaTahtaeg(arveNr);
                        uuendaTreeView();

                    } catch (NumberFormatException ex) {
                        new Alert(Alert.AlertType.ERROR, "Vigane arvenumber").showAndWait();
                    }
                }
            }

            if (e.getCode()== KeyCode.ENTER){
                TreeItem<String> rida=treeView.getSelectionModel().getSelectedItem();
                String value = rida.getValue();
                if (value.equals("Pole midagi näidata...")) return;
                if (value.contains(" - ")) {
                    try {
                        int arveNr = Integer.parseInt(value.split(" - ")[0].trim());
                        model.MakstudTahtaeg(arveNr);
                        uuendaTreeView();

                    } catch (NumberFormatException ex) {
                        new Alert(Alert.AlertType.ERROR, "Vigane arvenumber").showAndWait();
                    }
                }
            }
        });

        //bold
        treeView.setCellFactory(tv -> new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().remove("date-cell");
                } else {
                    setText(item);

                    if (!getTreeItem().isLeaf()) {
                        getStyleClass().add("date-cell"); // määrame klassi ülemistele
                    } else {
                        getStyleClass().remove("date-cell");
                    }
                }
            }
        });

    }

    private TreeItem<String> buildTree() {
        ObservableList<Tahtaeg> tahtajad= model.getTahtajad();
        TreeItem<String> root = new TreeItem<>("Arved");
        //Kui tühi
        if (tahtajad.isEmpty()) {
            TreeItem<String> nothingToShow=new TreeItem<>("Pole midagi näidata...");
            root.getChildren().add(nothingToShow);
            return root;
        }

        Map<LocalDate, List<Tahtaeg>> grouped = tahtajad.stream()
                .collect(Collectors.groupingBy(Tahtaeg::getKuupäev));

        List<LocalDate> sortedDates = new ArrayList<>(grouped.keySet());
        Collections.sort(sortedDates);

        for (LocalDate date : sortedDates) {
            List<Tahtaeg> arved = grouped.get(date);

            // Summa arvutus
            double totalSum = arved.stream()
                    .mapToDouble(Tahtaeg::getSumma)
                    .sum();

            // Kuupäeva item koos summaga
            String dateLabel = date + " (€" + String.format("%.2f", totalSum) + ")";
            if (LocalDate.now().isAfter(date)){
                dateLabel=dateLabel+ " -  Üle aja!";
            }
            TreeItem<String> dateItem = new TreeItem<>(dateLabel);
            dateItem.setExpanded(true);

            for (Tahtaeg t : arved) {
                String label = t.getArvenumber() + " - " + t.getFirma() +
                        " (€" + String.format("%.2f", t.getSumma()) + ")";

                TreeItem<String> arveItem = new TreeItem<>(label);

                dateItem.getChildren().add(arveItem);
            }

            root.getChildren().add(dateItem);
        }

        return root;
    }

    private void uuendaTreeView() {
        treeView.setRoot(null);
        TreeItem<String> root = buildTree();
        root.setExpanded(true);
        treeView.setRoot(root);
    }


}
