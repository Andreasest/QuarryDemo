package QuarryDemo.service;

import QuarryDemo.model.MaterjaliSummaRida;
import QuarryDemo.repository.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MüügiKäiveViewModel {
    private final Connection connection;

    public MüügiKäiveViewModel(){
        this.connection = DbConnection.getConnection();
        if (connection==null) System.exit(1);
    }
    public Map<String, List<MaterjaliSummaRida>> getMüügiKäive(){
        Map<String, List<MaterjaliSummaRida>> müügiKäive=new LinkedHashMap<>();
        String sql="SELECT  " +
                "    strftime('%Y-%m', a.kuupaev) AS kuu, " +
                "    r.materjal, " +
                "    SUM(r.kogus * r.hind) AS summa " +
                "FROM arved a " +
                "JOIN arve_read r ON a.arvenumber = r.arvenumber " +
                "GROUP BY kuu, r.materjal " +
                "ORDER BY kuu DESC, r.materjal;";
        try (PreparedStatement pstmt=connection.prepareStatement(sql)){
            ResultSet rs=pstmt.executeQuery();
            while (rs.next()){
                String kuu=rs.getString("kuu");
                String materjal=rs.getString("materjal");
                double summa= rs.getDouble("summa");
                List<MaterjaliSummaRida> list=müügiKäive.computeIfAbsent(kuu, k -> new ArrayList<>());
                list.add(new MaterjaliSummaRida(materjal,summa));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return müügiKäive;
    }

}
