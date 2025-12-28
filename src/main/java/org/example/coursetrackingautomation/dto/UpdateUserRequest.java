package org.example.coursetrackingautomation.dto;

/**
 * Request payload used when updating an existing user profile.
 *
 * <p>This DTO is used by profile/admin edit workflows. Password changes (if provided) are handled
 * by the service layer.
 */
public record UpdateUserRequest(
    String firstName,
    String lastName,
    String email,
    String phone,
    String password
) {
}
