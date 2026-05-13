package gradetracker.ui;

import gradetracker.db.GradeTrackerDAO;
import gradetracker.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.concurrent.ExecutionException;

/**
 * DashboardScreen — Main hub after login.
 * Shows live stats and navigation to all modules.
 * Only accessible when SessionManager.isLoggedIn() == true.
 */
public class DashboardScreen extends JFrame {

    private static final double PASS_THRESHOLD = 75.0;
    private final GradeTrackerDAO dao = new GradeTrackerDAO();

    private JLabel statStudents, statSubjects, statPassing, statFailing;
    private StarfieldPanel background;

    public DashboardScreen() {
        if (!SessionManager.isLoggedIn()) {
            new LoginScreen().setVisible(true);
            dispose();
            return;
        }

        setTitle("HoloTracker — Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 1100, 700, 20, 20));

        buildUI();
        refreshStats();
    }

    private void buildUI() {
        background = new StarfieldPanel();
        background.setLayout(null);
        setContentPane(background);

        // ── Window drag ──
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

        String user = SessionManager.getCurrentUser().getFullName();
        JLabel welcomeLbl = HoloTheme.makeLabel("Welcome, " + user + "  ●", HoloTheme.FONT_BODY, HoloTheme.TEXT_DIM);
        welcomeLbl.setBounds(750, 18, 300, 24);
        topBar.add(welcomeLbl);

        JButton closeBtn = makeWinBtn("close");
        closeBtn.setBounds(1058, 14, 32, 32);
        closeBtn.addActionListener(e -> System.exit(0));
        topBar.add(closeBtn);

        JButton minimizeBtn = makeWinBtn("min");
        minimizeBtn.setBounds(1020, 14, 32, 32);
        minimizeBtn.addActionListener(e -> setState(ICONIFIED));
        topBar.add(minimizeBtn);

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

        String[] navLabels = {"[H]  Dashboard", "[S]  Students", "[C]  Subjects", "[G]  Grades"};
        String[] navScreens = {"dash", "students", "subjects", "grades"};
        int[] navY = {20, 80, 140, 200};

        for (int i = 0; i < navLabels.length; i++) {
            final String screen = navScreens[i];
            JButton navBtn = makeNavButton(navLabels[i], i == 0);
            navBtn.setBounds(15, navY[i], 190, 46);
            sidebar.add(navBtn);
            navBtn.addActionListener(e -> navigate(screen));
        }

        // Logout at bottom of sidebar
        JButton logoutBtn = makeNavButton("[X]  Logout", false);
        logoutBtn.setForeground(HoloTheme.ROSE);
        logoutBtn.setBounds(15, 560, 190, 46);
        sidebar.add(logoutBtn);
        logoutBtn.addActionListener(e -> confirmLogout());

        // ── Main content area ──
        JPanel content = new JPanel(null);
        content.setOpaque(false);
        content.setBounds(240, 80, 840, 600);
        background.add(content);

        // Page title
        JLabel pageTitle = HoloTheme.makeLabel("Dashboard", new Font("Segoe UI", Font.BOLD, 28), HoloTheme.TEXT_MAIN);
        pageTitle.setBounds(0, 10, 500, 40);
        content.add(pageTitle);

        JLabel pageSub = HoloTheme.makeLabel("Real-time overview of your grade tracker", HoloTheme.FONT_SMALL, HoloTheme.TEXT_DIM);
        pageSub.setBounds(0, 52, 500, 20);
        content.add(pageSub);

        // ── Stats row ──
        statStudents = new JLabel("…");
        statSubjects = new JLabel("…");
        statPassing  = new JLabel("…");
        statFailing  = new JLabel("…");

        JPanel[] statCards = {
            makeStatCard("Total Students", statStudents, HoloTheme.TEAL),
            makeStatCard("Total Subjects", statSubjects, HoloTheme.ACCENT),
            makeStatCard("Passing",         statPassing,  HoloTheme.GOLD),
            makeStatCard("Failing",          statFailing,  HoloTheme.ROSE)
        };
        int sx = 0;
        for (JPanel sc : statCards) {
            sc.setBounds(sx, 90, 185, 110);
            content.add(sc);
            sx += 200;
        }

        // ── Quick-action cards ──
        String[][] actions = {
            {"[S]", "Students", "Add, view, edit & delete\nstudent records", "students"},
            {"[C]", "Subjects",  "Manage the list of\nsubjects in the system", "subjects"},
            {"[G]", "Grades",    "Enter and update\nstudent grades",          "grades"}
        };
        int ax = 0;
        for (String[] act : actions) {
            JPanel qCard = makeActionCard(act[0], act[1], act[2], act[3]);
            qCard.setBounds(ax, 240, 255, 160);
            content.add(qCard);
            ax += 270;
        }

        // ── Info panel ──
        JPanel infoPanel = HoloTheme.makeCard();
        infoPanel.setLayout(null);
        infoPanel.setBounds(0, 430, 810, 130);
        content.add(infoPanel);

        JLabel infoTitle = HoloTheme.makeLabel("ℹ  System Info", HoloTheme.FONT_HEAD, HoloTheme.ACCENT);
        infoTitle.setBounds(20, 16, 400, 22);
        infoPanel.add(infoTitle);

        String[] infos = {
            "  >>  Passing threshold: 75.0  (change PASS_THRESHOLD in each screen class)",
            "  >>  Grades are computed live from the database on every view",
            "  >>  Deleting a student automatically removes all their grade records (CASCADE)",
            "  >>  All passwords are stored hashed with SHA-256"
        };
        int iy = 44;
        for (String info : infos) {
            JLabel l = HoloTheme.makeLabel(info, HoloTheme.FONT_SMALL, HoloTheme.TEXT_DIM);
            l.setBounds(10, iy, 780, 18);
            infoPanel.add(l);
            iy += 20;
        }
    }

    private JPanel makeStatCard(String title, JLabel valueLbl, Color valueColor) {
        JPanel card = HoloTheme.makeCard();
        card.setLayout(null);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 38));
        valueLbl.setForeground(valueColor);
        valueLbl.setBounds(0, 18, 185, 50);
        valueLbl.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLbl);
        JLabel titLbl = HoloTheme.makeLabel(title, HoloTheme.FONT_SMALL, HoloTheme.TEXT_DIM);
        titLbl.setBounds(0, 70, 185, 20);
        titLbl.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titLbl);
        return card;
    }

    private JPanel makeActionCard(String icon, String title, String desc, String screen) {
        JPanel card = HoloTheme.makeCard();
        card.setLayout(null);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = HoloTheme.makeLabel(icon, new Font("Segoe UI Emoji", Font.PLAIN, 32), HoloTheme.TEXT_MAIN);
        iconLbl.setBounds(20, 18, 50, 44);
        card.add(iconLbl);

        JLabel titleLbl = HoloTheme.makeLabel(title, HoloTheme.FONT_HEAD, HoloTheme.TEXT_MAIN);
        titleLbl.setBounds(75, 24, 160, 24);
        card.add(titleLbl);

        JLabel descLbl = new JLabel("<html>" + desc.replace("\n", "<br>") + "</html>");
        descLbl.setFont(HoloTheme.FONT_SMALL);
        descLbl.setForeground(HoloTheme.TEXT_DIM);
        descLbl.setBounds(20, 70, 215, 44);
        card.add(descLbl);

        JLabel arrow = HoloTheme.makeLabel("→", HoloTheme.FONT_HEAD, HoloTheme.ACCENT);
        arrow.setBounds(215, 120, 30, 24);
        card.add(arrow);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { navigate(screen); }
            public void mouseEntered(java.awt.event.MouseEvent e) { arrow.setForeground(HoloTheme.ACCENT2); card.repaint(); }
            public void mouseExited(java.awt.event.MouseEvent e)  { arrow.setForeground(HoloTheme.ACCENT);  card.repaint(); }
        });
        return card;
    }

    private JButton makeNavButton(String text, boolean active) {
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

    private void refreshStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<int[], Void>() {
            int students, subjects;
            int[] pf;
            @Override protected int[] doInBackground() throws Exception {
                students = dao.getTotalStudents();
                subjects = dao.getTotalSubjects();
                pf       = dao.getPassFailCounts(PASS_THRESHOLD);
                return pf;
            }
            @Override protected void done() {
                try {
                    get();
                    animateCount(statStudents, students);
                    animateCount(statSubjects, subjects);
                    animateCount(statPassing,  pf[0]);
                    animateCount(statFailing,  pf[1]);
                } catch (Exception ex) {
                    statStudents.setText("?");
                    statSubjects.setText("?");
                    statPassing.setText("?");
                    statFailing.setText("?");
                }
            }
        };
        worker.execute();
    }

    private void animateCount(JLabel label, int target) {
        Timer t = new Timer(30, null);
        int[] current = {0};
        t.addActionListener(e -> {
            current[0] = Math.min(current[0] + Math.max(1, target / 20), target);
            label.setText(String.valueOf(current[0]));
            if (current[0] >= target) ((Timer)e.getSource()).stop();
        });
        t.start();
    }

    private void navigate(String screen) {
        switch (screen) {
            case "students" -> { dispose(); background.stopAnimation(); new StudentScreen().setVisible(true); }
            case "subjects" -> { dispose(); background.stopAnimation(); new SubjectScreen().setVisible(true); }
            case "grades"   -> { dispose(); background.stopAnimation(); new GradeScreen().setVisible(true); }
            default -> { /* already on dashboard */ }
        }
    }

    private void confirmLogout() {
        if (HoloTheme.showConfirm(this, "Logout", "Are you sure you want to log out?", false)) {
            SessionManager.logout();
            dispose();
            background.stopAnimation();
            new LoginScreen().setVisible(true);
        }
    }

    private JButton styleTopBtn(JButton btn) { return btn; } // unused stub

    /** Custom-painted window control button — draws crisp lines, no text. */
    private JButton makeWinBtn(final String type) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                if (getModel().isRollover()) {
                    Color bg = type.equals("close")
                        ? new Color(180, 40, 60, 200)
                        : new Color(60, 55, 100, 150);
                    g2.setColor(bg);
                    g2.fillOval(0, 0, w, h);
                }
                int pad = 8;
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(getModel().isRollover() ? Color.WHITE : HoloTheme.TEXT_DIM);
                if (type.equals("close")) {
                    g2.drawLine(pad, pad, w - pad, h - pad);
                    g2.drawLine(w - pad, pad, pad, h - pad);
                } else {
                    g2.drawLine(pad, h / 2, w - pad, h / 2);
                }
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
