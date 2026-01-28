package QuarryDemo.dialogue;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import QuarryDemo.service.MainViewModel;

import java.io.IOException;

public class TahtajadDialoog {

    private MainViewModel mainViewModel;

    public TahtajadDialoog(MainViewModel mainViewModel){
        this.mainViewModel = mainViewModel;
    }

    public void show(Stage owner){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TahtajadView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Arvete maksetähtajad");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.setScene(new Scene(root));
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}