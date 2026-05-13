package gradetracker.model;

public class Student {
    private int    studentId;
    private String studentNumber;
    private String firstName;
    private String lastName;
    private int    yearLevel;

    public Student(int studentId, String studentNumber, String firstName, String lastName, int yearLevel) {
        this.studentId     = studentId;
        this.studentNumber = studentNumber;
        this.firstName     = firstName;
        this.lastName      = lastName;
        this.yearLevel     = yearLevel;
    }
    public int    getStudentId()     { return studentId; }
    public String getStudentNumber() { return studentNumber; }
    public String getFirstName()     { return firstName; }
    public String getLastName()      { return lastName; }
    public String getFullName()      { return firstName + " " + lastName; }
    public int    getYearLevel()     { return yearLevel; }
    public void   setFirstName(String v)  { firstName  = v; }
    public void   setLastName(String v)   { lastName   = v; }
    public void   setYearLevel(int v)     { yearLevel  = v; }
}
