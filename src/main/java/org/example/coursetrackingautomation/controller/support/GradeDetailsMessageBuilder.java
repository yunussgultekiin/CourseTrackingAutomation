package org.example.coursetrackingautomation.controller.support;

import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.ui.GradeStatusUiMapper;

public final class GradeDetailsMessageBuilder {

    private GradeDetailsMessageBuilder() {
    }

    public static String buildStudentDashboardMessage(GradeDTO item) {
        if (item == null) {
            return "";
        }

        String midtermText = item.getMidtermScore() == null ? "-" : String.valueOf(item.getMidtermScore());
        String finalText = item.getFinalScore() == null ? "-" : String.valueOf(item.getFinalScore());
        String averageText = item.getAverageScore() == null ? "-" : String.valueOf(item.getAverageScore());
        String letterText = item.getLetterGrade() == null ? "-" : item.getLetterGrade();
        String statusText = item.getStatus() == null ? "-" : GradeStatusUiMapper.toTurkish(item.getStatus());

        String weeklyTotal = item.getWeeklyTotalHours() == null ? "-" : item.getWeeklyTotalHours().toString();
        String weeklyTheory = item.getWeeklyTheoryHours() == null ? "-" : item.getWeeklyTheoryHours().toString();
        String weeklyPractice = item.getWeeklyPracticeHours() == null ? "-" : item.getWeeklyPracticeHours().toString();

        return "Ders: " + item.getCourseCode() + " - " + item.getCourseName() + "\n"
            + "Kredi: " + (item.getCredit() == null ? "-" : item.getCredit()) + "\n"
            + "Saat (Haftalık): " + weeklyTotal + " (Teori " + weeklyTheory + ", Uygulama " + weeklyPractice + ")\n"
            + "Vize: " + midtermText + "\n"
            + "Final: " + finalText + "\n"
            + "Ortalama: " + averageText + "\n"
            + "Harf Notu: " + letterText + "\n"
            + "Devamsızlık (Saat): " + (item.getAttendanceCount() == null ? 0 : item.getAttendanceCount()) + "\n"
            + "Durum: " + statusText;
    }

    public static String buildTranscriptPopupMessage(GradeDTO item) {
        if (item == null) {
            return "";
        }

        return "Ders: " + item.getCourseCode() + "\n"
            + "Vize: " + (item.getMidtermScore() == null ? "-" : item.getMidtermScore()) + "\n"
            + "Final: " + (item.getFinalScore() == null ? "-" : item.getFinalScore()) + "\n"
            + "Ortalama: " + (item.getAverageScore() == null ? "-" : item.getAverageScore()) + "\n"
            + "Harf: " + (item.getLetterGrade() == null ? "-" : item.getLetterGrade()) + "\n"
            + "Devamsızlık (Saat): " + (item.getAttendanceCount() == null ? 0 : item.getAttendanceCount()) + "\n"
            + "Saat (Haftalık): "
            + (item.getWeeklyTotalHours() == null ? "-" : item.getWeeklyTotalHours())
            + " (Teori " + (item.getWeeklyTheoryHours() == null ? "-" : item.getWeeklyTheoryHours())
            + ", Uygulama " + (item.getWeeklyPracticeHours() == null ? "-" : item.getWeeklyPracticeHours()) + ")\n"
            + "Durum: " + (item.getStatus() == null ? "-" : GradeStatusUiMapper.toTurkish(item.getStatus()));
    }
}
