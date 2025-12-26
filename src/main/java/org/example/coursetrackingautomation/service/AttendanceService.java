package org.example.coursetrackingautomation.service;

import org.example.coursetrackingautomation.entity.Course;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {

    private static final int DEFAULT_TOTAL_COURSE_HOURS = 42;
    private static final int DEFAULT_TERM_WEEKS = 14;
    private static final double WARNING_PERCENTAGE = 0.10;
    private static final double CRITICAL_PERCENTAGE = 0.20;

    /**
     * DB: absenteeismCount is stored as "absence count" (weekly attendance records). UI: display in hours.
     */
    public int toAbsentHours(Course course, Integer absenteeismCount) {
        int count = absenteeismCount == null ? 0 : Math.max(0, absenteeismCount);
        if (course == null || course.getWeeklyTotalHours() == null || course.getWeeklyTotalHours() <= 0) {
            return count;
        }
        return count * course.getWeeklyTotalHours();
    }

    public int toAbsentCount(Course course, Integer absentHours) {
        int hours = absentHours == null ? 0 : Math.max(0, absentHours);
        if (course == null || course.getWeeklyTotalHours() == null || course.getWeeklyTotalHours() <= 0) {
            return hours;
        }
        int weekly = course.getWeeklyTotalHours();
        if (weekly <= 0) {
            return hours;
        }
        return hours / weekly;
    }

    public int getTotalCourseHoursForTerm(Course course) {
        if (course != null && course.getWeeklyTotalHours() != null && course.getWeeklyTotalHours() > 0) {
            return course.getWeeklyTotalHours() * DEFAULT_TERM_WEEKS;
        }
        return DEFAULT_TOTAL_COURSE_HOURS;
    }

    public int getTotalCourseHoursForTerm(Integer weeklyTotalHours) {
        if (weeklyTotalHours != null && weeklyTotalHours > 0) {
            return weeklyTotalHours * DEFAULT_TERM_WEEKS;
        }
        return DEFAULT_TOTAL_COURSE_HOURS;
    }

    public boolean isAttendanceCritical(Course course, Integer absenteeismCount) {
        int totalCourseHours = getTotalCourseHoursForTerm(course);
        int absentHours = toAbsentHours(course, absenteeismCount);
        return isAttendanceCritical(totalCourseHours, absentHours);
    }

    public boolean isAttendanceWarning(Course course, Integer absenteeismCount) {
        int totalCourseHours = getTotalCourseHoursForTerm(course);
        int absentHours = toAbsentHours(course, absenteeismCount);
        return isAttendanceWarning(totalCourseHours, absentHours);
    }

    public boolean isAttendanceCriticalByHours(Course course, int absentHours) {
        int totalCourseHours = getTotalCourseHoursForTerm(course);
        return isAttendanceCritical(totalCourseHours, absentHours);
    }

    public boolean isAttendanceWarningByHours(Course course, int absentHours) {
        int totalCourseHours = getTotalCourseHoursForTerm(course);
        return isAttendanceWarning(totalCourseHours, absentHours);
    }

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