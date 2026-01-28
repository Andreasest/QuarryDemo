package QuarryDemo.dialogue;

import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.ReadOnlyStringWrapper;
import QuarryDemo.service.MainViewModel;

public class NumbrimargidDialoog {

    private final MainViewModel model;
    private final String firma;

    public NumbrimargidDialoog(MainViewModel model, String firma) {
        this.model = model;
        this.firma = firma;
    }

    public void show() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(firma + " numbrimärgid");
        //Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        //Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        //stage.getIcons().add(icon);

        ButtonType sulge = new ButtonType("Sulge", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(sulge);

        //All data
        ObservableList<String> allData = FXCollections.observableArrayList(model.getNumbrimargidFirmale(firma));
        FilteredList<String> filteredData = new FilteredList<>(allData, s -> true);

        // TableView
        TableView<String> table = new TableView<>(filteredData);
        TableColumn<String, String> col = new TableColumn<>("Numbrimärk");
        col.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue()));
        col.setPrefWidth(200);
        table.getColumns().add(col);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // otsing
        TextField searchField = new TextField();
        searchField.setPromptText("Otsi numbrimärki...");
        Label resultLabel = new Label();

        searchField.textProperty().addListener((obs, vana, uus) -> {
            String search = uus.trim().toLowerCase();
            filteredData.setPredicate(item -> item.toLowerCase().contains(search));
            if (search.isEmpty()) {
                resultLabel.setText("");
            } else if (allData.stream().anyMatch(s -> s.equalsIgnoreCase(search))) {
                resultLabel.setText("Numbrimärk kuulub firmale.");
            } else {
                resultLabel.setText("Numbrimärk puudub.");
            }
        });

        //Lisa/kustuta
        TextField newNumberField = new TextField();
        newNumberField.setPromptText("Uus numbrimärk");

        Button addButton = new Button("Lisa");
        Button removeButton = new Button("Kustuta");

        addButton.setOnAction(e -> {
            String uus = newNumberField.getText().trim().toUpperCase();
            if (!uus.isEmpty() && !allData.contains(uus)) {
                model.lisaNumbrimark(firma, uus);
                allData.add(uus);
                newNumberField.clear();
            }
        });

        removeButton.setOnAction(e -> {
            String valitud = table.getSelectionModel().getSelectedItem();
            if (valitud != null) {
                model.kustutaNumbrimark(firma, valitud);
                allData.remove(valitud);
            }
        });

        HBox lisaBox = new HBox(10, new Label("Numbrimärk:"), newNumberField, addButton, removeButton);
        lisaBox.setStyle("-fx-padding: 10; -fx-alignment: center-left;");

        VBox layout = new VBox(10, new Label("Otsing:"), searchField, table, lisaBox, resultLabel);
        layout.setStyle("-fx-padding: 15;");

        dialog.getDialogPane().setContent(layout);
        dialog.showAndWait();
    }
}
