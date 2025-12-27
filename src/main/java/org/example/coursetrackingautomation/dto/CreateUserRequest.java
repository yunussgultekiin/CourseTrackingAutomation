package org.example.coursetrackingautomation.dto;

public record CreateUserRequest(
    String username,
    String password,
    String firstName,
    String lastName,
    RoleDTO role,
    String studentNumber,
    String email,
    String phone,
    boolean active
) {
}
