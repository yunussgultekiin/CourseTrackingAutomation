package org.example.coursetrackingautomation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Course entity'sini UI'da göstermek için kullanılan DTO sınıfı
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String code;
    private String name;
    private Integer credit;
    private Integer quota;
    private String term;
    private Boolean active;
    
    // Instructor bilgileri
    private Long instructorId;
    private String instructorName;
    
    // İstatistikler (opsiyonel - UI'da gösterim için)
    private Long currentEnrollmentCount;
    private Long availableQuota; // quota - currentEnrollmentCount
}