package gradetracker.model;

public class Grade {
    private int    gradeId;
    private int    studentId;
    private int    subjectId;
    private double grade;
    private String subjectCode;
    private String subjectName;

    public Grade(int gradeId, int studentId, int subjectId, double grade, String subjectCode, String subjectName) {
        this.gradeId     = gradeId;
        this.studentId   = studentId;
        this.subjectId   = subjectId;
        this.grade       = grade;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
    }
    public int    getGradeId()     { return gradeId; }
    public int    getStudentId()   { return studentId; }
    public int    getSubjectId()   { return subjectId; }
    public double getGrade()       { return grade; }
    public String getSubjectCode() { return subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void   setGrade(double v) { grade = v; }
}
