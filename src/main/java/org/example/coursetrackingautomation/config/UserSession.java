package org.example.coursetrackingautomation.config;

import java.util.Optional;
import org.example.coursetrackingautomation.dto.SessionUser;
import org.springframework.stereotype.Component;

@Component
/**
 * Stores the currently authenticated user for the running desktop session.
 *
 * <p>This is a lightweight in-memory session abstraction used by JavaFX controllers/services.
 * It is not a web session and does not provide persistence across application restarts.</p>
 */
public class UserSession {

    private SessionUser currentUser;

    /**
     * Returns the currently authenticated user.
     *
     * @return optional current user
     */
    public Optional<SessionUser> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    /**
     * Sets the current authenticated user.
     *
     * @param user authenticated user representation
     */
    public void setCurrentUser(SessionUser user) {
        this.currentUser = user;
    }

    /**
     * Clears the current session.
     */
    public void cleanUserSession() {
        this.currentUser = null;
    }
}