package QuarryDemo.repository;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnection {
    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");

            String dbPath = System.getProperty("db.path");
            return DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}