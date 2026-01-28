package QuarryDemo.dialogue;

import QuarryDemo.service.MainViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SattedDialoog {

    private MainViewModel mainViewModel;

    public SattedDialoog(MainViewModel mainViewModel){
        this.mainViewModel = mainViewModel;
    }

    public void show(Stage owner){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SattedView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Sätted");
            //stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.setScene(new Scene(root));
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}