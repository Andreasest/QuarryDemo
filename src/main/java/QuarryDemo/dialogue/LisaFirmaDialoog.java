package QuarryDemo.dialogue;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import QuarryDemo.service.MainViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LisaFirmaDialoog {

    private MainViewModel mainViewModel;

    private TextField firmaField = new TextField();
    private TextField esindajaField = new TextField();
    private TextField telefonField = new TextField();
    private TextField emailField = new TextField();
    private TextField aadressField = new TextField();
    private TextField maksetingimusField = new TextField("14");
    private TextField krediidilimiitField = new TextField("3000");
    private TextArea numbrimargidField = new TextArea();

    private Map<String, TextField> hinnadFields = new HashMap<>();

    public void show(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
        while (true) {

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Lisa uus firma");
            dialog.setHeaderText("Täida järgmised väljad:");
            //Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            //Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            //stage.getIcons().add(icon);

            // Nupud OK ja Cancel
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));

            GridPane grid1 = new GridPane();
            grid1.setHgap(10);
            grid1.setVgap(10);
            grid1.setPadding(new Insets(20, 20, 10, 10));

            int row = 0;
            grid.add(new Label("Kontaktandmed"){{setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");}}, 0, row++);

            grid.add(new Label("Firma:"), 0, row);
            grid.add(firmaField, 1, row++);

            grid.add(new Label("Esindaja:"), 0, row);
            grid.add(esindajaField, 1, row++);

            grid.add(new Label("Telefon:"), 0, row);
            grid.add(telefonField, 1, row++);

            grid.add(new Label("Email:"), 0, row);
            grid.add(emailField, 1, row++);

            grid.add(new Label("Aadress:"), 0, row);
            grid.add(aadressField, 1, row++);

            grid.add(new Label("Maksetingimus:"), 0, row);
            grid.add(maksetingimusField, 1, row++);

            grid.add(new Label("Krediidilimiit:"), 0, row);
            grid.add(krediidilimiitField, 1, row++);

            Label numbrimargidLabel = new Label("Numbrimärgid (komaga eraldatud):");
            numbrimargidLabel.setWrapText(true);
            grid.add(numbrimargidLabel, 0, row);
            grid.add(numbrimargidField, 1, row++);
            numbrimargidField.setMaxSize(200, 50);
            numbrimargidField.setStyle("-fx-font-size: 10px;");
            row=0;
            grid1.add(new Label("Hinnad (t)"){{setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");}},0,row++);
            List<String> mat=mainViewModel.getMaterjalid();
            for (int i = 0; i < mat.size(); i++) {
                String materjal = mat.get(i);
                Label label = new Label(materjal + ":");
                TextField hindField = new TextField();
                String hind;
                switch (materjal){
                    case "Liiv":{
                        hind="3";
                        break;
                    }
                    case "Sõelutud liiv":{
                        hind="5.5";
                        break;
                    }
                    case "Sõelutud muld":{
                        hind="7";
                        break;
                    }
                    default: hind="0";
                }
                hindField.setText(hind);
                hindField.setPromptText("Sisesta hind");
                hinnadFields.put(materjal, hindField);
                grid1.add(label, 0, row);
                grid1.add(hindField, 1, row++);
            }
            HBox hbox=new HBox(grid,grid1);
            dialog.getDialogPane().setContent(hbox);

            // nupu disable
            Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            // kontroll
            firmaField.textProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(newVal.trim().isEmpty());
            });

            Optional<ButtonType> tulemus = dialog.showAndWait();

            if (tulemus.isPresent() && tulemus.get() == ButtonType.OK) {
                if (!validateInput()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Viga");
                    alert.setHeaderText(null);
                    alert.setContentText("Palun täida väljad korrektselt.");
                    alert.showAndWait();
                    continue; // näita uuesti
                }
                saveData();
            }
            break;
        }
    }

    private boolean validateInput() {
        if (firmaField.getText().trim().isEmpty()) return false;

        int krediit = Integer.parseInt(krediidilimiitField.getText().trim());
        if (krediit < 0) return false;

        try {
            if (!krediidilimiitField.getText().trim().isEmpty()) {
                Integer.parseInt(krediidilimiitField.getText().trim());
            }
        } catch (NumberFormatException e) {
            return false;
        }

        for (TextField hindField : hinnadFields.values()) {
            String text = hindField.getText().trim();
            if (!text.isEmpty()) {
                try {
                    if (Double.parseDouble(text.replace(",",".")) < 0) return false;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return true;
    }

    private void saveData() {
        mainViewModel.addFirma(
                firmaField.getText().trim(),
                esindajaField.getText().trim(),
                telefonField.getText().trim(),
                emailField.getText().trim(),
                aadressField.getText().trim(),
                maksetingimusField.getText().trim(),
                krediidilimiitField.getText().trim(),
                numbrimargidField.getText().trim(),
                hinnadFieldsToMap()
        );
    }

    private Map<String, Double> hinnadFieldsToMap() {
        Map<String, Double> hinnad = new HashMap<>();
        for (Map.Entry<String, TextField> entry : hinnadFields.entrySet()) {
            String text = entry.getValue().getText().trim();
            if (!text.isEmpty()) {
                hinnad.put(entry.getKey(), Double.parseDouble(text));
            }
        }
        return hinnad;
    }
}