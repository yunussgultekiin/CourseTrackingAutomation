package org.example.coursetrackingautomation.service;

import org.example.coursetrackingautomation.entity.Course;
import org.springframework.stereotype.Service;

@Service
/**
 * Provides attendance and absenteeism calculation utilities.
 *
 * <p>The application tracks absenteeism both as "count" (number of missed weeks/sessions) and
 * as "hours" derived from a course's weekly total hours. This service converts between these
 * representations and determines warning/critical thresholds.</p>
 */
public class AttendanceService {

    private static final int DEFAULT_TOTAL_COURSE_HOURS = 42;
    private static final int DEFAULT_TERM_WEEKS = 14;
    private static final double WARNING_PERCENTAGE = 0.10;
    private static final double CRITICAL_PERCENTAGE = 0.20;

    /**
     * Converts an absenteeism count into absent hours using the course's weekly total hours.
     *
     * @param course the course
     * @param absenteeismCount number of missed sessions/weeks
     * @return absent hours
     */
    public int toAbsentHours(Course course, Integer absenteeismCount) {
        int count = absenteeismCount == null ? 0 : Math.max(0, absenteeismCount);
        if (course == null || course.getWeeklyTotalHours() == null || course.getWeeklyTotalHours() <= 0) {
            return count;
        }
        return count * course.getWeeklyTotalHours();
    }

    /**
     * Converts an absenteeism count into absent hours.
     *
     * @param weeklyTotalHours weekly total hours for the course
     * @param absenteeismCount number of missed sessions/weeks
     * @return absent hours
     */
    public int toAbsentHours(Integer weeklyTotalHours, Integer absenteeismCount) {
        int count = absenteeismCount == null ? 0 : Math.max(0, absenteeismCount);
        if (weeklyTotalHours == null || weeklyTotalHours <= 0) {
            return count;
        }
        return count * weeklyTotalHours;
    }

    /**
     * Converts absent hours into an absenteeism count using the course's weekly total hours.
     *
     * @param course the course
     * @param absentHours total absent hours
     * @return derived absenteeism count
     */
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

    /**
     * Converts absent hours into an absenteeism count.
     *
     * @param weeklyTotalHours weekly total hours for the course
     * @param absentHours total absent hours
     * @return derived absenteeism count
     */
    public int toAbsentCount(Integer weeklyTotalHours, Integer absentHours) {
        int hours = absentHours == null ? 0 : Math.max(0, absentHours);
        if (weeklyTotalHours == null || weeklyTotalHours <= 0) {
            return hours;
        }
        return hours / weeklyTotalHours;
    }

    /**
     * Computes total course hours for a standard term, using course-specific weekly hours when available.
     *
     * @param course the course
     * @return total hours for the term
     */
    public int getTotalCourseHoursForTerm(Course course) {
        if (course != null && course.getWeeklyTotalHours() != null && course.getWeeklyTotalHours() > 0) {
            return course.getWeeklyTotalHours() * DEFAULT_TERM_WEEKS;
        }
        return DEFAULT_TOTAL_COURSE_HOURS;
    }

    /**
     * Computes total course hours for a standard term.
     *
     * @param weeklyTotalHours weekly total hours
     * @return total hours for the term
     */
    public int getTotalCourseHoursForTerm(Integer weeklyTotalHours) {
        if (weeklyTotalHours != null && weeklyTotalHours > 0) {
            return weeklyTotalHours * DEFAULT_TERM_WEEKS;
        }
        return DEFAULT_TOTAL_COURSE_HOURS;
    }

    /**
     * Determines whether attendance is in a critical state based on absenteeism count.
     *
     * @param course the course
     * @param absenteeismCount number of missed sessions/weeks
     * @return {@code true} if the critical threshold is met or exceeded
     */
    public boolean isAttendanceCritical(Course course, Integer absenteeismCount) {
        int totalCourseHours = getTotalCourseHoursForTerm(course);
        int absentHours = toAbsentHours(course, absenteeismCount);
        return isAttendanceCritical(totalCourseHours, absentHours);
    }

    /**
     * Determines whether attendance is in a warning state based on absenteeism count.
     *
     * @param course the course
     * @param absenteeismCount number of missed sessions/weeks
     * @return {@code true} if the warning threshold is met
     */
    public boolean isAttendanceWarning(Course course, Integer absenteeismCount) {
        int totalCourseHours = getTotalCourseHoursForTerm(course);
        int absentHours = toAbsentHours(course, absenteeismCount);
        return isAttendanceWarning(totalCourseHours, absentHours);
    }

    /**
     * Determines whether attendance is in a critical state based on absent hours.
     *
     * @param course the course
     * @param absentHours total absent hours
     * @return {@code true} if the critical threshold is met or exceeded
     */
    public boolean isAttendanceCriticalByHours(Course course, int absentHours) {
        int totalCourseHours = getTotalCourseHoursForTerm(course);
        return isAttendanceCritical(totalCourseHours, absentHours);
    }

    /**
     * Determines whether attendance is in a critical state based on absent hours.
     *
     * @param weeklyTotalHours weekly total hours for the course
     * @param absentHours total absent hours
     * @return {@code true} if the critical threshold is met or exceeded
     */
    public boolean isAttendanceCriticalByHours(Integer weeklyTotalHours, int absentHours) {
        int totalCourseHours = getTotalCourseHoursForTerm(weeklyTotalHours);
        return isAttendanceCritical(totalCourseHours, absentHours);
    }

    /**
     * Determines whether attendance is in a warning state based on absent hours.
     *
     * @param course the course
     * @param absentHours total absent hours
     * @return {@code true} if the warning threshold is met
     */
    public boolean isAttendanceWarningByHours(Course course, int absentHours) {
        int totalCourseHours = getTotalCourseHoursForTerm(course);
        return isAttendanceWarning(totalCourseHours, absentHours);
    }

    /**
     * Determines whether attendance is in a warning state based on absent hours.
     *
     * @param weeklyTotalHours weekly total hours for the course
     * @param absentHours total absent hours
     * @return {@code true} if the warning threshold is met
     */
    public boolean isAttendanceWarningByHours(Integer weeklyTotalHours, int absentHours) {
        int totalCourseHours = getTotalCourseHoursForTerm(weeklyTotalHours);
        return isAttendanceWarning(totalCourseHours, absentHours);
    }

    /**
     * Evaluates the critical threshold using raw hour inputs.
     *
     * @param totalCourseHours total course hours for the term
     * @param currentAbsentHours current absent hours
     * @return {@code true} if absent hours ratio is greater than or equal to the critical threshold
     */
    public boolean isAttendanceCritical(int totalCourseHours, int currentAbsentHours) {
        if (totalCourseHours <= 0) {
            return false;
        }

        double absentRatio = (double) currentAbsentHours / (double) totalCourseHours;
        return absentRatio >= CRITICAL_PERCENTAGE;
    }

    /**
     * Evaluates the warning threshold using raw hour inputs.
     *
     * @param totalCourseHours total course hours for the term
     * @param currentAbsentHours current absent hours
     * @return {@code true} if absent hours ratio is within the warning band
     */
    public boolean isAttendanceWarning(int totalCourseHours, int currentAbsentHours) {
        if (totalCourseHours <= 0) {
            return false;
        }

        double absentRatio = (double) currentAbsentHours / (double) totalCourseHours;
        return absentRatio >= WARNING_PERCENTAGE && absentRatio < CRITICAL_PERCENTAGE;
    }

    /**
     * Convenience overload using default total course hours.
     *
     * @param currentAbsentHours current absent hours
     * @return {@code true} if critical threshold is met
     */
    public boolean isAttendanceCritical(int currentAbsentHours) {
        return isAttendanceCritical(DEFAULT_TOTAL_COURSE_HOURS, currentAbsentHours);
    }

    /**
     * Convenience overload using default total course hours.
     *
     * @param currentAbsentHours current absent hours
     * @return {@code true} if warning threshold is met
     */
    public boolean isAttendanceWarning(int currentAbsentHours) {
        return isAttendanceWarning(DEFAULT_TOTAL_COURSE_HOURS, currentAbsentHours);
    }
}