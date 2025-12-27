package org.example.coursetrackingautomation.dto;

public record UserDetailsDTO(
    Long id,
    String username,
    String firstName,
    String lastName,
    String email,
    String phone
) {
}
