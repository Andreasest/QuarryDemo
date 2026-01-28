package QuarryDemo.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import QuarryDemo.model.Arve;
import QuarryDemo.model.Firma;
import QuarryDemo.service.MainViewModel;
import QuarryDemo.util.InvoiceGen;
import QuarryDemo.util.NumbrimargidExporter;
import QuarryDemo.dialogue.*;
import QuarryDemo.model.Tabel;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    public MainViewModel mainViewModel =new MainViewModel();
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private String firma;

    @FXML private ListView<String> firmadList;
    @FXML private TextField numberplateBox;
    @FXML private DatePicker dateBox;
    @FXML private TextField amountBox;
    @FXML private ComboBox materialCombo;
    @FXML private TextField multiplierBox;
    @FXML private Button inputButton;
    @FXML private TableView<Tabel> tableView;
    @FXML private Label firmaLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private TableColumn<Tabel, String> kuupaevColumn;
    @FXML private TableColumn<Tabel, String> numbrimarkColumn;
    @FXML private TableColumn<Tabel, String> materjalColumn;
    @FXML private TableColumn<Tabel, Double> kogusColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Ühendus ja firmade list
        if (mainViewModel.isDbConnected()){
            firmadList.setItems(FXCollections.observableArrayList(mainViewModel.getFirmad()));
        }
        //Tabel
        tableView.setPlaceholder(new Label("Tabelis pole andmeid"));
        kuupaevColumn.setCellValueFactory(new PropertyValueFactory<>("kuupaev"));
        numbrimarkColumn.setCellValueFactory(new PropertyValueFactory<>("numbrimark"));
        materjalColumn.setCellValueFactory(new PropertyValueFactory<>("materjal"));
        kogusColumn.setCellValueFactory(new PropertyValueFactory<>("kogus"));
        kogusColumn.setCellFactory(column-> new TableCell<Tabel, Double>(){
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item == Math.floor(item)) {
                        setText(String.format("%.0f", item));
                    } else {
                        setText(String.valueOf(item));
                    }
                }
            }
        });
        tableView.setOnKeyPressed(e->{
            if (e.getCode()== KeyCode.DELETE){
                Tabel rida=tableView.getSelectionModel().getSelectedItem();
                if (rida!=null){
                    mainViewModel.kustutaRida(rida.getId());
                    uuendaTabel();
                }else {
                    Alert alert=new Alert(Alert.AlertType.ERROR,"Vali enne kirje");
                    alert.showAndWait();
                }
            }
        });

        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.valueProperty().addListener((obs, vana, uus) -> uuendaTabel());
        endDatePicker.valueProperty().addListener((obs, vana, uus) -> uuendaTabel());

        // Valitud firma
        firmadList.getSelectionModel().selectedItemProperty().addListener((obs, vanaValik, uusValik) -> {
            if (uusValik != null) firma = uusValik;
            uuendaTabel();
            firmaLabel.setText(firma);
        });
        if (!firmadList.getItems().isEmpty()) {
            firmadList.getSelectionModel().selectFirst();
            firma = firmadList.getSelectionModel().getSelectedItem();
            uuendaTabel();
        }

        //Automaatne kuupäev
        dateBox.setValue(LocalDate.now());
        //Materialid
        materialCombo.setItems(mainViewModel.getMaterjalid());
        materialCombo.getSelectionModel().selectFirst();
    }

    public void contactButtonAction(ActionEvent e){
        Firma kontakt= mainViewModel.getFirmaKontakt(firma);
        if (kontakt!=null){
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Kontakt");
            alert.setHeaderText(kontakt.getName());
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            TextArea textArea = new TextArea(
                    "Esindaja: "+kontakt.getSpokesperson() +
                            "\nE-post: " +kontakt.getEmail() +
                            "\nTelefon: "+kontakt.getPhone()
            );
            textArea.setEditable(false);
            textArea.setWrapText(true);
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Kontakti ei leitud!");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    public void priceButtonAction(ActionEvent e){
        HinnadDialoog hd=new HinnadDialoog(mainViewModel,firma,startDatePicker.getValue(),endDatePicker.getValue());
        hd.show();

    }

    public void numberplateButtonAction(ActionEvent e){
        NumbrimargidDialoog nd=new NumbrimargidDialoog(mainViewModel,firma);
        nd.show();
    }

    public void billButtonAction(ActionEvent e){
        boolean arveSuccessful=false;
        boolean autodeListSuccessful=false;
        try{
            Arve arve=new Arve(firma,startDatePicker.getValue(), endDatePicker.getValue(), mainViewModel);
            arveSuccessful=InvoiceGen.createInvoice(arve, mainViewModel.getArveAsukoht());
            autodeListSuccessful=InvoiceGen.createCarList(arve, mainViewModel.getAutodeListiAsukoht());
            mainViewModel.addArve(arve);
        } catch (RuntimeException ex) {
            System.out.println("Arve loomine katkestati: "+ex.getMessage());
            ex.printStackTrace();
        }
        if (!arveSuccessful||!autodeListSuccessful){
            showAlert(Alert.AlertType.ERROR,"Viga","Arve loomine ebaõnnestus");
        } else{
            showAlert(Alert.AlertType.INFORMATION,"Õnnestus","Arve loomine õnnestus");
        }

    }

    public void inputButtonAction(ActionEvent e){
        String firma=this.firma;
        String kuupaev=dateBox.getValue().toString();
        String numbrimark=numberplateBox.getText().toUpperCase();
        String materjal= (String) materialCombo.getValue();
        double kogus;
        int kordused;
        // numbrimärk
        if (numbrimark.isEmpty()){
            showAlert(Alert.AlertType.ERROR,"Viga","Palun sisesta numbrimärk.");
            return;
        }
        // kogus
        try{
            kogus=Double.parseDouble(amountBox.getText().replace(",","."));
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR,"Viga","Palun sisesta kogus numbrina.");
            return;
        }
        // kordused
        try{
            kordused=Integer.parseInt(multiplierBox.getText());
            if (kordused<1) kordused=1;
        } catch (NumberFormatException ex){
            kordused=1;
        }

        boolean successful=true;
        for (int i = 0; i < kordused; i++) {
            boolean added= mainViewModel.lisaArve(firma,kuupaev,numbrimark,materjal,kogus);
            if (!added) {
                successful=false;
                break;
            }
        }
        // kontroll
        if (successful){
            uuendaTabel();
            numberplateBox.setText("");
            amountBox.setText("");
            multiplierBox.setText("");
        } else {
            showAlert(Alert.AlertType.ERROR,"Viga","Andmete lisamine ebaõnnestus.");
        }
    }

    public void addCompanyAction (ActionEvent e){
        new LisaFirmaDialoog().show(mainViewModel);
        firmadList.setItems(FXCollections.observableArrayList(mainViewModel.getFirmad()));
    }

    public void showAllnumberplatesAction (ActionEvent e){
        NumbrimargidExporter.eksportiNumbrimargidExcel(mainViewModel.getConnection(),"numbrimargid.xlsx");
    }

    public void showTonStatisticsAction (ActionEvent e){
        new StatistikaDialoog().show(mainViewModel);
    }

    public void addMaterialAction(ActionEvent e){
        new LisaMaterjalDialoog().show(mainViewModel);
    }

    public void tahtajadButtonAction(ActionEvent e){
        new TahtajadDialoog(mainViewModel).show(primaryStage);
    }

    public void showMüügiKäivection(ActionEvent e){
        new MüügiKäiveDialoog(mainViewModel).show(primaryStage);
    }

    public void settingsAction(ActionEvent e){
        new SattedDialoog(mainViewModel).show(primaryStage);
    }

    //Abimeetodid
    private void uuendaTabel() {
        if (firma != null && startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            tableView.setItems(mainViewModel.getAndmedFirmale(firma, startDatePicker.getValue(), endDatePicker.getValue()));
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
