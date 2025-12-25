package org.example.coursetrackingautomation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeDTO {
    
    private Long studentId;
    private String studentName; 
    private String courseCode;  
    private String courseName;  
    private Integer credit;   
    private Double midtermScore;   
    private Double finalScore;   
    private Double averageScore;  
    private String letterGrade;     
    private String status;        
    private Integer attendanceCount; 
    private boolean absentCritically; 
    private Boolean present;
}