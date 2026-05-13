package gradetracker.ui;

import gradetracker.db.GradeTrackerDAO;
import gradetracker.model.Grade;
import gradetracker.model.Student;
import gradetracker.util.HoloTheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * StudentReportDialog — Full grade breakdown for one student.
 * Shows: student info, list of subjects+grades, computed average, pass/fail.
 */
public class StudentReportDialog extends JDialog {

    private final Student        student;
    private final GradeTrackerDAO dao;
    private final double         threshold;

    public StudentReportDialog(Frame parent, Student student, GradeTrackerDAO dao, double threshold) {
        super(parent, "Grade Report", true);
        this.student   = student;
        this.dao       = dao;
        this.threshold = threshold;
        buildUI();
        setSize(560, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void buildUI() {
        JPanel root = new JPanel(null);
        root.setBackground(HoloTheme.BG_PANEL);
        setContentPane(root);

        // ── Header ──
        JPanel header = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                GradientPaint gp = new GradientPaint(0,0, new Color(40,20,80), getWidth(), 0, new Color(20,10,50));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBounds(0, 0, 560, 80);
        root.add(header);

        JLabel title = HoloTheme.makeLabel("Grade Report", new Font("Segoe UI", Font.BOLD, 20), HoloTheme.ACCENT);
        title.setBounds(20, 14, 300, 28);
        header.add(title);

        JLabel nameL = HoloTheme.makeLabel(student.getFullName(), HoloTheme.FONT_HEAD, HoloTheme.TEXT_MAIN);
        nameL.setBounds(20, 46, 360, 22);
        header.add(nameL);

        JLabel numL = HoloTheme.makeLabel("ID: " + student.getStudentNumber() + "   |   Year " + student.getYearLevel(),
            HoloTheme.FONT_SMALL, HoloTheme.TEXT_DIM);
        numL.setBounds(350, 52, 200, 18);
        header.add(numL);

        // ── Grade table ──
        String[] cols = {"Subject Code", "Subject Name", "Grade"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        double total = 0; int count = 0;
        List<Grade> grades = null;
        try { grades = dao.getGradesForStudent(student.getStudentId()); } catch (Exception ex) { grades = List.of(); }

        for (Grade g : grades) {
            model.addRow(new Object[]{ g.getSubjectCode(), g.getSubjectName(), String.format("%.2f", g.getGrade()) });
            total += g.getGrade(); count++;
        }

        JTable table = new JTable(model);
        table.setFont(HoloTheme.FONT_BODY);
        table.setForeground(HoloTheme.TEXT_MAIN);
        table.setBackground(HoloTheme.BG_PANEL);
        table.setGridColor(HoloTheme.BORDER);
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(70,55,130));
        table.getTableHeader().setBackground(HoloTheme.BG_CARD);
        table.getTableHeader().setForeground(HoloTheme.ACCENT);
        table.getTableHeader().setFont(HoloTheme.FONT_HEAD);

        // right-align grade column
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(right);

        JScrollPane scroll = HoloTheme.makeScrollPane(table);
        scroll.setBounds(20, 95, 520, 260);
        root.add(scroll);

        // ── Summary bar ──
        JPanel summary = HoloTheme.makeCard();
        summary.setLayout(null);
        summary.setBounds(20, 368, 520, 90);
        root.add(summary);

        double avg = count > 0 ? total / count : -1;
        String avgStr  = avg < 0 ? "No Grades" : String.format("%.2f", avg);
        String statStr = avg < 0 ? "No Grades" : (avg >= threshold ? "✔ PASSING" : "✘ FAILING");
        Color  statClr = avg < 0 ? HoloTheme.TEXT_DIM : (avg >= threshold ? HoloTheme.GOLD : HoloTheme.ROSE);

        JLabel subCount = HoloTheme.makeLabel("Subjects: " + count, HoloTheme.FONT_BODY, HoloTheme.TEXT_DIM);
        subCount.setBounds(20, 14, 140, 22);
        summary.add(subCount);

        JLabel avgLabel = HoloTheme.makeLabel("Average: " + avgStr, new Font("Segoe UI", Font.BOLD, 15), HoloTheme.TEXT_MAIN);
        avgLabel.setBounds(20, 44, 200, 26);
        summary.add(avgLabel);

        JLabel statLabel = HoloTheme.makeLabel(statStr, new Font("Segoe UI", Font.BOLD, 22), statClr);
        statLabel.setBounds(320, 28, 180, 36);
        statLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        summary.add(statLabel);

        // ── Close button ──
        JButton closeBtn = HoloTheme.makePrimaryButton("Close");
        closeBtn.setBounds(200, 470, 160, 38);
        root.add(closeBtn);
        closeBtn.addActionListener(e -> dispose());
    }
}
