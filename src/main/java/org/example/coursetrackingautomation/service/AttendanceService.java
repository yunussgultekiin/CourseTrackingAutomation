package org.example.coursetrackingautomation.service;

import org.springframework.stereotype.Service;

@Service
public class AttendanceService {

    private static final int DEFAULT_TOTAL_COURSE_HOURS = 42;
    private static final double WARNING_PERCENTAGE = 0.10;
    private static final double CRITICAL_PERCENTAGE = 0.20;

    public boolean isAttendanceCritical(int totalCourseHours, int currentAbsentHours) {
        if (totalCourseHours <= 0) {
            return false;
        }

        double absentRatio = (double) currentAbsentHours / (double) totalCourseHours;
        return absentRatio >= CRITICAL_PERCENTAGE;
    }

    public boolean isAttendanceWarning(int totalCourseHours, int currentAbsentHours) {
        if (totalCourseHours <= 0) {
            return false;
        }

        double absentRatio = (double) currentAbsentHours / (double) totalCourseHours;
        return absentRatio >= WARNING_PERCENTAGE && absentRatio < CRITICAL_PERCENTAGE;
    }

    public boolean isAttendanceCritical(int currentAbsentHours) {
        return isAttendanceCritical(DEFAULT_TOTAL_COURSE_HOURS, currentAbsentHours);
    }

    public boolean isAttendanceWarning(int currentAbsentHours) {
        return isAttendanceWarning(DEFAULT_TOTAL_COURSE_HOURS, currentAbsentHours);
    }
}