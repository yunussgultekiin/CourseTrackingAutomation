package org.example.coursetrackingautomation.exception;

/**
 * Indicates a login attempt for a user account that exists but is not active.
 */
public class InactiveUserException extends AuthenticationException {
	/**
	 * Creates an exception with a default message.
	 */
    public InactiveUserException() {
        super("User is inactive");
    }
}
