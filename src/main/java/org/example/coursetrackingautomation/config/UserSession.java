package org.example.coursetrackingautomation.config;

import java.util.Optional;
import org.example.coursetrackingautomation.dto.SessionUser;
import org.springframework.stereotype.Component;

@Component
public class UserSession {

    private SessionUser currentUser;

    public Optional<SessionUser> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public void setCurrentUser(SessionUser user) {
        this.currentUser = user;
    }

    public void cleanUserSession() {
        this.currentUser = null;
    }
}