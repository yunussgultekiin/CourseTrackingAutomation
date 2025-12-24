package org.example.coursetrackingautomation.service;

import org.springframework.stereotype.Service;

@Service
public class AttendanceService {

    private static final double CRITICAL_PERCENTAGE = 0.20;

    /**
     * Devamsızlık kritik sınırı aştı mı?
     * @param totalCourseHours Dersin toplam saati (Örn: 14 hafta * 3 saat = 42)
     * @param currentAbsentHours Öğrencinin şu anki devamsızlığı
     * @return Sınır aşıldıysa TRUE döner.
     */
    public boolean isAttendanceCritical(int totalCourseHours, int currentAbsentHours) {
        if (totalCourseHours == 0) return false; // Ders saati 0 ise hesaplanamaz
        
        double absentRatio = (double) currentAbsentHours / totalCourseHours;
        
        return absentRatio >= CRITICAL_PERCENTAGE;
    }
}