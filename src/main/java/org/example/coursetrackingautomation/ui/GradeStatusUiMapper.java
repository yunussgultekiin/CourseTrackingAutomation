package org.example.coursetrackingautomation.ui;

import org.example.coursetrackingautomation.dto.GradeStatus;

/**
 * Maps {@link GradeStatus} values to Turkish UI text.
 */
public final class GradeStatusUiMapper {

    private GradeStatusUiMapper() {
    }

    public static String toTurkish(GradeStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case NOT_GRADED -> UiConstants.UI_STATUS_NOT_GRADED;
            case PASSED -> UiConstants.UI_STATUS_PASSED;
            case FAILED -> UiConstants.UI_STATUS_FAILED;
        };
    }
}
