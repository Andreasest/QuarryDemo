package QuarryDemo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import QuarryDemo.service.SattedViewModel;

import java.io.File;

public class SattedViewController {

    @FXML private TextField arveField;
    @FXML private TextField autodeNimekiriField;


    private final SattedViewModel svm=new SattedViewModel();


    @FXML
    public void initialize(){
        String arveAsukoht=svm.getArveAsukoht();
        String autodeListiAsukoht=svm.getAutodeListiAsukoht();
        if (arveAsukoht.isEmpty()){
            arveAsukoht=System.getProperty("user.home")+"/Desktop";
        }
        if (autodeListiAsukoht.isEmpty()){
            autodeListiAsukoht=System.getProperty("user.home")+"/Desktop";
        }
        arveField.setText(arveAsukoht);
        autodeNimekiriField.setText(autodeListiAsukoht);
    }
    public void browseArveButtonAction(ActionEvent e){
        DirectoryChooser dc = new DirectoryChooser();
        File salvestuskoht = dc.showDialog(arveField.getScene().getWindow());

        if (salvestuskoht != null) {
            svm.updateArveAsukoht(salvestuskoht.toString().replace('\\','/'));
            arveField.setText(salvestuskoht.toString());
        }
    }
    public void browseAutodeNimekiriButtonAction(ActionEvent e){
        DirectoryChooser dc = new DirectoryChooser();
        File salvestuskoht = dc.showDialog(autodeNimekiriField.getScene().getWindow());

        if (salvestuskoht != null) {
            svm.updateAutodeListiAsukoht(salvestuskoht.toString().replace('\\','/'));
            autodeNimekiriField.setText(salvestuskoht.toString());
        }
    }
}
