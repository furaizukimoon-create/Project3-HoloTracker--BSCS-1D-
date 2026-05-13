package gradetracker.ui;

import gradetracker.db.DatabaseConnection;
import gradetracker.db.GradeTrackerDAO;
import gradetracker.model.User;
import gradetracker.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * LoginScreen — The first screen users see.
 * Features animated starfield background, a glowing card,
 * and hololive-inspired aesthetics.
 *
 * Default credentials:  admin / admin123
 */
public class LoginScreen extends JFrame {

    private StarfieldPanel background;
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JButton        loginButton;
    private final GradeTrackerDAO dao = new GradeTrackerDAO();

    // Animation state
    private float cardAlpha   = 0f;
    private Timer fadeInTimer;

    public LoginScreen() {
        setTitle("HoloTracker — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 1000, 650, 20, 20));

        buildUI();
        startFadeIn();
    }

    private void buildUI() {
        background = new StarfieldPanel();
        background.setLayout(null);
        setContentPane(background);

        // ── Drag-to-move (since undecorated) ──
        MouseAdapter drag = new MouseAdapter() {
            Point start;
            public void mousePressed(MouseEvent e)  { start = e.getPoint(); }
            public void mouseDragged(MouseEvent e)  {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - start.x, loc.y + e.getY() - start.y);
            }
        };
        background.addMouseListener(drag);
        background.addMouseMotionListener(drag);

        // ── Close button (custom-painted X — no text rendering) ──
        JButton closeBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                if (getModel().isRollover()) {
                    g2.setColor(new Color(180, 40, 60, 200));
                    g2.fillOval(0, 0, w, h);
                }
                int pad = 8;
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(getModel().isRollover() ? Color.WHITE : HoloTheme.TEXT_DIM);
                g2.drawLine(pad, pad, w - pad, h - pad);
                g2.drawLine(w - pad, pad, pad, h - pad);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        closeBtn.setBounds(957, 9, 32, 32);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        background.add(closeBtn);

        // ── Left branding panel ──
        JPanel branding = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                // nothing — transparent over starfield
            }
        };
        branding.setOpaque(false);
        branding.setBounds(60, 0, 420, 650);
        background.add(branding);

        // Logo / title art — uses HoloTheme's custom star painter (no emoji dependency)
        JLabel logoIcon = HoloTheme.createLogoLabel(72, 40f);
        logoIcon.setBounds(0, 170, 380, 80);
        branding.add(logoIcon);

        JLabel tagline = HoloTheme.makeLabel("Student Grade Management System", HoloTheme.FONT_BODY, HoloTheme.TEXT_DIM);
        tagline.setBounds(0, 258, 380, 22);
        branding.add(tagline);

        JLabel school = HoloTheme.makeLabel("Powered by JDBC  ·  MySQL  ·  Java Swing", HoloTheme.FONT_SMALL, new Color(100, 90, 140));
        school.setBounds(0, 330, 380, 18);
        branding.add(school);

        // ── Login card panel ──
        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardAlpha));
                // card fill
                g2.setColor(new Color(15, 15, 38, 230));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                // border glow
                g2.setColor(HoloTheme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 24, 24));
                // top accent line
                GradientPaint line = new GradientPaint(30,0, HoloTheme.ACCENT, getWidth()-30, 0, HoloTheme.ACCENT2);
                g2.setPaint(line);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(30, 1, getWidth()-60, 2, 2, 2));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBounds(520, 125, 400, 400);
        background.add(card);

        // Card title
        JLabel cardTitle = HoloTheme.makeLabel("Welcome Back", new Font("Segoe UI", Font.BOLD, 24), HoloTheme.TEXT_MAIN);
        cardTitle.setBounds(30, 30, 340, 34);
        card.add(cardTitle);

        JLabel cardSub = HoloTheme.makeLabel("Sign in to manage student records", HoloTheme.FONT_SMALL, HoloTheme.TEXT_DIM);
        cardSub.setBounds(30, 68, 340, 18);
        card.add(cardSub);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(HoloTheme.BORDER);
        sep.setBounds(30, 95, 340, 2);
        card.add(sep);

        // Username
        JLabel uLbl = HoloTheme.makeLabel("Username", HoloTheme.FONT_SMALL, HoloTheme.TEXT_DIM);
        uLbl.setBounds(30, 115, 200, 18);
        card.add(uLbl);

        usernameField = HoloTheme.makeTextField(20);
        usernameField.setBounds(30, 135, 340, 40);
        card.add(usernameField);

        // Password
        JLabel pLbl = HoloTheme.makeLabel("Password", HoloTheme.FONT_SMALL, HoloTheme.TEXT_DIM);
        pLbl.setBounds(30, 190, 200, 18);
        card.add(pLbl);

        passwordField = HoloTheme.makePasswordField(20);
        passwordField.setBounds(30, 210, 340, 40);
        card.add(passwordField);

        // Error label
        errorLabel = HoloTheme.makeLabel("", HoloTheme.FONT_SMALL, HoloTheme.ROSE);
        errorLabel.setBounds(30, 260, 340, 18);
        card.add(errorLabel);

        // Login button
        loginButton = HoloTheme.makePrimaryButton("Sign In");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBounds(30, 285, 340, 44);
        card.add(loginButton);

        // Hint
        JLabel hint = HoloTheme.makeLabel("Default: admin / admin123", HoloTheme.FONT_SMALL, new Color(80, 75, 110));
        hint.setBounds(30, 345, 340, 18);
        card.add(hint);

        // ── Actions ──
        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // focus username on open
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    private void startFadeIn() {
        fadeInTimer = new Timer(20, e -> {
            cardAlpha = Math.min(1f, cardAlpha + 0.04f);
            repaint();
            if (cardAlpha >= 1f) ((Timer)e.getSource()).stop();
        });
        fadeInTimer.start();
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Both fields are required.");
            shake(loginButton);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        // Run DB check off EDT to avoid UI freeze
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override protected User doInBackground() throws Exception {
                return dao.login(username, password);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        gradetracker.util.SessionManager.login(user);
                        dispose();
                        background.stopAnimation();
                        SwingUtilities.invokeLater(() -> new DashboardScreen().setVisible(true));
                    } else {
                        errorLabel.setText("Invalid username or password.");
                        shake(loginButton);
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    errorLabel.setText("Database error: " + ex.getMessage());
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                }
            }
        };
        worker.execute();
    }

    /** Quick horizontal shake animation for error feedback. */
    private void shake(Component c) {
        Point orig = c.getLocation();
        Timer shaker = new Timer(30, null);
        int[] steps = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        int[] idx = {0};
        shaker.addActionListener(e -> {
            if (idx[0] < steps.length) {
                c.setLocation(orig.x + steps[idx[0]], orig.y);
                idx[0]++;
            } else {
                c.setLocation(orig);
                shaker.stop();
            }
        });
        shaker.start();
    }

    public static void main(String[] args) {
        HoloTheme.applyGlobalDefaults();
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        HoloTheme.applyGlobalDefaults();

        // Test DB connection on startup
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConnection.getConnection();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                    "Cannot connect to database!\n\n" +
                    "Make sure:\n" +
                    "1. XAMPP is running\n" +
                    "2. MySQL service is started\n" +
                    "3. grade_tracker_db.sql has been imported\n\n" +
                    "Error: " + ex.getMessage(),
                    "Database Connection Failed",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
            new LoginScreen().setVisible(true);
        });
    }
}
