package org.example.coursetrackingautomation.exception;

/**
 * Base type for authentication/authorization-related errors.
 *
 * <p>Thrown from the service layer and typically handled by the UI exception handler to display
 * an appropriate user-facing message.</p>
 */
public class AuthenticationException extends RuntimeException {
    /**
     * Creates a new authentication exception.
     *
     * @param message error message
     */
    public AuthenticationException(String message) {
        super(message);
    }
}
