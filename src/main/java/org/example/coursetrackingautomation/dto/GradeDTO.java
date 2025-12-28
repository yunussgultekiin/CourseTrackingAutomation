package org.example.coursetrackingautomation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Row model used by instructor and transcript-related views.
 *
 * <p>Combines student, course, grade, and attendance indicators into a single display-friendly
 * structure.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeDTO {
    
    private Long studentId;
    private String studentName; 
    private String courseCode;  
    private String courseName;  
    private Integer credit;   
    private Integer weeklyTotalHours;
    private Integer weeklyTheoryHours;
    private Integer weeklyPracticeHours;
    private Double midtermScore;   
    private Double finalScore;   
    private Double averageScore;  
    private String letterGrade;     
    private GradeStatus status;
    private Integer attendanceCount; 
    private boolean absentCritically; 
    private Boolean present;
}