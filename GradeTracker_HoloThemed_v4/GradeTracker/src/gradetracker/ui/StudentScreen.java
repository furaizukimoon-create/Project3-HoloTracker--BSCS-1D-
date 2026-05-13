package gradetracker.ui;

import gradetracker.db.GradeTrackerDAO;
import gradetracker.model.Student;
import gradetracker.util.HoloTheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentScreen — Full CRUD for student records.
 * Table: Student Number | Full Name | Year Level | Average Grade | Status
 */
public class StudentScreen extends BaseScreen {

    private static final double PASS_THRESHOLD = 75.0;
    private final GradeTrackerDAO dao = new GradeTrackerDAO();

    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Student>     students = new ArrayList<>();

    public StudentScreen() {
        initScreen("Students");
    }

    @Override protected String getPageTitle()    { return "Students"; }
    @Override protected String getPageSubtitle() { return "Manage student records — Add, edit, view and delete"; }

    @Override
    protected void buildContent() {
        JButton addBtn     = HoloTheme.makeSuccessButton("+ Add Student");
        JButton editBtn    = HoloTheme.makePrimaryButton("Edit");
        JButton deleteBtn  = HoloTheme.makeDangerButton("Delete");
        JButton reportBtn  = HoloTheme.makeButton("Report", new Color(40, 30, 80), HoloTheme.TEAL);
        JButton refreshBtn = HoloTheme.makeButton("Refresh", new Color(20, 20, 50), HoloTheme.TEXT_DIM);

        addBtn.setBounds(0,   80, 145, 38);
        editBtn.setBounds(152, 80, 110, 38);
        deleteBtn.setBounds(269, 80, 110, 38);
        reportBtn.setBounds(386, 80, 110, 38);
        refreshBtn.setBounds(503, 80, 110, 38);

        contentPanel.add(addBtn);
        contentPanel.add(editBtn);
        contentPanel.add(deleteBtn);
        contentPanel.add(reportBtn);
        contentPanel.add(refreshBtn);

        String[] cols = {"Student Number", "Full Name", "Year Level", "Average Grade", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = HoloTheme.makeScrollPane(table);
        scroll.setBounds(0, 130, 820, 420);
        contentPanel.add(scroll);

        addBtn.addActionListener(e    -> showAddDialog());
        editBtn.addActionListener(e   -> showEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        reportBtn.addActionListener(e -> showReport());
        refreshBtn.addActionListener(e -> loadStudents());

        loadStudents();
    }

    private void styleTable() {
        table.setFont(HoloTheme.FONT_BODY);
        table.setForeground(HoloTheme.TEXT_MAIN);
        table.setBackground(HoloTheme.BG_PANEL);
        table.setGridColor(HoloTheme.BORDER);
        table.setRowHeight(34);
        table.setSelectionBackground(new Color(70, 55, 130, 200));
        table.setSelectionForeground(Color.WHITE);
        table.setFocusable(false);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(HoloTheme.FONT_HEAD);
        header.setBackground(HoloTheme.BG_CARD);
        header.setForeground(HoloTheme.ACCENT);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String val = (v == null) ? "" : v.toString();
                if (val.contains("Passing"))      setForeground(HoloTheme.GOLD);
                else if (val.contains("Failing")) setForeground(HoloTheme.ROSE);
                else                              setForeground(HoloTheme.TEXT_DIM);
                setHorizontalAlignment(CENTER);
                setBackground(sel ? new Color(70, 55, 130, 200) : HoloTheme.BG_PANEL);
                setOpaque(true);
                return this;
            }
        });
    }

    private void loadStudents() {
        tableModel.setRowCount(0);

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                students = dao.getAllStudents();
                List<Object[]> rows = new ArrayList<Object[]>();
                for (Student s : students) {
                    double avg = dao.getAverage(s.getStudentId());
                    String avgStr;
                    String status;
                    if (avg < 0) {
                        avgStr = "-";
                        status = "No Grades";
                    } else {
                        avgStr = String.format("%.2f", avg);
                        status = (avg >= PASS_THRESHOLD) ? "Passing" : "Failing";
                    }
                    rows.add(new Object[]{
                        s.getStudentNumber(), s.getFullName(),
                        "Year " + s.getYearLevel(), avgStr, status
                    });
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    for (Object[] row : rows) tableModel.addRow(row);
                } catch (Exception ex) {
                    showError("Failed to load students:\n" + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showAddDialog() {
        JTextField numField   = HoloTheme.makeTextField(18);
        JTextField fNameField = HoloTheme.makeTextField(18);
        JTextField lNameField = HoloTheme.makeTextField(18);
        SpinnerNumberModel spinModel = new SpinnerNumberModel(1, 1, 4, 1);
        JSpinner yearSpinner = new JSpinner(spinModel);
        yearSpinner.setFont(HoloTheme.FONT_BODY);
        numField.setToolTipText("e.g. 2024-00123");

        JPanel p = buildFormPanel(
            new String[]{"Student Number:", "First Name:", "Last Name:", "Year Level (1-4):"},
            new JComponent[]{numField, fNameField, lNameField, yearSpinner}
        );

        if (!HoloTheme.showFormDialog(this, "Add New Student", p, "Add")) return;

        String num   = numField.getText().trim();
        String fName = fNameField.getText().trim();
        String lName = lNameField.getText().trim();
        int    year  = (Integer) yearSpinner.getValue();

        if (num.isEmpty() || fName.isEmpty() || lName.isEmpty()) {
            showError("All fields are required."); return;
        }
        if (num.length() > 20) {
            showError("Student Number cannot exceed 20 characters."); return;
        }

        try {
            if (dao.studentNumberExists(num)) {
                showError("Student Number \"" + num + "\" already exists."); return;
            }
            dao.addStudent(num, fName, lName, year);
            loadStudents();
            showInfo("Student added successfully!");
        } catch (Exception ex) {
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a student to edit."); return; }
        if (row >= students.size()) return;
        Student s = students.get(row);

        JTextField fNameField = HoloTheme.makeTextField(18);
        JTextField lNameField = HoloTheme.makeTextField(18);
        fNameField.setText(s.getFirstName());
        lNameField.setText(s.getLastName());

        SpinnerNumberModel spinModel = new SpinnerNumberModel(s.getYearLevel(), 1, 4, 1);
        JSpinner yearSpinner = new JSpinner(spinModel);
        yearSpinner.setFont(HoloTheme.FONT_BODY);

        JTextField numDisplay = HoloTheme.makeTextField(18);
        numDisplay.setText(s.getStudentNumber());
        numDisplay.setEditable(false);
        numDisplay.setForeground(HoloTheme.TEXT_DIM);

        JPanel p = buildFormPanel(
            new String[]{"Student Number (locked):", "First Name:", "Last Name:", "Year Level (1-4):"},
            new JComponent[]{numDisplay, fNameField, lNameField, yearSpinner}
        );

        if (!HoloTheme.showFormDialog(this, "Edit Student", p, "Save")) return;

        String fName = fNameField.getText().trim();
        String lName = lNameField.getText().trim();
        int    year  = (Integer) yearSpinner.getValue();

        if (fName.isEmpty() || lName.isEmpty()) {
            showError("Name fields are required."); return;
        }

        try {
            dao.updateStudent(s.getStudentId(), fName, lName, year);
            loadStudents();
            showInfo("Student updated successfully!");
        } catch (Exception ex) {
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a student to delete."); return; }
        if (row >= students.size()) return;
        Student s = students.get(row);

        if (!showConfirm("Delete Student",
                "Delete student " + s.getFullName() + "?\n" +
                "This will also delete ALL their grade records.", true)) return;

        try {
            dao.deleteStudent(s.getStudentId());
            loadStudents();
            showInfo("Student deleted.");
        } catch (Exception ex) {
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private void showReport() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a student to view their report."); return; }
        if (row >= students.size()) return;
        Student s = students.get(row);
        new StudentReportDialog(this, s, dao, PASS_THRESHOLD).setVisible(true);
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
