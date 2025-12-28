package org.example.coursetrackingautomation.dto;

/**
 * User profile details returned to UI screens.
 *
 * <p>Intentionally excludes credential fields.
 */
public record UserDetailsDTO(
    Long id,
    String username,
    String firstName,
    String lastName,
    String email,
    String phone
) {
}
