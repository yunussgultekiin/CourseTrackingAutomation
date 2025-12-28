package org.example.coursetrackingautomation.exception;

/**
 * Indicates a failed login attempt due to invalid or missing credentials.
 */
public class InvalidCredentialsException extends AuthenticationException {
	/**
	 * Creates an exception with a default message.
	 */
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
