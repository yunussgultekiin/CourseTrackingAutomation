package org.example.coursetrackingautomation.service;

import org.springframework.stereotype.Service;

@Service
public class GradeService {

    private static final double MIDTERM_WEIGHT = 0.40;
    private static final double FINAL_WEIGHT = 0.60;

    private static final double THRESHOLD_AA = 90.0;
    private static final double THRESHOLD_BA = 85.0;
    private static final double THRESHOLD_BB = 80.0;
    private static final double THRESHOLD_CB = 75.0;
    private static final double THRESHOLD_CC = 70.0;
    private static final double THRESHOLD_DC = 60.0;
    private static final double THRESHOLD_DD = 50.0;
    private static final double THRESHOLD_FD = 40.0;

    public Double calculateAverage(Double midterm, Double finalScore) {
        if (midterm == null || finalScore == null) {
            return null;
        }

        return (midterm * MIDTERM_WEIGHT) + (finalScore * FINAL_WEIGHT);
    }

    public String determineLetterGrade(Double average) {
        if (average == null) {
            return null;
        }

        if (average >= THRESHOLD_AA) {
            return "AA";
        }
        if (average >= THRESHOLD_BA) {
            return "BA";
        }
        if (average >= THRESHOLD_BB) {
            return "BB";
        }
        if (average >= THRESHOLD_CB) {
            return "CB";
        }
        if (average >= THRESHOLD_CC) {
            return "CC";
        }
        if (average >= THRESHOLD_DC) {
            return "DC";
        }
        if (average >= THRESHOLD_DD) {
            return "DD";
        }
        if (average >= THRESHOLD_FD) {
            return "FD";
        }
        return "FF";
    }

    public boolean isPassed(String letterGrade) {
        if (letterGrade == null || letterGrade.isBlank()) {
            return false;
        }
        return !letterGrade.equals("FF") && !letterGrade.equals("FD");
    }
}