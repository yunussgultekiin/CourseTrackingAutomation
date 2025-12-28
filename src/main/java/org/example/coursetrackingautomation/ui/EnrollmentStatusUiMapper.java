package org.example.coursetrackingautomation.ui;

import org.example.coursetrackingautomation.entity.EnrollmentStatus;

/**
 * Maps enrollment domain statuses to Turkish UI text.
 */
public final class EnrollmentStatusUiMapper {

    private EnrollmentStatusUiMapper() {
    }

    public static String toTurkish(EnrollmentStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case ACTIVE -> "Aktif";
            case ENROLLED -> "Kayıtlı";
            case REGISTERED -> "Kesin Kayıt";
            case DROPPED -> "Bıraktı";
            case CANCELLED -> "İptal";
        };
    }

    /**
     * Accepts either an enum name or already-normalized code and returns Turkish UI text.
     */
    public static String toTurkish(String statusCode) {
        if (statusCode == null) {
            return "";
        }
        try {
            return toTurkish(EnrollmentStatus.valueOf(statusCode.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return statusCode;
        }
    }
}
