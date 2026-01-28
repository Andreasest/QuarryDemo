package QuarryDemo.dialogue;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Modality;
import QuarryDemo.service.MainViewModel;

import java.io.IOException;

public class MüügiKäiveDialoog {
    private MainViewModel mainViewModel;

    public MüügiKäiveDialoog(MainViewModel mainViewModel){
        this.mainViewModel = mainViewModel;
    }

    public void show(Stage owner){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MüügikäiveView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Müügikäive kuulõikes");
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