package QuarryDemo.dialogue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import QuarryDemo.model.HindRida;
import QuarryDemo.service.MainViewModel;

import java.time.LocalDate;
import java.util.Map;

public class HinnadDialoog {

    private final MainViewModel model;
    private final String firma;
    private final LocalDate algKuupaev;
    private final LocalDate loppKuupaev;

    public HinnadDialoog(MainViewModel model, String firma, LocalDate algKuupaev, LocalDate loppKuupaev) {
        this.model = model;
        this.firma = firma;
        this.algKuupaev = algKuupaev;
        this.loppKuupaev = loppKuupaev;
    }

    public void show() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Hinnad firmale: " + firma);

        //Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        //Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
        //stage.getIcons().add(icon);


        // Nupud
        ButtonType saveButtonType = new ButtonType("Salvesta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Tabeli veerud
        TableColumn<HindRida, String> materjalCol = new TableColumn<>("Materjal");
        materjalCol.setCellValueFactory(cellData -> cellData.getValue().materjalProperty());
        materjalCol.setEditable(false);

        TableColumn<HindRida, Double> hindCol = new TableColumn<>("Hind");
        hindCol.setCellValueFactory(cellData -> cellData.getValue().hindProperty().asObject());
        hindCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        hindCol.setEditable(true);
        hindCol.setOnEditCommit(event -> {
            HindRida rida = event.getRowValue();
            Double uusHind = event.getNewValue();
            if (uusHind != null) {
                rida.setHind(uusHind);
            }
        });

        // Kogus
        TableColumn<HindRida, String> kogusCol = new TableColumn<>("Kogus");
        kogusCol.setCellValueFactory(cellData -> cellData.getValue().kogusProperty());
        kogusCol.setEditable(false);

        // Tabeli read
        ObservableList<HindRida> data = FXCollections.observableArrayList();
        Map<String, Double> hinnad = model.getHinnadFirmale(firma);

        double kogusKokku=0;

        for (Map.Entry<String, Double> entry : hinnad.entrySet()) {
            String materjal = entry.getKey();
            double hind = entry.getValue();
            String kogusStr = model.getKogusFirmale(firma, materjal,algKuupaev,loppKuupaev).replace(",",".");
            data.add(new HindRida(materjal, hind, kogusStr));
            try {
                kogusKokku += Double.parseDouble(kogusStr.replace(",", "."));
            } catch (NumberFormatException ignored) {}
        }

        TableView<HindRida> tableView = new TableView<>(data);
        tableView.getColumns().addAll(materjalCol, hindCol, kogusCol);
        tableView.setEditable(true);
        tableView.setPrefSize(300, 200);

        // Kogus kokku ja krediidilimiit
        Label kogusSumLabel = new Label("Kokku kogus (t):");
        TextField kogusSumField = new TextField(String.valueOf(kogusKokku));
        kogusSumField.setEditable(false);

        Label krediitLabel = new Label("Krediidilimiit (t):");
        TextField krediitField = new TextField(String.valueOf(model.getKrediitFirmale(firma)));

        HBox bottomBox = new HBox(20, kogusSumLabel, kogusSumField, krediitLabel, krediitField);
        bottomBox.setStyle("-fx-padding: 10; -fx-alignment: center-left;");


        VBox content = new VBox(10);
        Label juhis = new Label("NB! Vajuta Enter pärast hinna muutmist, enne salvestamist.");
        juhis.setStyle("-fx-text-fill: red; -fx-font-size: 12;");

        content.getChildren().addAll(tableView, bottomBox,juhis);
        dialog.getDialogPane().setContent(content);

        // Salvesta nupp
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                for (HindRida rida : data) {
                    model.setHinnadFirmale(firma, rida.getMaterjal(), rida.getHind());
                }
                try {
                    int uusKrediit=Integer.parseInt(krediitField.getText().trim());
                    model.setKrediitFirmale(firma, uusKrediit);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
