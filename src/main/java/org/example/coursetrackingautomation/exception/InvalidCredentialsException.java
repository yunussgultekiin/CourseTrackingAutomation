package org.example.coursetrackingautomation.exception;

public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
