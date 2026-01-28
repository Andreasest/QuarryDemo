package QuarryDemo.model;

import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import QuarryDemo.service.MainViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Arve {
    private List<Teenus> teenused; // kujul[Liiv - perioodil 01.06.2025 - 16.07.2025|t|326,50|2.70|881.55]
    private List<String[]> kogused; // kujul [2025-06-04, 123ABC, 28, Liiv]
    private Firma firma;
    private int arvenumber;
    private LocalDate algus;
    private LocalDate lõpp;


    public Arve(String firma, LocalDate startDate, LocalDate endDate, MainViewModel mainViewModel) {
        this.teenused = mainViewModel.getAndmedArvele(firma, startDate, endDate);
        this.kogused = mainViewModel.getAndmedAutodeListile(firma, startDate, endDate);
        this.firma = mainViewModel.getFirmaKontakt(firma);
        this.algus=startDate;
        this.lõpp=endDate;
        setArvenumber(mainViewModel);
    }


    public LocalDate getMakseTähtaeg(){
        LocalDate viimanePäev=lõpp.withDayOfMonth(lõpp.getMonth().length(lõpp.isLeapYear()));
        int päev=lõpp.getDayOfWeek().getValue();
        LocalDate arveKuupäev=lõpp.plusDays(7-päev+1);
        if (arveKuupäev.isAfter(viimanePäev)){
            return lõpp;
        }
        return arveKuupäev;
    }

    public String getSumma() {
        double summa = 0.0;
        for (Teenus teenus : teenused) {
            summa += teenus.getSumma();
        }
        return String.format(Locale.US, "%.2f", summa);
    }

    public String getPeriood(){
        return algus.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))+" - "+
                lõpp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public List<Teenus> getTeenused() {
        return teenused;
    }

    public List<String[]> getKogused() {
        return kogused;
    }

    public Firma getFirma() {
        return firma;
    }

    public int getArvenumber() {
        return arvenumber;
    }

    public void setArvenumber(MainViewModel mvm) {
        boolean viga = false;

        while (true) {
            int arvenr = mvm.getArvenumber();
            while (true){
                if(mvm.arveEksisteerib(arvenr)==0) break;
                else{
                    arvenr++;
                }
            }
            TextInputDialog td = new TextInputDialog("" + arvenr);
            if (viga) {
                td.setHeaderText("Selline arvenumber juba eksisteerib või on vigane, vali uus");
            } else {
                td.setHeaderText("Arvenumber");
            }
            td.getDialogPane().getButtonTypes().remove(ButtonType.CANCEL);
            var result=td.showAndWait();
            if (result.isEmpty()){
                throw new RuntimeException("Programm ei saanud arvenumbri tulemust kätte.");
            }

            String uus = td.getEditor().getText();
            int uusArveNr;
            try {
                uusArveNr = Integer.parseInt(uus);
            } catch (NumberFormatException e) {
                uusArveNr = arvenr;
                continue;
            }

            int eksisteerib = mvm.arveEksisteerib(uusArveNr);
            if (eksisteerib == 0) {
                this.arvenumber = uusArveNr;
                break;
            } else {
                viga = true;
            }
        }

        mvm.setArveNumber(arvenumber);
    }
    public LocalDate getLõpp(){
        return this.lõpp;
    }

}
