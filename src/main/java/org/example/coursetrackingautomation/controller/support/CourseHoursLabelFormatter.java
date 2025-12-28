package org.example.coursetrackingautomation.controller.support;

import org.example.coursetrackingautomation.dto.CourseDTO;

public final class CourseHoursLabelFormatter {

    private CourseHoursLabelFormatter() {
    }

    public static String format(CourseDTO course) {
        if (course == null) {
            return "";
        }

        Integer weeklyTotal = course.getWeeklyTotalHours();
        Integer weeklyTheory = course.getWeeklyTheoryHours();
        Integer weeklyPractice = course.getWeeklyPracticeHours();

        String totalText = weeklyTotal == null ? "-" : weeklyTotal.toString();
        String theoryText = weeklyTheory == null ? "-" : weeklyTheory.toString();
        String practiceText = weeklyPractice == null ? "-" : weeklyPractice.toString();
        return "Saat: " + totalText + " (Teori " + theoryText + ", Uyg. " + practiceText + ")";
    }
}
