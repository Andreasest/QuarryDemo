package QuarryDemo.service;

import QuarryDemo.repository.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SattedViewModel {
    private final Connection connection;

    public SattedViewModel() {
        this.connection = DbConnection.getConnection();
        if (connection==null) System.exit(1);
    }

    public void updateAutodeListiAsukoht(String asukoht) {
        String sql="UPDATE eelistused SET value=? WHERE key='autode_nimekirja_salvestuskoht'; ";
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setString(1,asukoht);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateArveAsukoht(String asukoht) {
        String sql="UPDATE eelistused SET value=? WHERE key='arve_salvestuskoht'; ";
        try (PreparedStatement pstmt= connection.prepareStatement(sql)){
            pstmt.setString(1,asukoht);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        return vastus;
    }
}
