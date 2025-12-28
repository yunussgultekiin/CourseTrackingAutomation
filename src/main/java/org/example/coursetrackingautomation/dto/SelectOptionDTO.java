package org.example.coursetrackingautomation.dto;

/**
 * Simple id/label option used by UI selection controls (e.g., ComboBox).
 *
 * <p>{@link #toString()} returns the label to support default JavaFX display rendering.
 */
public record SelectOptionDTO(Long id, String label) {

    /**
     * Returns the label to be displayed by selection controls.
     *
     * @return human-readable label
     */
    @Override
    public String toString() {
        return label;
    }
}
