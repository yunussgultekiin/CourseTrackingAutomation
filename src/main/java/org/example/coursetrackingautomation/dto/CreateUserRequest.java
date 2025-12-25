package org.example.coursetrackingautomation.dto;

import org.example.coursetrackingautomation.entity.Role;

public record CreateUserRequest(
    String username,
    String password,
    String firstName,
    String lastName,
    Role role,
    String studentNumber,
    String email,
    String phone,
    boolean active
) {
}
