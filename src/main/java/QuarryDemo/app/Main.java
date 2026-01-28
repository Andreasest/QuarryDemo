package QuarryDemo.app;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import QuarryDemo.controller.MainViewController;
import QuarryDemo.util.Backup;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Main extends Application {
    private static final String DB_NAME = "andmebaas.db";
    private static final String APP_NAME = "QuarryDemo";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // andmebaasi haldus
        String appData=System.getenv("APPDATA");
        Path kasutajaKaust = Paths.get(appData, APP_NAME);
        Path sihtfail = kasutajaKaust.resolve(DB_NAME);
        if (Files.notExists(sihtfail)) {
            Files.createDirectories(kasutajaKaust);

            try (InputStream sisend = getClass().getResourceAsStream("/db/andmebaas.db")) {
                if (sisend == null) {
                    throw new RuntimeException("Andmebaasifaili ei leitud JAR seest");
                }
                Files.copy(sisend, sihtfail, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Andmebaas kopeeritud: " + sihtfail);
            }
        } else {
            System.out.println("Andmebaas juba olemas: " + sihtfail);
        }

        //varukoopia
        System.setProperty("db.path", sihtfail.toString());
        //Backup.teeVarukoopia(5);


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();

        MainViewController controller= loader.getController();
        controller.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        primaryStage.setTitle(APP_NAME);
        primaryStage.setMinWidth(720);
        primaryStage.setMinHeight(540);
        primaryStage.show();
    }
}
