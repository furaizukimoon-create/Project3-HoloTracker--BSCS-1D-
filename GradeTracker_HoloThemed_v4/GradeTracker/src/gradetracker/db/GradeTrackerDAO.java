package gradetracker.db;

import gradetracker.model.*;
import gradetracker.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GradeTrackerDAO — All database operations in one place.
 * Uses PreparedStatement everywhere to prevent SQL injection.
 */
public class GradeTrackerDAO {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // ──────────────────────────────────────────────────────────
    // AUTH
    // ──────────────────────────────────────────────────────────

    /** Attempt login. Returns User on success, null on failure. */
    public User login(String username, String plainPassword) throws SQLException {
        String sql = "SELECT user_id, username, password, full_name FROM users WHERE username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (PasswordUtil.verify(plainPassword, storedHash)) {
                    return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"));
                }
            }
        }
        return null;
    }

    // ──────────────────────────────────────────────────────────
    // DASHBOARD STATS
    // ──────────────────────────────────────────────────────────

    public int getTotalStudents() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getTotalSubjects() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM subjects")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Returns [passing, failing] counts based on average grade threshold. */
    public int[] getPassFailCounts(double threshold) throws SQLException {
        String sql = """
            SELECT
                SUM(CASE WHEN avg_grade >= ? THEN 1 ELSE 0 END) AS passing,
                SUM(CASE WHEN avg_grade <  ? THEN 1 ELSE 0 END) AS failing
            FROM (
                SELECT student_id, AVG(grade) AS avg_grade
                FROM grades
                GROUP BY student_id
            ) averages
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, threshold);
            ps.setDouble(2, threshold);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new int[]{ rs.getInt("passing"), rs.getInt("failing") };
            }
        }
        return new int[]{0, 0};
    }

    // ──────────────────────────────────────────────────────────
    // STUDENTS
    // ──────────────────────────────────────────────────────────

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY last_name, first_name";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(
                    rs.getInt("student_id"),
                    rs.getString("student_number"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getInt("year_level")
                ));
            }
        }
        return list;
    }

    public void addStudent(String studentNumber, String firstName, String lastName, int yearLevel) throws SQLException {
        String sql = "INSERT INTO students (student_number, first_name, last_name, year_level) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, studentNumber);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setInt(4, yearLevel);
            ps.executeUpdate();
        }
    }

    public void updateStudent(int studentId, String firstName, String lastName, int yearLevel) throws SQLException {
        String sql = "UPDATE students SET first_name=?, last_name=?, year_level=? WHERE student_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setInt(3, yearLevel);
            ps.setInt(4, studentId);
            ps.executeUpdate();
        }
    }

    public void deleteStudent(int studentId) throws SQLException {
        // grades are cascade-deleted due to ON DELETE CASCADE
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        }
    }

    public boolean studentNumberExists(String number) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE student_number = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, number);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /** Average grade for a student, or -1 if no grades yet. */
    public double getAverage(int studentId) throws SQLException {
        String sql = "SELECT AVG(grade) AS avg FROM grades WHERE student_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble("avg");
                return rs.wasNull() ? -1 : avg;
            }
        }
        return -1;
    }

    // ──────────────────────────────────────────────────────────
    // SUBJECTS
    // ──────────────────────────────────────────────────────────

    public List<Subject> getAllSubjects() throws SQLException {
        List<Subject> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM subjects ORDER BY subject_code")) {
            while (rs.next()) {
                list.add(new Subject(rs.getInt("subject_id"), rs.getString("subject_code"), rs.getString("subject_name")));
            }
        }
        return list;
    }

    public void addSubject(String code, String name) throws SQLException {
        String sql = "INSERT INTO subjects (subject_code, subject_name) VALUES (?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code); ps.setString(2, name); ps.executeUpdate();
        }
    }

    public void updateSubject(int subjectId, String code, String name) throws SQLException {
        String sql = "UPDATE subjects SET subject_code=?, subject_name=? WHERE subject_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code); ps.setString(2, name); ps.setInt(3, subjectId); ps.executeUpdate();
        }
    }

    public void deleteSubject(int subjectId) throws SQLException {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, subjectId); ps.executeUpdate();
        }
    }

    public boolean subjectCodeExists(String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subjects WHERE subject_code = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean subjectHasGrades(int subjectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM grades WHERE subject_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // ──────────────────────────────────────────────────────────
    // GRADES
    // ──────────────────────────────────────────────────────────

    /** Retrieves all grades for a student with subject info (3-table JOIN). */
    public List<Grade> getGradesForStudent(int studentId) throws SQLException {
        List<Grade> list = new ArrayList<>();
        String sql = """
            SELECT g.grade_id, g.student_id, g.subject_id, g.grade,
                   s.subject_code, s.subject_name
            FROM grades g
            JOIN subjects s ON g.subject_id = s.subject_id
            WHERE g.student_id = ?
            ORDER BY s.subject_code
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Grade(
                    rs.getInt("grade_id"), rs.getInt("student_id"), rs.getInt("subject_id"),
                    rs.getDouble("grade"), rs.getString("subject_code"), rs.getString("subject_name")
                ));
            }
        }
        return list;
    }

    public void addGrade(int studentId, int subjectId, double grade) throws SQLException {
        String sql = "INSERT INTO grades (student_id, subject_id, grade) VALUES (?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, subjectId); ps.setDouble(3, grade);
            ps.executeUpdate();
        }
    }

    public void updateGrade(int gradeId, double grade) throws SQLException {
        String sql = "UPDATE grades SET grade=? WHERE grade_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, grade); ps.setInt(2, gradeId); ps.executeUpdate();
        }
    }

    public void deleteGrade(int gradeId) throws SQLException {
        String sql = "DELETE FROM grades WHERE grade_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, gradeId); ps.executeUpdate();
        }
    }

    public boolean gradeExists(int studentId, int subjectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM grades WHERE student_id=? AND subject_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, subjectId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}
