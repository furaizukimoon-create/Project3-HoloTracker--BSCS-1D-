package gradetracker.model;

public class User {
    private int    userId;
    private String username;
    private String fullName;

    public User(int userId, String username, String fullName) {
        this.userId   = userId;
        this.username = username;
        this.fullName = fullName;
    }
    public int    getUserId()   { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
}
