package gradetracker.ui;

import gradetracker.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * BaseScreen — Shared layout (starfield + sidebar + topbar) for all
 * management screens (Students, Subjects, Grades).
 * Subclasses call buildBaseUI() then add their content to contentPanel.
 */
public abstract class BaseScreen extends JFrame {

    protected StarfieldPanel background;
    protected JPanel         contentPanel;   // 840×600 work area

    protected abstract String getPageTitle();
    protected abstract String getPageSubtitle();
    protected abstract void   buildContent();  // subclass populates contentPanel

    protected void initScreen(String frameTitle) {
        if (!SessionManager.isLoggedIn()) {
            new LoginScreen().setVisible(true);
            dispose(); return;
        }
        setTitle("HoloTracker — " + frameTitle);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 1100, 700, 20, 20));
        buildBaseUI();
        buildContent();
    }

    private void buildBaseUI() {
        background = new StarfieldPanel();
        background.setLayout(null);
        setContentPane(background);

        // drag
        MouseAdapter drag = new MouseAdapter() {
            Point start;
            public void mousePressed(java.awt.event.MouseEvent e)  { start = e.getPoint(); }
            public void mouseDragged(java.awt.event.MouseEvent e)  {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - start.x, loc.y + e.getY() - start.y);
            }
        };
        background.addMouseListener(drag);
        background.addMouseMotionListener(drag);

        // ── Top bar ──
        JPanel topBar = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(new Color(10, 10, 28, 200));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(HoloTheme.BORDER);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setBounds(0, 0, 1100, 60);
        background.add(topBar);

        JLabel logoLbl = HoloTheme.createLogoLabel(22, 15f);
        logoLbl.setBounds(12, 10, 200, 40);
        topBar.add(logoLbl);

        JButton closeBtn = makeTopBtn("close");
        closeBtn.setBounds(1058, 14, 32, 32);
        closeBtn.addActionListener(e -> System.exit(0));
        topBar.add(closeBtn);

        JButton minBtn = makeTopBtn("min");
        minBtn.setBounds(1020, 14, 32, 32);
        minBtn.addActionListener(e -> setState(ICONIFIED));
        topBar.add(minBtn);

        // ── Sidebar ──
        JPanel sidebar = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(new Color(10, 10, 28, 220));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(HoloTheme.BORDER);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setBounds(0, 60, 220, 640);
        background.add(sidebar);

        String[][] navItems = {
            {"[H]  Dashboard", "dash"},
            {"[S]  Students",  "students"},
            {"[C]  Subjects",  "subjects"},
            {"[G]  Grades",    "grades"}
        };
        int[] navY = {20, 80, 140, 200};
        String current = getPageTitle().toLowerCase();

        for (int i = 0; i < navItems.length; i++) {
            boolean active = navItems[i][0].toLowerCase().contains(current.substring(0, Math.min(4, current.length())));
            JButton navBtn = makeNavBtn(navItems[i][0], active);
            navBtn.setBounds(15, navY[i], 190, 46);
            final String sc = navItems[i][1];
            navBtn.addActionListener(e -> navigate(sc));
            sidebar.add(navBtn);
        }

        JButton logoutBtn = makeNavBtn("[X]  Logout", false);
        logoutBtn.setForeground(HoloTheme.ROSE);
        logoutBtn.setBounds(15, 560, 190, 46);
        logoutBtn.addActionListener(e -> confirmLogout());
        sidebar.add(logoutBtn);

        // ── Content area ──
        contentPanel = new JPanel(null);
        contentPanel.setOpaque(false);
        contentPanel.setBounds(240, 80, 840, 600);
        background.add(contentPanel);

        JLabel pageTitle = HoloTheme.makeLabel(getPageTitle(), new Font("Segoe UI", Font.BOLD, 28), HoloTheme.TEXT_MAIN);
        pageTitle.setBounds(0, 10, 600, 40);
        contentPanel.add(pageTitle);

        JLabel pageSub = HoloTheme.makeLabel(getPageSubtitle(), HoloTheme.FONT_SMALL, HoloTheme.TEXT_DIM);
        pageSub.setBounds(0, 52, 600, 20);
        contentPanel.add(pageSub);
    }

    private JButton makeNavBtn(String text, boolean active) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active || getModel().isRollover()) {
                    g2.setColor(new Color(80, 70, 140, 80));
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                    g2.setColor(HoloTheme.ACCENT);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(0, 8, 0, getHeight()-8);
                }
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(HoloTheme.FONT_BODY);
        btn.setForeground(active ? HoloTheme.TEXT_MAIN : HoloTheme.TEXT_DIM);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a custom-painted window control button.
     * type: "close" draws an X with lines, "min" draws a dash — no text rendering at all.
     */
    private JButton makeTopBtn(final String type) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // Hover background
                if (getModel().isRollover()) {
                    Color bg = type.equals("close")
                        ? new Color(180, 40, 60, 200)
                        : new Color(60, 55, 100, 150);
                    g2.setColor(bg);
                    g2.fillOval(0, 0, w, h);
                }

                int pad = 8;
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Color iconColor = getModel().isRollover() ? Color.WHITE : HoloTheme.TEXT_DIM;
                g2.setColor(iconColor);

                if (type.equals("close")) {
                    // Draw X with two diagonal lines
                    g2.drawLine(pad, pad, w - pad, h - pad);
                    g2.drawLine(w - pad, pad, pad, h - pad);
                } else {
                    // Draw minimize dash
                    int mid = h / 2;
                    g2.drawLine(pad, mid, w - pad, mid);
                }
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    protected void navigate(String screen) {
        dispose(); background.stopAnimation();
        switch (screen) {
            case "dash"     -> new DashboardScreen().setVisible(true);
            case "students" -> new StudentScreen().setVisible(true);
            case "subjects" -> new SubjectScreen().setVisible(true);
            case "grades"   -> new GradeScreen().setVisible(true);
        }
    }

    protected void confirmLogout() {
        if (HoloTheme.showConfirm(this, "Logout", "Are you sure you want to log out?", false)) {
            SessionManager.logout();
            dispose();
            background.stopAnimation();
            new LoginScreen().setVisible(true);
        }
    }

    protected void showError(String msg) {
        HoloTheme.showMessage(this, "Error", msg, true);
    }

    protected void showInfo(String msg) {
        HoloTheme.showMessage(this, "Done", msg, false);
    }

    /** Themed confirm dialog. Returns true if user confirmed. */
    protected boolean showConfirm(String title, String message, boolean danger) {
        return HoloTheme.showConfirm(this, title, message, danger);
    }
}
