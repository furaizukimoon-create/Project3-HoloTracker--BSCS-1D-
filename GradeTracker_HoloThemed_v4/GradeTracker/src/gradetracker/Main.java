package gradetracker;

import gradetracker.db.DatabaseConnection;
import gradetracker.ui.LoginScreen;
import gradetracker.util.HoloTheme;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Main — Application entry point.
 *
 * Run this class to start HoloTracker.
 *
 * Pre-flight checks:
 *  1. Applies global Hololive dark theme
 *  2. Tests database connection (XAMPP must be running)
 *  3. Launches Login screen
 */
public class Main {

    public static void main(String[] args) {
        // 1. Apply cross-platform LAF so custom painting works correctly
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // 2. Apply our Hololive dark theme globally
        HoloTheme.applyGlobalDefaults();

        // 3. Start on EDT
        SwingUtilities.invokeLater(() -> {
            // 4. Test database connection before showing any UI
            try {
                DatabaseConnection.getConnection();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                    "Cannot connect to MySQL database!\n\n" +
                    "Please make sure:\n" +
                    "  1. XAMPP is installed and running\n" +
                    "  2. The MySQL service is STARTED in XAMPP Control Panel\n" +
                    "  3. You have imported grade_tracker_db.sql into phpMyAdmin\n\n" +
                    "Technical error:\n" + ex.getMessage(),
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }

            // 5. Launch login screen
            new LoginScreen().setVisible(true);
        });

        // 6. Clean up DB connection on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));
    }
}
