package org.example.coursetrackingautomation.dto;

public record SelectOptionDTO(Long id, String label) {
    @Override
    public String toString() {
        return label;
    }
}
