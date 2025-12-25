package org.example.coursetrackingautomation.dto;

public record UpdateUserRequest(
    String firstName,
    String lastName,
    String email,
    String phone,
    String password
) {
}
