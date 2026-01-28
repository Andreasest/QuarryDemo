package QuarryDemo.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import QuarryDemo.model.Tahtaeg;
import QuarryDemo.repository.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class TahtajadViewModel {
    private Connection connection;

    public TahtajadViewModel() {
        this.connection = DbConnection.getConnection();
        if (connection==null) System.exit(1);
    }

    public ObservableList<Tahtaeg> getTahtajad(){
        ObservableList<Tahtaeg> tahtajad= FXCollections.observableArrayList();
        String sql="SELECT * FROM arved WHERE makstud=0; ";
        try (PreparedStatement pstmt=connection.prepareStatement(sql)){
            ResultSet rs=pstmt.executeQuery();
            while (rs.next()){
                String kuupäev= String.valueOf(LocalDate.parse(rs.getString("kuupaev")).plusDays(rs.getInt("maksetingimus")));
                tahtajad.add(new Tahtaeg(rs.getInt("arvenumber"),
                                        rs.getString("firma"),
                                        kuupäev,
                                        rs.getDouble("summa")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tahtajad;
    }
    public void MakstudTahtaeg(int arvenumber){
        String sql=("UPDATE arved SET makstud=1 WHERE arvenumber=?; ");
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setInt(1,arvenumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void kustutaTahtaeg(int arvenumber){
        String sql=("DELETE FROM arved WHERE arvenumber=?; ");
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setInt(1,arvenumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
