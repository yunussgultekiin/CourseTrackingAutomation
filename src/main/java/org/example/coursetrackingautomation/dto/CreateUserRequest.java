package org.example.coursetrackingautomation.dto;

/**
 * Request payload used when creating a new {@code User} account.
 *
 * <p>This DTO is typically created from an admin UI form. Password encoding and business
 * validations are performed in the service layer.
 */
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
