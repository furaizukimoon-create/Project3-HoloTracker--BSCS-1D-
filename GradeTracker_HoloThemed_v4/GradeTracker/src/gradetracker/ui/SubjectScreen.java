package gradetracker.ui;

import gradetracker.db.GradeTrackerDAO;
import gradetracker.model.Subject;
import gradetracker.util.HoloTheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SubjectScreen — Full CRUD for subjects.
 * Warns before deleting a subject that still has grade records.
 */
public class SubjectScreen extends BaseScreen {

    private final GradeTrackerDAO dao = new GradeTrackerDAO();

    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Subject>     subjects = new ArrayList<>();

    public SubjectScreen() {
        initScreen("Subjects");
    }

    @Override protected String getPageTitle()    { return "Subjects"; }
    @Override protected String getPageSubtitle() { return "Manage subjects — Add, edit and delete subject records"; }

    @Override
    protected void buildContent() {
        JButton addBtn     = HoloTheme.makeSuccessButton("+ Add Subject");
        JButton editBtn    = HoloTheme.makePrimaryButton("Edit");
        JButton deleteBtn  = HoloTheme.makeDangerButton("Delete");
        JButton refreshBtn = HoloTheme.makeButton("Refresh", new Color(20, 20, 50), HoloTheme.TEXT_DIM);

        addBtn.setBounds(0,   80, 145, 38);
        editBtn.setBounds(152, 80, 110, 38);
        deleteBtn.setBounds(269, 80, 110, 38);
        refreshBtn.setBounds(386, 80, 110, 38);

        contentPanel.add(addBtn);
        contentPanel.add(editBtn);
        contentPanel.add(deleteBtn);
        contentPanel.add(refreshBtn);

        String[] cols = {"Subject Code", "Subject Name"};
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
        refreshBtn.addActionListener(e -> loadSubjects());

        loadSubjects();
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
    }

    private void loadSubjects() {
        tableModel.setRowCount(0);
        try {
            subjects = dao.getAllSubjects();
            for (Subject s : subjects) {
                tableModel.addRow(new Object[]{ s.getSubjectCode(), s.getSubjectName() });
            }
        } catch (Exception ex) {
            showError("Failed to load subjects:\n" + ex.getMessage());
        }
    }

    private void showAddDialog() {
        JTextField codeField = HoloTheme.makeTextField(18);
        JTextField nameField = HoloTheme.makeTextField(28);
        codeField.setToolTipText("e.g. CC101");

        JPanel p = buildFormPanel(
            new String[]{"Subject Code:", "Subject Name:"},
            new JComponent[]{codeField, nameField}
        );

        if (!HoloTheme.showFormDialog(this, "Add New Subject", p, "Add")) return;

        String code = codeField.getText().trim();
        String name = nameField.getText().trim();

        if (code.isEmpty() || name.isEmpty()) {
            showError("All fields are required."); return;
        }
        if (code.length() > 20) {
            showError("Subject Code cannot exceed 20 characters."); return;
        }
        if (name.length() > 100) {
            showError("Subject Name cannot exceed 100 characters."); return;
        }

        try {
            if (dao.subjectCodeExists(code)) {
                showError("Subject Code \"" + code + "\" already exists."); return;
            }
            dao.addSubject(code, name);
            loadSubjects();
            showInfo("Subject added successfully!");
        } catch (Exception ex) {
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a subject to edit."); return; }
        if (row >= subjects.size()) return;
        Subject s = subjects.get(row);

        JTextField codeField = HoloTheme.makeTextField(18);
        JTextField nameField = HoloTheme.makeTextField(28);
        codeField.setText(s.getSubjectCode());
        nameField.setText(s.getSubjectName());

        JPanel p = buildFormPanel(
            new String[]{"Subject Code:", "Subject Name:"},
            new JComponent[]{codeField, nameField}
        );

        if (!HoloTheme.showFormDialog(this, "Edit Subject", p, "Save")) return;

        String code = codeField.getText().trim();
        String name = nameField.getText().trim();

        if (code.isEmpty() || name.isEmpty()) {
            showError("All fields are required."); return;
        }

        try {
            dao.updateSubject(s.getSubjectId(), code, name);
            loadSubjects();
            showInfo("Subject updated successfully!");
        } catch (Exception ex) {
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a subject to delete."); return; }
        if (row >= subjects.size()) return;
        Subject s = subjects.get(row);

        try {
            if (dao.subjectHasGrades(s.getSubjectId())) {
                if (!showConfirm("Warning — Subject Has Grades",
                        "Subject \"" + s.getSubjectName() + "\" still has grade records.\n" +
                        "Deleting it will also remove all associated grades.", true)) return;
            } else {
                if (!showConfirm("Delete Subject",
                        "Delete subject \"" + s.getSubjectName() + "\"?", true)) return;
            }
            dao.deleteSubject(s.getSubjectId());
            loadSubjects();
            showInfo("Subject deleted.");
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
