package gradetracker.model;

public class Subject {
    private int    subjectId;
    private String subjectCode;
    private String subjectName;

    public Subject(int subjectId, String subjectCode, String subjectName) {
        this.subjectId   = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
    }
    public int    getSubjectId()   { return subjectId; }
    public String getSubjectCode() { return subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void   setSubjectCode(String v) { subjectCode = v; }
    public void   setSubjectName(String v) { subjectName = v; }

    @Override
    public String toString() { return subjectCode + " — " + subjectName; }
}
