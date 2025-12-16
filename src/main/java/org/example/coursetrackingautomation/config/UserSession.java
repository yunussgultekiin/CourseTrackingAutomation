package org.example.coursetrackingautomation.config;

public class UserSession {
    private static UserSession instance;
    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
}