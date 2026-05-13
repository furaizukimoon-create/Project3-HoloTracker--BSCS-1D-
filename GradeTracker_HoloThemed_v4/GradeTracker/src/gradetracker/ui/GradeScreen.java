package gradetracker.ui;

import gradetracker.db.GradeTrackerDAO;
import gradetracker.model.Grade;
import gradetracker.model.Student;
import gradetracker.model.Subject;
import gradetracker.util.HoloTheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GradeScreen — Full CRUD for grade records.
 *
 * Workflow:
 *   1. Select a student from the dropdown.
 *   2. Table shows all grades for that student (3-table JOIN).
 *   3. Add / Edit / Delete grade entries.
 *   4. Average and Pass/Fail status update live at the bottom.
 *
 * Key educational point: uses a JOIN across grades, students, subjects.
 */
public class GradeScreen extends BaseScreen {

    private static final double PASS_THRESHOLD = 75.0;
    private final GradeTrackerDAO dao = new GradeTrackerDAO();

    private JComboBox<Student>  studentBox;
    private JTable              table;
    private DefaultTableModel   tableModel;
    private List<Grade>         grades   = new ArrayList<>();
    private List<Student>       students = new ArrayList<>();
    private JLabel              avgLabel, statusLabel;

    public GradeScreen() {
        initScreen("Grades");
    }

    @Override protected String getPageTitle()    { return "Grades"; }
    @Override protected String getPageSubtitle() { return "Enter and manage student grades per subject"; }

    @Override
    protected void buildContent() {

        // ── Student selector row ──────────────────────────────
        JLabel selectLbl = HoloTheme.makeLabel("Student:", HoloTheme.FONT_BODY, HoloTheme.TEXT_DIM);
        selectLbl.setBounds(0, 80, 80, 32);
        contentPanel.add(selectLbl);

        studentBox = new JComboBox<Student>();
        studentBox.setBackground(HoloTheme.BG_INPUT);
        studentBox.setForeground(HoloTheme.TEXT_MAIN);
        studentBox.setFont(HoloTheme.FONT_BODY);
        studentBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? HoloTheme.ACCENT.darker().darker() : HoloTheme.BG_INPUT);
                setForeground(HoloTheme.TEXT_MAIN);
                if (value instanceof Student) {
                    Student s = (Student) value;
                    setText(s.getStudentNumber() + "  —  " + s.getFullName());
                }
                return this;
            }
        });
        studentBox.setBounds(88, 80, 380, 32);
        contentPanel.add(studentBox);

        JButton reloadStudentsBtn = HoloTheme.makeButton("Reload", new Color(20, 20, 50), HoloTheme.TEXT_DIM);
        reloadStudentsBtn.setBounds(476, 80, 90, 32);
        contentPanel.add(reloadStudentsBtn);

        // ── Action buttons ────────────────────────────────────
        JButton addBtn    = HoloTheme.makeSuccessButton("+ Add Grade");
        JButton editBtn   = HoloTheme.makePrimaryButton("Edit Grade");
        JButton deleteBtn = HoloTheme.makeDangerButton("Delete");

        addBtn.setBounds(0,   125, 140, 36);
        editBtn.setBounds(147, 125, 130, 36);
        deleteBtn.setBounds(284, 125, 110, 36);

        contentPanel.add(addBtn);
        contentPanel.add(editBtn);
        contentPanel.add(deleteBtn);

        // ── Grade table ───────────────────────────────────────
        String[] cols = {"Subject Code", "Subject Name", "Grade"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = HoloTheme.makeScrollPane(table);
        scroll.setBounds(0, 174, 820, 310);
        contentPanel.add(scroll);

        // ── Summary bar ───────────────────────────────────────
        JPanel summaryBar = HoloTheme.makeCard();
        summaryBar.setLayout(null);
        summaryBar.setBounds(0, 494, 820, 66);
        contentPanel.add(summaryBar);

        JLabel avgTitleLbl = HoloTheme.makeLabel("Average Grade:", HoloTheme.FONT_BODY, HoloTheme.TEXT_DIM);
        avgTitleLbl.setBounds(16, 20, 130, 24);
        summaryBar.add(avgTitleLbl);

        avgLabel = HoloTheme.makeLabel("—", new Font("Segoe UI", Font.BOLD, 20), HoloTheme.TEXT_MAIN);
        avgLabel.setBounds(150, 17, 140, 30);
        summaryBar.add(avgLabel);

        JLabel statTitleLbl = HoloTheme.makeLabel("Status:", HoloTheme.FONT_BODY, HoloTheme.TEXT_DIM);
        statTitleLbl.setBounds(360, 20, 60, 24);
        summaryBar.add(statTitleLbl);

        statusLabel = HoloTheme.makeLabel("—", new Font("Segoe UI", Font.BOLD, 20), HoloTheme.TEXT_DIM);
        statusLabel.setBounds(425, 17, 200, 30);
        summaryBar.add(statusLabel);

        // ── Wire events ───────────────────────────────────────
        loadStudentDropdown();
        studentBox.addActionListener(e -> loadGrades());
        reloadStudentsBtn.addActionListener(e -> { loadStudentDropdown(); loadGrades(); });
        addBtn.addActionListener(e    -> showAddGradeDialog());
        editBtn.addActionListener(e   -> showEditGradeDialog());
        deleteBtn.addActionListener(e -> deleteSelectedGrade());
    }

    private void styleTable() {
        table.setFont(HoloTheme.FONT_BODY);
        table.setForeground(HoloTheme.TEXT_MAIN);
        table.setBackground(HoloTheme.BG_PANEL);
        table.setGridColor(HoloTheme.BORDER);
        table.setRowHeight(32);
        table.setSelectionBackground(new Color(70, 55, 130, 200));
        table.setSelectionForeground(Color.WHITE);
        table.setFocusable(false);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(HoloTheme.FONT_HEAD);
        header.setBackground(HoloTheme.BG_CARD);
        header.setForeground(HoloTheme.ACCENT);

        // Color-code grade by pass/fail threshold
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                try {
                    double g = Double.parseDouble(v.toString());
                    setForeground(g >= PASS_THRESHOLD ? HoloTheme.GOLD : HoloTheme.ROSE);
                } catch (Exception ignored) {
                    setForeground(HoloTheme.TEXT_MAIN);
                }
                setHorizontalAlignment(CENTER);
                setBackground(sel ? new Color(70, 55, 130, 200) : HoloTheme.BG_PANEL);
                setOpaque(true);
                return this;
            }
        });
    }

    private void loadStudentDropdown() {
        // Remember current selection
        Student prevSelected = (Student) studentBox.getSelectedItem();

        studentBox.removeAllItems();
        try {
            students = dao.getAllStudents();
            for (Student s : students) studentBox.addItem(s);

            // Re-select previously selected student if still present
            if (prevSelected != null) {
                for (int i = 0; i < studentBox.getItemCount(); i++) {
                    if (studentBox.getItemAt(i).getStudentId() == prevSelected.getStudentId()) {
                        studentBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            showError("Failed to load students:\n" + ex.getMessage());
        }

        // Trigger grade load for current selection
        if (studentBox.getItemCount() > 0) loadGrades();
    }

    private void loadGrades() {
        tableModel.setRowCount(0);
        grades = new ArrayList<>();
        avgLabel.setText("—");
        statusLabel.setText("—");
        statusLabel.setForeground(HoloTheme.TEXT_DIM);

        Student s = (Student) studentBox.getSelectedItem();
        if (s == null) return;

        try {
            grades = dao.getGradesForStudent(s.getStudentId());
            for (Grade g : grades) {
                tableModel.addRow(new Object[]{
                    g.getSubjectCode(),
                    g.getSubjectName(),
                    String.format("%.2f", g.getGrade())
                });
            }

            double avg = dao.getAverage(s.getStudentId());
            if (avg < 0) {
                avgLabel.setText("No Grades");
                avgLabel.setForeground(HoloTheme.TEXT_DIM);
                statusLabel.setText("No Grades");
                statusLabel.setForeground(HoloTheme.TEXT_DIM);
            } else {
                avgLabel.setText(String.format("%.2f", avg));
                avgLabel.setForeground(HoloTheme.TEXT_MAIN);
                boolean pass = avg >= PASS_THRESHOLD;
                statusLabel.setText(pass ? "PASSING" : "FAILING");
                statusLabel.setForeground(pass ? HoloTheme.GOLD : HoloTheme.ROSE);
            }
        } catch (Exception ex) {
            showError("Failed to load grades:\n" + ex.getMessage());
        }
    }

    private void showAddGradeDialog() {
        Student student = (Student) studentBox.getSelectedItem();
        if (student == null) { showError("Please select a student first."); return; }

        List<Subject> subjects;
        try {
            subjects = dao.getAllSubjects();
        } catch (Exception ex) {
            showError("Cannot load subjects:\n" + ex.getMessage()); return;
        }

        if (subjects.isEmpty()) {
            showError("No subjects found. Please add subjects first."); return;
        }

        // Filter out subjects the student already has a grade for
        List<Subject> available = new ArrayList<>();
        for (Subject sub : subjects) {
            try {
                if (!dao.gradeExists(student.getStudentId(), sub.getSubjectId())) {
                    available.add(sub);
                }
            } catch (Exception ignored) {
                available.add(sub);
            }
        }

        if (available.isEmpty()) {
            showError("This student already has grades for all available subjects."); return;
        }

        JComboBox<Subject> subBox = new JComboBox<Subject>(available.toArray(new Subject[0]));
        subBox.setBackground(HoloTheme.BG_INPUT);
        subBox.setForeground(HoloTheme.TEXT_MAIN);
        subBox.setFont(HoloTheme.FONT_BODY);
        subBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? HoloTheme.ACCENT.darker().darker() : HoloTheme.BG_INPUT);
                setForeground(HoloTheme.TEXT_MAIN);
                return this;
            }
        });

        JTextField gradeField = HoloTheme.makeTextField(10);
        gradeField.setToolTipText("Enter a number between 0 and 100");

        JLabel studentInfo = HoloTheme.makeLabel(
            student.getFullName() + "  [" + student.getStudentNumber() + "]",
            HoloTheme.FONT_BODY, HoloTheme.TEAL);

        JPanel p = buildFormPanel(
            new String[]{"Student:", "Subject:", "Grade (0 to 100):"},
            new JComponent[]{studentInfo, subBox, gradeField}
        );

        if (!HoloTheme.showFormDialog(this, "Add Grade", p, "Save")) return;

        Subject subject = (Subject) subBox.getSelectedItem();
        if (subject == null) return;

        String gradeStr = gradeField.getText().trim();
        if (gradeStr.isEmpty()) { showError("Grade is required."); return; }

        double grade;
        try {
            grade = Double.parseDouble(gradeStr);
        } catch (NumberFormatException e) {
            showError("Grade must be a number (e.g. 85 or 92.5)."); return;
        }
        if (grade < 0 || grade > 100) {
            showError("Grade must be between 0 and 100."); return;
        }

        try {
            // Double-check duplicate (safety net)
            if (dao.gradeExists(student.getStudentId(), subject.getSubjectId())) {
                showError("This student already has a grade for " + subject.getSubjectCode() +
                          ".\nUse the Edit button to change it."); return;
            }
            dao.addGrade(student.getStudentId(), subject.getSubjectId(), grade);
            loadGrades();
            showInfo("Grade saved successfully!");
        } catch (Exception ex) {
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private void showEditGradeDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a grade to edit."); return; }
        if (row >= grades.size()) return;
        Grade g = grades.get(row);

        JTextField gradeField = HoloTheme.makeTextField(10);
        gradeField.setText(String.format("%.2f", g.getGrade()));

        JLabel subjectInfo = HoloTheme.makeLabel(
            g.getSubjectCode() + "  —  " + g.getSubjectName(),
            HoloTheme.FONT_BODY, HoloTheme.TEAL);

        JPanel p = buildFormPanel(
            new String[]{"Subject:", "New Grade (0 to 100):"},
            new JComponent[]{subjectInfo, gradeField}
        );

        if (!HoloTheme.showFormDialog(this, "Edit Grade", p, "Save")) return;

        String gradeStr = gradeField.getText().trim();
        if (gradeStr.isEmpty()) { showError("Grade is required."); return; }

        double newGrade;
        try {
            newGrade = Double.parseDouble(gradeStr);
        } catch (NumberFormatException e) {
            showError("Grade must be a number."); return;
        }
        if (newGrade < 0 || newGrade > 100) {
            showError("Grade must be between 0 and 100."); return;
        }

        try {
            dao.updateGrade(g.getGradeId(), newGrade);
            loadGrades();
            showInfo("Grade updated successfully!");
        } catch (Exception ex) {
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private void deleteSelectedGrade() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a grade to delete."); return; }
        if (row >= grades.size()) return;
        Grade g = grades.get(row);

        if (!showConfirm("Delete Grade",
                "Delete grade for " + g.getSubjectCode() + "?", true)) return;

        try {
            dao.deleteGrade(g.getGradeId());
            loadGrades();
            showInfo("Grade deleted.");
        } catch (Exception ex) {
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private JPanel buildFormPanel(String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(HoloTheme.BG_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i;
            gc.anchor = GridBagConstraints.EAST;
            gc.fill = GridBagConstraints.NONE;
            gc.weightx = 0;
            p.add(HoloTheme.makeLabel(labels[i], HoloTheme.FONT_BODY, HoloTheme.TEXT_DIM), gc);
            gc.gridx = 1;
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1.0;
            p.add(fields[i], gc);
        }
        return p;
    }
}
