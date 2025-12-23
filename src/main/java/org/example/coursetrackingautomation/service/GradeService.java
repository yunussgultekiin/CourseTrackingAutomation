package org.example.coursetrackingautomation.service;

import org.springframework.stereotype.Service;

@Service
public class GradeService {

    // 1. Ortalama Hesaplama Mantığı (Vize %40 + Final %60)
    public Double calculateAverage(Double midterm, Double finalScore) {
        if (midterm == null) midterm = 0.0;
        if (finalScore == null) finalScore = 0.0;
        
        return (midterm * 0.4) + (finalScore * 0.6);
    }

    // 2. Harf Notu Belirleme Mantığı
    public String determineLetterGrade(Double average) {
        if (average == null) return "FF";
        
        if (average >= 90) return "AA";
        else if (average >= 85) return "BA";
        else if (average >= 80) return "BB";
        else if (average >= 75) return "CB";
        else if (average >= 70) return "CC";
        else if (average >= 60) return "DC";
        else if (average >= 50) return "DD";
        else if (average >= 40) return "FD";
        else return "FF";
    }

    // 3. Geçti/Kaldı Kontrolü
    public boolean isPassed(String letterGrade) {
        // FF veya FD ise kaldı, diğer durumlarda geçti sayılır.
        return !letterGrade.equals("FF") && !letterGrade.equals("FD");
    }
}