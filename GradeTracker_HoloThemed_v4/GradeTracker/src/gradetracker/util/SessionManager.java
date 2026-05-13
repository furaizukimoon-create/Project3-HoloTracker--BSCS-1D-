package gradetracker.util;

import gradetracker.model.User;

/**
 * SessionManager — Tracks the currently logged-in user.
 * Acts like a simple session store for the desktop app.
 */
public class SessionManager {
    private static User currentUser = null;

    public static void login(User user)  { currentUser = user; }
    public static void logout()          { currentUser = null; }
    public static User getCurrentUser()  { return currentUser; }
    public static boolean isLoggedIn()   { return currentUser != null; }
}
