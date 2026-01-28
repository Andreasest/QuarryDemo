package QuarryDemo.dialogue;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import QuarryDemo.service.MainViewModel;

import java.util.Optional;

public class LisaMaterjalDialoog {
    private MainViewModel mainViewModel;

    public void show(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
        while (true){
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Lisa materjal");
            dialog.setHeaderText("Täida järgmised väljad:");
            //Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            //Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            //stage.getIcons().add(icon);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));

            grid.add(new Label("Materjal"),0,0);
            grid.add(new Label("Hind (t)"),0,1);
            TextField materjal=new TextField();
            TextField hind=new TextField("0");
            grid.add(materjal,1,0);
            grid.add(hind,1,1);

            dialog.getDialogPane().setContent(grid);

            Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);
            materjal.textProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(newVal.trim().isEmpty());
            });

            hind.textProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(newVal.trim().isEmpty());
            });

            Optional<ButtonType> tulemus = dialog.showAndWait();
            if (tulemus.isPresent() && tulemus.get() == ButtonType.OK) {
                if (!validateInput(materjal,hind)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Viga");
                    alert.setHeaderText(null);
                    alert.setContentText("Palun täida väljad korrektselt.");
                    alert.showAndWait();
                    continue;
                }
                String formMat=materjal.getText().toLowerCase();
                mainViewModel.addMaterjal((formMat.substring(0, 1).toUpperCase() + formMat.substring(1)),Double.parseDouble(hind.getText().replace(",",".")));
            }
            break;
        }
    }

    private boolean validateInput(TextField materjal, TextField hind) {
        if (materjal.getText().trim().isEmpty()) return false;
        if (hind.getText().trim().isEmpty()) return false;
        try {
            Double.parseDouble(hind.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
