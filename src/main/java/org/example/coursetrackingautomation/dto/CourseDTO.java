package org.example.coursetrackingautomation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Long instructorId;
    private String instructorName;
    private Long currentEnrollmentCount;
    private Long availableQuota; 
}