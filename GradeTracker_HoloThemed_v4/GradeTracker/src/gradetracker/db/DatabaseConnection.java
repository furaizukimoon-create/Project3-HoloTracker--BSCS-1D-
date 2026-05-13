package gradetracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — Singleton JDBC connection manager.
 * Connects to grade_tracker_db on localhost via XAMPP MySQL.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/grade_tracker_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";           // default XAMPP root has no password

    private static Connection instance = null;

    private DatabaseConnection() {}

    /**
     * Returns a single shared Connection. Creates one if it doesn't exist or was closed.
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (instance == null || instance.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Make sure mysql-connector-j.jar is in your lib/ folder.", e);
        }
        return instance;
    }

    /** Call this when the application closes. */
    public static void closeConnection() {
        if (instance != null) {
            try { instance.close(); } catch (SQLException ignored) {}
        }
    }
}
