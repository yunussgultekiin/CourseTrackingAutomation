package org.example.coursetrackingautomation.dto;

/**
 * Projection used to render user rows in the admin dashboard.
 */
public record AdminUserRowDTO(
    Long id,
    String username,
    String firstName,
    String lastName,
    RoleDTO role,
    String email
) {
}
