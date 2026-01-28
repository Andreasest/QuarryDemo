package QuarryDemo.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import QuarryDemo.model.Arve;
import QuarryDemo.model.Firma;
import QuarryDemo.model.Tabel;
import QuarryDemo.model.Teenus;
import QuarryDemo.repository.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainViewModel {
    private Connection connection;
    private final String järjekord="order by " +
            " case materjal " +
            "  when 'Liiv' then 1 " +
            "  when 'Sõelutud liiv (0/4)' then 2 " +
            "  when 'Sõelutud liiv (0/8)' then 3 " +
            "  when'Mittestandartne liiv' then 4"+
            "  when 'Sõelutud täitepinnas' then 5 " +
            "  when 'Sõelumata täitepinnas' then 6 "+
            "  when 'Täitepinnas' then 7 "+
            "  when 'Purubetoon' then 8 "+
            "  when 'Purukruus' then 9 "+
            "  else 10 " +
            "end; ";

    public Connection getConnection() {
        return connection;
    }

    public MainViewModel() {
        this.connection = DbConnection.getConnection();
        if (connection==null) System.exit(1);
    }

    public Boolean isDbConnected(){
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getFirmad(){
        List<String> firmad=new ArrayList<>();
        try{
            String firmadQuery="SELECT DISTINCT firma FROM kontakt " +
                    " order by case when lower(firma)='arveta klient' then 1 else 0 end, " +
                    "lower(firma) collate NOCASE; ";
            PreparedStatement stmnt=connection.prepareStatement(firmadQuery);
            ResultSet rs= stmnt.executeQuery();
            while (rs.next()) firmad.add(rs.getString("firma"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return firmad;
    }

    public Firma getFirmaKontakt(String firmaNimi) {
        String sql = "SELECT * FROM Kontakt WHERE firma = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firmaNimi);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Firma(
                        rs.getString("firma"),
                        rs.getString("esindaja"),
                        rs.getString("email"),
                        rs.getString("telefon"),
                        rs.getString("aadress"),
                        rs.getInt("maksetingimus")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ObservableList<String> getMaterjalid(){
        String sql="select materjal " +
                "from hinnad  " +
                "group by materjal " +järjekord;
        ObservableList<String> materjalid=FXCollections.observableArrayList();
        try(PreparedStatement pstmt= connection.prepareStatement(sql)){
            ResultSet rs=pstmt.executeQuery();
            while (rs.next()){
                materjalid.add(rs.getString("materjal"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materjalid;
    }

    public Map<String, Double> getHinnadFirmale(String firmaNimi){
        Map<String, Double> hinnad=new LinkedHashMap<>();
        String sql="SELECT materjal, hind FROM hinnad where firma = ? " + järjekord;
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setString(1,firmaNimi);
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()){
                hinnad.put(rs.getString("materjal"), rs.getDouble("hind"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hinnad;
    }

    public void setHinnadFirmale(String firma, String materjal, double hind) {
        String sql = "INSERT INTO hinnad (firma, materjal, hind) VALUES (?, ?, ?) " +
                "ON CONFLICT(firma, materjal) DO UPDATE SET hind=excluded.hind;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firma);
            pstmt.setString(2, materjal);
            pstmt.setDouble(3, hind);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Tabel> getAndmedFirmale(String firma, LocalDate algus, LocalDate lõpp){
        ObservableList<Tabel> data= FXCollections.observableArrayList();

        String sql="SELECT * FROM saatelehed WHERE firma = ? " +
                "AND kuupaev BETWEEN ? AND ?" +
                " ORDER BY kuupaev DESC, id DESC, numbrimark, materjal;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, firma);
            pstmt.setString(2, algus.toString());
            pstmt.setString(3, lõpp.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                data.add(new Tabel(
                        rs.getInt("id"),
                        kuupaevConverter(rs.getString("kuupaev")),
                        rs.getString("numbrimark"),
                        rs.getString("materjal"),
                        rs.getDouble("kogus")
                ));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }
    public String kuupaevConverter(String kuupäev){
        String[] osad=kuupäev.split("-");
        return osad[2]+"."+osad[1]+"."+osad[0];
    }

    public List<Teenus> getAndmedArvele(String firma, LocalDate algus, LocalDate lõpp){
        List<Teenus> teenused= new ArrayList<>();

            String sql1="SELECT a.materjal,SUM(kogus) AS summa_kogus, h.hind " +
                    "FROM saatelehed a " +
                    "JOIN hinnad h ON a.firma=h.firma AND a.materjal=h.materjal "+
                    "WHERE a.firma = ? " +
                    "AND kuupaev BETWEEN ? AND ? "+
                    "GROUP BY a.materjal, h.hind; ";
            try(PreparedStatement pstmt= connection.prepareStatement(sql1)){
                pstmt.setString(1, firma);
                pstmt.setString(2, algus.toString());
                pstmt.setString(3, lõpp.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String materjal=rs.getString("materjal");
                    Double kogus=rs.getDouble("summa_kogus");
                    String kogusForm;
                    if (kogus == Math.floor(kogus)) {
                        kogusForm = String.format("%.0f", kogus); // täisarv
                    } else {
                        kogusForm = String.format("%.2f", kogus); // kuni 2 kohta
                    }
                    Double ühikuHind=rs.getDouble("hind");
                    teenused.add(new Teenus(materjal,algus.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))+" - "+
                            lõpp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),"t",kogus,ühikuHind));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        return teenused;
    }

    public List<String[]> getAndmedAutodeListile(String firma, LocalDate algus, LocalDate lõpp){
        List<String[]> kogused= new ArrayList<>();

        String sql1="SELECT kuupaev, numbrimark, sum(kogus) AS summa, materjal " +
                "FROM saatelehed " +
                "WHERE firma = ? " +
                "AND kuupaev BETWEEN ? AND ? " +
                "GROUP BY kuupaev,numbrimark, materjal " +
                "ORDER BY kuupaev ASC; ";
        try(PreparedStatement pstmt= connection.prepareStatement(sql1)){
            pstmt.setString(1, firma);
            pstmt.setString(2, algus.toString());
            pstmt.setString(3, lõpp.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String materjal=rs.getString("materjal");
                Double summa=rs.getDouble("summa");
                String kogusForm;
                if (summa == Math.floor(summa)) {
                    kogusForm = String.format("%.0f", summa); // täisarv
                } else {
                    kogusForm = String.format("%.2f", summa); // kuni 2 kohta
                }
                String numbrimark=rs.getString("numbrimark");
                String kuupaev=rs.getString("kuupaev");
                kogused.add(new String[]{kuupaev, numbrimark, kogusForm, materjal});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return kogused;
    }

    public boolean lisaArve(String firma, String kuupaev, String numbrimark, String materjal, double kogus){
        String sql ="INSERT INTO saatelehed (firma, kuupaev, numbrimark, materjal, kogus)" +
                "VALUES (?,?,?,?,?);";
        try(PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setString(1,firma);
            pstmt.setString(2,kuupaev);
            pstmt.setString(3,numbrimark);
            pstmt.setString(4,materjal);
            pstmt.setDouble(5,kogus);
            return pstmt.executeUpdate()>0;
        } catch (SQLException e) {
            System.err.println("Andmete lisamisel tekkis viga: "+e.getMessage());
            return false;
        }
    }

    public int getArvenumber(){
        String sql="SELECT key,value FROM eelistused WHERE key='viimane_arvenumber'; ";
        int arvenumber=0;
        try (PreparedStatement pstmt=connection.prepareStatement(sql)){
            ResultSet rs=pstmt.executeQuery();
            arvenumber= rs.getInt("value");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return arvenumber;
    }

    public void setArveNumber(int number){
        String sql1="UPDATE eelistused SET value= ? WHERE key='viimane_arvenumber'; ";
        try(PreparedStatement pstmt1= connection.prepareStatement(sql1)){
            pstmt1.setInt(1,number+1);
            pstmt1.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getKogusFirmale(String firma, String materjal, LocalDate algKuupaev, LocalDate loppKuupaev) {
        String sql="SELECT materjal, sum(kogus) AS kokku FROM saatelehed " +
                "WHERE firma=? AND materjal=? " +
                "AND kuupaev BETWEEN ? AND ?" +
                "GROUP BY materjal;";
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setString(1,firma);
            pstmt.setString(2,materjal);
            pstmt.setString(3,algKuupaev.toString());
            pstmt.setString(4,loppKuupaev.toString());
            ResultSet rs= pstmt.executeQuery();
            double kokku=rs.getDouble("kokku");
            if (kokku == Math.floor(kokku)) {
                return String.format("%.0f", kokku);
            } else {
                return String.format("%.2f", kokku);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getKrediitFirmale(String firma) {
        String sql="SELECT krediidilimiit FROM kontakt " +
                "WHERE firma=?";
        try (PreparedStatement pstmt=connection.prepareStatement(sql)){
            pstmt.setString(1,firma);
            ResultSet rs= pstmt.executeQuery();
            return rs.getInt("krediidilimiit");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setKrediitFirmale(String firma, int uusKrediit) {
        String sql="UPDATE kontakt SET krediidilimiit=? WHERE firma=?;";
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setInt(1,uusKrediit);
            pstmt.setString(2,firma);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getNumbrimargidFirmale(String firma) {
        String sql="SELECT numbrimark FROM numbrimargid WHERE firma=?";
        List<String> numbrimärgid=new ArrayList<>();
        try(PreparedStatement pstmt= connection.prepareStatement(sql)) {
            pstmt.setString(1,firma);
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()){
                numbrimärgid.add(rs.getString("numbrimark"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numbrimärgid;
    }

    public void lisaNumbrimark(String firma, String uus) {
        String sql="INSERT INTO numbrimargid(firma, numbrimark) " +
                "VALUES (?,?)";
        try (PreparedStatement pstmt=connection.prepareStatement(sql)){
            pstmt.setString(1,firma);
            pstmt.setString(2,uus);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void kustutaNumbrimark(String firma, String valitud) {
        String sql="DELETE FROM numbrimargid WHERE firma=? and numbrimark=?; ";
        try (PreparedStatement pstmt=connection.prepareStatement(sql)){
            pstmt.setString(1,firma);
            pstmt.setString(2,valitud);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addFirma(String firma, String esindaja, String telefon, String email, String aadress, String maksetingimus, String krediidilimiit, String numbrimargid, Map<String, Double> hinnad) {
        //Kontakt
        String sql="INSERT INTO Kontakt(firma,esindaja,telefon,email,aadress,maksetingimus,krediidilimiit) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?) ";
        try(PreparedStatement pstmt= connection.prepareStatement(sql)) {
            pstmt.setString(1,firma);
            pstmt.setString(2,esindaja);
            pstmt.setString(3,telefon);
            pstmt.setString(4,email);
            pstmt.setString(5,aadress);
            pstmt.setString(6,maksetingimus);
            pstmt.setString(7,krediidilimiit);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Numbrimärgid
        if (!numbrimargid.isBlank()) {
            String sql1 = "INSERT INTO numbrimargid(firma,numbrimark) " +
                    "VALUES (?, ?);";
            String[] nrmargid = numbrimargid.split(",");
            for (int i = 0; i < nrmargid.length; i++) {
                try (PreparedStatement pstmt = connection.prepareStatement(sql1)) {
                    pstmt.setString(1, firma);
                    pstmt.setString(2, nrmargid[i]);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        //Hinnad
        String sql2="INSERT INTO hinnad(firma,materjal,hind) VALUES (?, ?, ?); ";
        for (Map.Entry<String,Double> entry: hinnad.entrySet()){
            try (PreparedStatement pstmt= connection.prepareStatement(sql2)){
                pstmt.setString(1,firma);
                pstmt.setString(2, entry.getKey());
                pstmt.setDouble(3,entry.getValue());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Map<String, Double>> importStatistika() {
        Map<String, Map<String, Double>> kuuMaterjalid=new LinkedHashMap<>();
        String sql="SELECT " +
                "  strftime('%Y-%m', kuupaev) AS kuu, " +
                "  materjal, " +
                "  SUM(kogus) AS summa " +
                "FROM saatelehed " +
                "GROUP BY kuu, materjal " +
                "ORDER BY kuu; ";
        try(PreparedStatement pstmt= connection.prepareStatement(sql)){
            ResultSet rs=pstmt.executeQuery();
            kuuMaterjalid.clear();
            while (rs.next()){
                String kuu = rs.getString("kuu");
                String materjal = rs.getString("materjal");
                double summa = rs.getDouble("summa");
                kuuMaterjalid.putIfAbsent(kuu, new HashMap<>());
                kuuMaterjalid.get(kuu).put(materjal, summa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kuuMaterjalid;
    }

    public void kustutaRida(int id) {
        String sql="DELETE FROM saatelehed where id=?;";
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setInt(1,id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMaterjal(String materjal, double hind) {
        String sql="INSERT INTO HINNAD (firma, materjal, hind) VALUES (?,?,?); ";
        for (String firma:getFirmad()){
            try (PreparedStatement pstmt= connection.prepareStatement(sql)){
                pstmt.setString(1, firma);
                pstmt.setString(2, materjal);
                pstmt.setDouble(3, hind);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void addArve(Arve arve) {
        String sql="INSERT INTO arved (arvenumber, firma, kuupaev, maksetingimus, summa) VALUES (?,?,?,?,?); ";
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setInt(1,arve.getArvenumber());
            pstmt.setString(2,arve.getFirma().getName());
            pstmt.setString(3, String.valueOf(arve.getMakseTähtaeg()));
            pstmt.setInt(4,arve.getFirma().getMakseTingimus());
            pstmt.setDouble(5,Double.parseDouble(arve.getSumma()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql1="INSERT INTO arve_read (arvenumber, materjal, kogus, hind) VALUES (?, ?, ?, ?); ";
        List<Teenus> teenused=arve.getTeenused();
        for (Teenus teenus:teenused){
            try (PreparedStatement pstmt= connection.prepareStatement(sql1)){
                pstmt.setInt(1,arve.getArvenumber());
                pstmt.setString(2,teenus.getMaterjal());
                pstmt.setDouble(3, teenus.getKogus());
                pstmt.setDouble(4,teenus.getHind());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public int arveEksisteerib(int arvenumber){
        int vastus = 1;
        String sql="SELECT EXISTS(SELECT * FROM arved WHERE arvenumber=?) as eksisteerib; ";
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setInt(1,arvenumber);
            ResultSet rs=pstmt.executeQuery();
            while (rs.next()){
                vastus=rs.getInt("eksisteerib");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vastus;
    }

    public String getArveAsukoht() {
        String vastus = "";
        String sql="SELECT value FROM eelistused WHERE key='arve_salvestuskoht'; ";
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()){
                vastus=rs.getString("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (vastus.isEmpty()){
            vastus=System.getProperty("user.home")+"/Desktop";
        }
        return vastus;
    }
    public String getAutodeListiAsukoht() {
        String vastus = "";
        String sql="SELECT value FROM eelistused WHERE key='autode_nimekirja_salvestuskoht'; ";
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()){
                vastus=rs.getString("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (vastus.isEmpty()){
            vastus=System.getProperty("user.home")+"/Desktop";
        }
        return vastus;
    }
}
