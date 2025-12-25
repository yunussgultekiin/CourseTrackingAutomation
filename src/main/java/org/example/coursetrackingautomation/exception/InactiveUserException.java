package org.example.coursetrackingautomation.exception;

public class InactiveUserException extends AuthenticationException {
    public InactiveUserException() {
        super("User is inactive");
    }
}
