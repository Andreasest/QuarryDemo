package QuarryDemo.dialogue;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import QuarryDemo.service.MainViewModel;
import QuarryDemo.util.NumberSõnadeks;

import java.util.*;

public class StatistikaDialoog {
    public void show(MainViewModel mainViewModel){
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Müügi statistika");
        dialog.setHeaderText("Müügi kogused kuude lõikes");
        //Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        //Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")));
        //stage.getIcons().add(icon);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        //Checkboxid
        List<String> mat=mainViewModel.getMaterjalid();
        GridPane grid=new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        int row=0;
        int column=0;
        Map<String, Map<String, Double>> kuuMaterjalid=mainViewModel.importStatistika();

        Map<String, TableColumn<KuuAndmed,?>> materjaliVeerud=new HashMap<>();

        // Tabel
        TableView<KuuAndmed> tabel = new TableView<>();
        TableColumn<KuuAndmed, String> kuuCol = new TableColumn<>("Kuu");
        kuuCol.setCellValueFactory(data -> data.getValue().kuuProperty());
        kuuCol.setPrefWidth(100);
        tabel.getColumns().add(kuuCol);

        for (String materjal : mat) {
            TableColumn<KuuAndmed, String> col = new TableColumn<>(materjal+" (t)");
            col.setCellValueFactory(data -> data.getValue().getKogusProperty(materjal));
            col.setPrefWidth(100);
            materjaliVeerud.put(materjal,col);
        }

        TableColumn<KuuAndmed, String> kokkuCol = new TableColumn<>("Kokku liiva (t)");
        kokkuCol.setCellValueFactory(data -> data.getValue().kokkuProperty());
        kokkuCol.setPrefWidth(100);
        tabel.getColumns().add(kokkuCol);

        for (String s : mat) {
            if (row > 2) {
                column += 2;
                row = 0;
            }
            CheckBox cb=new CheckBox(s);
            if (s.equals("Liiv") || s.equals("Sõelutud liiv")) {
                cb.setSelected(true);
                tabel.getColumns().add(tabel.getColumns().size() - 1, materjaliVeerud.get(s)); // enne kokkuCol
            }
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                TableColumn<KuuAndmed, ?> col = materjaliVeerud.get(s);
                if (newVal) {
                    tabel.getColumns().add(tabel.getColumns().size() - 1, col); // enne 'kokkuCol'i
                } else {
                    tabel.getColumns().remove(col);
                }
            });
            grid.add(cb,column,row++);
        }
        root.getChildren().addAll(grid,tabel);
        List<KuuAndmed> tabeliAndmed = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : kuuMaterjalid.entrySet()) {
            tabeliAndmed.add(new KuuAndmed(entry.getKey(), entry.getValue(), mat));
        }
        tabel.getItems().addAll(tabeliAndmed);
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }


    public static class KuuAndmed {
        private final StringProperty kuu;
        private final Map<String, StringProperty> kogused = new HashMap<>();
        private final StringProperty kokku = new SimpleStringProperty();
        private final Set<String> liivaMaterjalid = Set.of(
                "Liiv", "Sõelutud liiv", "Mittestandartne liiv"
        );

        public KuuAndmed(String kuu, Map<String, Double> materjalidKogused, List<String> materjalid) {
            String[] kuuOsad=kuu.split("-");
            int kuuNumber=Integer.parseInt(kuuOsad[1]);
            String kuuSõna="";
            switch (kuuNumber){
                case 1: {
                    kuuSõna=" Jaanuar";
                    break;
                }
                case 2: {
                    kuuSõna=" Veebruar";
                    break;
                }
                case 3: {
                    kuuSõna=" Märts";
                    break;
                }
                case 4: {
                    kuuSõna=" Aprill";
                    break;
                }
                case 5: {
                    kuuSõna=" Mai";
                    break;
                }
                case 6: {
                    kuuSõna=" Juuni";
                    break;
                }
                case 7: {
                    kuuSõna=" Juuli";
                    break;
                }
                case 8: {
                    kuuSõna=" August";
                    break;
                }
                case 9: {
                    kuuSõna=" September";
                    break;
                }
                case 10: {
                    kuuSõna=" Oktoober";
                    break;
                }
                case 11: {
                    kuuSõna=" November";
                    break;
                }
                case 12: {
                    kuuSõna=" Detsember";
                    break;
                }
            }
            this.kuu = new SimpleStringProperty(kuuOsad[0]+kuuSõna);

            double sum = 0;
            for (String m : materjalid) {
                Double v = materjalidKogused.getOrDefault(m, 0.0);
                kogused.put(m, new SimpleStringProperty(NumberSõnadeks.formatDouble(v)));
                //Liivaarvutus
                if (m.toUpperCase().contains("LIIV")) sum += v;
            }
            kokku.set(NumberSõnadeks.formatDouble(sum));
        }

        public StringProperty kuuProperty() {

            return kuu;
        }

        public StringProperty getKogusProperty(String materjal) {
            return kogused.getOrDefault(materjal, new SimpleStringProperty("0.00"));
        }

        public StringProperty kokkuProperty() {
            return kokku;
        }
    }
}