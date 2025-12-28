package org.example.coursetrackingautomation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Course representation used by the UI and service layer.
 *
 * <p>Includes instructor display data and derived quota-related fields used by dashboards and
 * enrollment screens.
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
    private Integer weeklyTotalHours;
    private Integer weeklyTheoryHours;
    private Integer weeklyPracticeHours;
    private Long instructorId;
    private String instructorName;
    private Long currentEnrollmentCount;
    private Long availableQuota; 
}