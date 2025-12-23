package org.example.coursetrackingautomation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeDTO {
    
    private Long studentId;
    private String studentName;      // Öğrenci Adı Soyadı
    private String courseCode;       // Ders Kodu (örn: CSE101)
    private Double midtermScore;     // Vize Notu
    private Double finalScore;       // Final Notu
    private Double averageScore;     // Ortalama
    private String letterGrade;      // Harf Notu (AA, BB...)
    private String status;           // Durum (Geçti/Kaldı)
    private Integer attendanceCount; // Devamsızlık Sayısı
    private boolean isAbsentCritically; // %20 sınırı aşıldı mı? (Kırmızı renk için)
}