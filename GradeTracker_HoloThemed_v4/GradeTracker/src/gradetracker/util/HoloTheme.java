package gradetracker.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * HoloTheme - Central theme constants and component factories.
 *
 * Hololive-inspired deep-space dark palette:
 *   BG_DEEP    - near-black navy
 *   BG_PANEL   - slightly lighter panel
 *   BG_CARD    - card backgrounds
 *   BG_INPUT   - text field backgrounds
 *   ACCENT     - signature lavender
 *   ACCENT2    - pink-lavender highlight
 *   GOLD       - passing / success
 *   ROSE       - failing / danger
 *   TEAL       - info / neutral
 *   TEXT_MAIN  - primary white text
 *   TEXT_DIM   - subdued text
 *   BORDER     - subtle border color
 */
public class HoloTheme {

    // ── Palette ──────────────────────────────────────────────
    public static final Color BG_DEEP   = new Color(8,   8,  20);
    public static final Color BG_PANEL  = new Color(15,  15, 35);
    public static final Color BG_CARD   = new Color(22,  20, 50);
    public static final Color BG_INPUT  = new Color(12,  12, 30);
    public static final Color ACCENT    = new Color(160, 140, 255);
    public static final Color ACCENT2   = new Color(220, 160, 255);
    public static final Color GOLD      = new Color(255, 210, 100);
    public static final Color ROSE      = new Color(255, 120, 140);
    public static final Color TEAL      = new Color(100, 220, 220);
    public static final Color TEXT_MAIN = new Color(240, 235, 255);
    public static final Color TEXT_DIM  = new Color(140, 130, 170);
    public static final Color BORDER    = new Color(60,  55, 100);

    // ── Typography ───────────────────────────────────────────
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEAD  = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO  = new Font("Consolas",  Font.PLAIN, 12);

    // ── Global UIManager defaults ────────────────────────────
    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background",               BG_PANEL);
        UIManager.put("OptionPane.background",          BG_PANEL);
        UIManager.put("OptionPane.messageForeground",   TEXT_MAIN);
        UIManager.put("Button.background",              BG_CARD);
        UIManager.put("Button.foreground",              TEXT_MAIN);
        UIManager.put("Label.foreground",               TEXT_MAIN);
        UIManager.put("TextField.background",           BG_INPUT);
        UIManager.put("TextField.foreground",           TEXT_MAIN);
        UIManager.put("TextField.caretForeground",      ACCENT);
        UIManager.put("TextField.border",               BorderFactory.createEmptyBorder(4,8,4,8));
        UIManager.put("PasswordField.background",       BG_INPUT);
        UIManager.put("PasswordField.foreground",       TEXT_MAIN);
        UIManager.put("PasswordField.caretForeground",  ACCENT);
        UIManager.put("ComboBox.background",            BG_INPUT);
        UIManager.put("ComboBox.foreground",            TEXT_MAIN);
        UIManager.put("ComboBox.selectionBackground",   ACCENT.darker().darker());
        UIManager.put("ComboBox.selectionForeground",   TEXT_MAIN);
        UIManager.put("List.background",                BG_INPUT);
        UIManager.put("List.foreground",                TEXT_MAIN);
        UIManager.put("List.selectionBackground",       ACCENT.darker().darker());
        UIManager.put("List.selectionForeground",       TEXT_MAIN);
        UIManager.put("Table.background",               BG_PANEL);
        UIManager.put("Table.foreground",               TEXT_MAIN);
        UIManager.put("Table.gridColor",                BORDER);
        UIManager.put("Table.selectionBackground",      ACCENT.darker().darker());
        UIManager.put("Table.selectionForeground",      Color.WHITE);
        UIManager.put("TableHeader.background",         BG_CARD);
        UIManager.put("TableHeader.foreground",         ACCENT);
        UIManager.put("ScrollPane.background",          BG_PANEL);
        UIManager.put("Viewport.background",            BG_PANEL);
        UIManager.put("ToolTip.background",             BG_CARD);
        UIManager.put("ToolTip.foreground",             TEXT_MAIN);
        UIManager.put("OptionPane.buttonFont",          FONT_BODY);
        UIManager.put("Spinner.background",             BG_INPUT);
        UIManager.put("Spinner.foreground",             TEXT_MAIN);
        UIManager.put("FormattedTextField.background",  BG_INPUT);
        UIManager.put("FormattedTextField.foreground",  TEXT_MAIN);
    }

    // -- Logo image (painted star, no emoji dependency) --------
    /**
     * Creates a custom star-burst logo image. This avoids relying on
     * system emoji fonts which may render differently on each machine.
     *
     * @param size   pixel size (width and height)
     * @return       rendered BufferedImage
     */
    public static ImageIcon createLogoIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float cx = size / 2f;
        float cy = size / 2f;

        // Outer glow
        RadialGradientPaint glow = new RadialGradientPaint(
            new Point2D.Float(cx, cy), size / 2f,
            new float[]{0f, 0.5f, 1f},
            new Color[]{
                new Color(160, 140, 255, 80),
                new Color(120, 100, 220, 30),
                new Color(0, 0, 0, 0)
            }
        );
        g2.setPaint(glow);
        g2.fillOval(0, 0, size, size);

        // Draw a 4-pointed star
        g2.setPaint(new GradientPaint(0, 0, ACCENT, size, size, ACCENT2));
        g2.fill(makeStar4(cx, cy, size * 0.44f, size * 0.14f));

        // Small center circle
        g2.setColor(new Color(255, 255, 255, 200));
        float r = size * 0.09f;
        g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));

        g2.dispose();
        return new ImageIcon(img);
    }

    /** Makes a 4-pointed star shape centered at (cx, cy). */
    private static Shape makeStar4(float cx, float cy, float outer, float inner) {
        Path2D star = new Path2D.Float();
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i - Math.PI / 2;
            float r = (i % 2 == 0) ? outer : inner;
            float x = cx + (float)(r * Math.cos(angle));
            float y = cy + (float)(r * Math.sin(angle));
            if (i == 0) star.moveTo(x, y);
            else         star.lineTo(x, y);
        }
        star.closePath();
        return star;
    }

    /** Creates a JLabel showing the HoloTracker logo + text. */
    public static JLabel createLogoLabel(int iconSize, float fontSize) {
        JLabel lbl = new JLabel(" HoloTracker") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Draw the star icon
                int iconY = (getHeight() - iconSize) / 2;
                ImageIcon icon = createLogoIcon(iconSize);
                icon.paintIcon(this, g2, 0, iconY);

                // Draw the text with gradient
                Font f = new Font("Segoe UI", Font.BOLD, (int) fontSize);
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics(f);
                int textX = iconSize + 6;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                GradientPaint gp = new GradientPaint(textX, 0, ACCENT, textX + 150, 0, ACCENT2);
                g2.setPaint(gp);
                g2.drawString("HoloTracker", textX, textY);
                g2.dispose();
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, (int) fontSize));
        lbl.setForeground(ACCENT);
        lbl.setPreferredSize(new Dimension(iconSize + 6 + 120, Math.max(iconSize + 4, (int) fontSize + 10)));
        return lbl;
    }

    // ── Styled button factory --------------------------
    public static JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill;
                if (!isEnabled())              fill = bg.darker().darker();
                else if (getModel().isPressed()) fill = bg.darker().darker();
                else if (getModel().isRollover()) fill = bg.brighter();
                else                             fill = bg;
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                if (getModel().isRollover() && isEnabled()) {
                    g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 100));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 12, 12));
                }
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setForeground(fg);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 36));
        return btn;
    }

    public static JButton makePrimaryButton(String text) {
        return makeButton(text, new Color(60, 50, 130), TEXT_MAIN);
    }

    public static JButton makeDangerButton(String text) {
        return makeButton(text, new Color(100, 20, 40), ROSE);
    }

    public static JButton makeSuccessButton(String text) {
        return makeButton(text, new Color(20, 80, 60), GOLD);
    }

    // ── Styled text field factory --------------------------
    public static JTextField makeTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(ACCENT);
        tf.setFont(FONT_BODY);
        tf.setOpaque(true);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return tf;
    }

    public static JPasswordField makePasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setBackground(BG_INPUT);
        pf.setForeground(TEXT_MAIN);
        pf.setCaretColor(ACCENT);
        pf.setFont(FONT_BODY);
        pf.setOpaque(true);
        pf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return pf;
    }

    // ── Label factory -----------------------------------------
    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    // ── Card panel (rounded, darker bg) ------------------
    public static JPanel makeCard() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 16, 16));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // ── Scrollpane with dark scrollbar ---------------------------
    public static JScrollPane makeScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_PANEL);
        sp.getViewport().setBackground(BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
        return sp;
    }

    // ── Rounded border -------------------------------------------
    public static class RoundedBorder extends AbstractBorder {
        private final int   radius;
        private final Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color  = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Float(x, y, w - 1, h - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius / 2;
            return insets;
        }
    }

    /**
     * Shows a themed form dialog hosting any JPanel.
     * Returns true if user clicked the confirm/save button.
     *
     * @param parent    owner component
     * @param title     dialog title
     * @param formPanel the form panel to embed
     * @param saveLabel text for the save/confirm button (e.g. "Add", "Save")
     */
    public static boolean showFormDialog(Component parent, String title, JPanel formPanel, String saveLabel) {
        Window owner = (parent instanceof Window)
            ? (Window) parent
            : SwingUtilities.getWindowAncestor(parent);

        JDialog dlg = (owner instanceof Frame)
            ? new JDialog((Frame) owner, title, true)
            : new JDialog((Frame) null, title, true);

        dlg.setUndecorated(true);

        // Measure the form panel so we size the dialog around it
        formPanel.setBackground(BG_PANEL);
        formPanel.revalidate();
        Dimension pref = formPanel.getPreferredSize();
        int dialogW = Math.max(420, pref.width + 48);
        int dialogH = pref.height + 130;  // title + form + buttons
        dlg.setSize(dialogW, dialogH);
        dlg.setLocationRelativeTo(owner);
        dlg.setShape(new RoundRectangle2D.Double(0, 0, dialogW, dialogH, 16, 16));

        final boolean[] result = {false};

        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                GradientPaint gp = new GradientPaint(20, 0, ACCENT, getWidth() - 20, 0, ACCENT2);
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(20, 1, getWidth() - 40, 2, 2, 2));
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        dlg.setContentPane(root);

        // Title bar with close button
        JLabel titleLbl = makeLabel(title, new Font("Segoe UI", Font.BOLD, 15), ACCENT);
        titleLbl.setBounds(20, 16, dialogW - 60, 24);
        root.add(titleLbl);

        // Custom-painted close button
        JButton closeDlgBtn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                if (getModel().isRollover()) {
                    g2.setColor(new Color(180, 40, 60, 200));
                    g2.fillOval(0, 0, w, h);
                }
                int pad = 7;
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(getModel().isRollover() ? Color.WHITE : TEXT_DIM);
                g2.drawLine(pad, pad, w - pad, h - pad);
                g2.drawLine(w - pad, pad, pad, h - pad);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        closeDlgBtn.setBounds(dialogW - 38, 9, 28, 28);
        closeDlgBtn.setContentAreaFilled(false);
        closeDlgBtn.setBorderPainted(false);
        closeDlgBtn.setFocusPainted(false);
        closeDlgBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeDlgBtn.addActionListener(e -> dlg.dispose());
        root.add(closeDlgBtn);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBounds(16, 46, dialogW - 32, 1);
        root.add(sep);

        // Embed the form panel
        formPanel.setBounds(16, 54, dialogW - 32, pref.height);
        root.add(formPanel);

        // Buttons
        int btnY = 54 + pref.height + 14;
        JButton cancelBtn = makeButton("Cancel", new Color(30, 28, 60), TEXT_DIM);
        cancelBtn.setBounds(dialogW - 228, btnY, 100, 36);
        root.add(cancelBtn);

        JButton saveBtn = makeButton(saveLabel, new Color(60, 50, 130), TEXT_MAIN);
        saveBtn.setBounds(dialogW - 120, btnY, 100, 36);
        root.add(saveBtn);

        cancelBtn.addActionListener(e -> dlg.dispose());
        saveBtn.addActionListener(e  -> { result[0] = true; dlg.dispose(); });

        dlg.getRootPane().setDefaultButton(saveBtn);
        dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        dlg.getRootPane().getActionMap().put("cancel",
            new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) { dlg.dispose(); }
            });

        dlg.setVisible(true);
        return result[0];
    }

    // ── Custom themed dialogs ------------------------------
    /**
     * Themed confirmation dialog — replaces ugly system JOptionPane.
     * Returns true if user clicked the confirm button.
     *
     * @param parent      owner frame/dialog
     * @param title       dialog title
     * @param message     main message text (can include \n)
     * @param danger      if true, confirm button is red; if false, it's lavender
     */
    public static boolean showConfirm(Component parent, String title, String message, boolean danger) {
        // Find parent window
        Window owner = (parent instanceof Window)
            ? (Window) parent
            : SwingUtilities.getWindowAncestor(parent);

        JDialog dialog = new JDialog((Frame) null, title, true) {
            @Override
            public void dispose() {
                super.dispose();
            }
        };
        if (owner instanceof Frame) {
            dialog = new JDialog((Frame) owner, title, true);
        }
        final JDialog dlg = dialog;
        dlg.setUndecorated(true);
        dlg.setSize(420, 220);
        dlg.setLocationRelativeTo(owner);
        dlg.setShape(new RoundRectangle2D.Double(0, 0, 420, 220, 16, 16));

        final boolean[] result = {false};

        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                // top accent line
                GradientPaint gp = new GradientPaint(
                    20, 0, danger ? ROSE : ACCENT,
                    getWidth() - 20, 0, danger ? new Color(180, 60, 80) : ACCENT2
                );
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(20, 1, getWidth() - 40, 2, 2, 2));
                // border
                g2.setColor(danger ? new Color(150, 40, 60) : BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        dlg.setContentPane(root);

        // Title
        JLabel titleLbl = makeLabel(title, new Font("Segoe UI", Font.BOLD, 15),
            danger ? ROSE : ACCENT);
        titleLbl.setBounds(24, 20, 370, 24);
        root.add(titleLbl);

        // Message (multi-line)
        String[] lines = message.split("\n");
        int msgY = 54;
        for (String line : lines) {
            JLabel lbl = makeLabel(line.isEmpty() ? " " : line, FONT_BODY, TEXT_MAIN);
            lbl.setBounds(24, msgY, 370, 20);
            root.add(lbl);
            msgY += 22;
        }

        // Buttons
        JButton cancelBtn = makeButton("Cancel", new Color(30, 28, 60), TEXT_DIM);
        cancelBtn.setBounds(200, 168, 100, 36);
        root.add(cancelBtn);

        Color confirmBg = danger ? new Color(120, 20, 40) : new Color(60, 50, 130);
        Color confirmFg = danger ? ROSE : TEXT_MAIN;
        JButton confirmBtn = makeButton(danger ? "Delete" : "Confirm", confirmBg, confirmFg);
        confirmBtn.setBounds(308, 168, 100, 36);
        root.add(confirmBtn);

        cancelBtn.addActionListener(e  -> dlg.dispose());
        confirmBtn.addActionListener(e -> { result[0] = true; dlg.dispose(); });

        // Allow Escape to cancel
        dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        dlg.getRootPane().getActionMap().put("cancel",
            new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) { dlg.dispose(); }
            });

        dlg.setVisible(true);
        return result[0];
    }

    /**
     * Themed info/error popup - replaces JOptionPane.showMessageDialog.
     * @param danger  true = red accent (error), false = lavender (info)
     */
    public static void showMessage(Component parent, String title, String message, boolean danger) {
        Window owner = (parent instanceof Window)
            ? (Window) parent
            : SwingUtilities.getWindowAncestor(parent);

        JDialog dlg = (owner instanceof Frame)
            ? new JDialog((Frame) owner, title, true)
            : new JDialog((Frame) null, title, true);

        dlg.setUndecorated(true);
        dlg.setSize(400, 180);
        dlg.setLocationRelativeTo(owner);
        dlg.setShape(new RoundRectangle2D.Double(0, 0, 400, 180, 16, 16));

        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                GradientPaint gp = new GradientPaint(20, 0, danger ? ROSE : ACCENT,
                    getWidth() - 20, 0, danger ? new Color(180, 60, 80) : ACCENT2);
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(20, 1, getWidth() - 40, 2, 2, 2));
                g2.setColor(danger ? new Color(150, 40, 60) : BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        dlg.setContentPane(root);

        JLabel titleLbl = makeLabel(title, new Font("Segoe UI", Font.BOLD, 15),
            danger ? ROSE : ACCENT);
        titleLbl.setBounds(24, 18, 350, 24);
        root.add(titleLbl);

        String[] lines = message.split("\n");
        int msgY = 50;
        for (String line : lines) {
            JLabel lbl = makeLabel(line.isEmpty() ? " " : line, FONT_BODY, TEXT_MAIN);
            lbl.setBounds(24, msgY, 350, 20);
            root.add(lbl);
            msgY += 22;
        }

        JButton okBtn = makeButton("OK", danger ? new Color(120, 20, 40) : new Color(60, 50, 130),
            danger ? ROSE : TEXT_MAIN);
        okBtn.setBounds(300, 132, 84, 34);
        root.add(okBtn);
        okBtn.addActionListener(e -> dlg.dispose());

        dlg.getRootPane().setDefaultButton(okBtn);
        dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "ok");
        dlg.getRootPane().getActionMap().put("ok",
            new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) { dlg.dispose(); }
            });

        dlg.setVisible(true);
    }

    // ── Dark scrollbar -----------------------------------------
    static class DarkScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor             = BORDER;
            trackColor             = BG_DEEP;
            thumbDarkShadowColor   = BG_DEEP;
            thumbHighlightColor    = ACCENT;
            thumbLightShadowColor  = BG_DEEP;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return invisibleBtn(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return invisibleBtn(); }

        private JButton invisibleBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
    }
}
