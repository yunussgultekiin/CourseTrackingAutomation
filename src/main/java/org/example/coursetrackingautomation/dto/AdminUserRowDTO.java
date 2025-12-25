package org.example.coursetrackingautomation.dto;

import org.example.coursetrackingautomation.entity.Role;

public record AdminUserRowDTO(
    Long id,
    String username,
    String firstName,
    String lastName,
    Role role,
    String email
) {
}
