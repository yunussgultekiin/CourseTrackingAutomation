package org.example.coursetrackingautomation.mapper;

import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.entity.Course;
import org.springframework.stereotype.Component;

/**
 * Course entity ile CourseDTO arasında dönüşüm yapan mapper sınıfı
 */
@Component
public class CourseMapper {
    
    /**
     * Course entity'sini CourseDTO'ya dönüştürür
     * 
     * @param course Dönüştürülecek Course entity
     * @return CourseDTO
     */
    public CourseDTO toDTO(Course course) {
        if (course == null) {
            return null;
        }
        
        return CourseDTO.builder()
            .id(course.getId())
            .code(course.getCode())
            .name(course.getName())
            .credit(course.getCredit())
            .quota(course.getQuota())
            .term(course.getTerm())
            .active(course.isActive())
            .instructorId(course.getInstructor() != null ? course.getInstructor().getId() : null)
            .instructorName(course.getInstructor() != null 
                ? (course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName())
                : null)
            .build();
    }
    
    /**
     * Course entity'sini CourseDTO'ya dönüştürür (mevcut kayıt sayısı ile)
     * 
     * @param course Dönüştürülecek Course entity
     * @param currentEnrollmentCount Mevcut aktif kayıt sayısı
     * @return CourseDTO
     */
    public CourseDTO toDTO(Course course, Long currentEnrollmentCount) {
        CourseDTO dto = toDTO(course);
        
        if (dto != null) {
            dto.setCurrentEnrollmentCount(currentEnrollmentCount);
            if (dto.getQuota() != null && currentEnrollmentCount != null) {
                dto.setAvailableQuota(dto.getQuota() - currentEnrollmentCount);
            }
        }
        
        return dto;
    }
    
    /**
     * CourseDTO'yu Course entity'sine dönüştürür
     * Not: Bu metod genellikle create/update işlemlerinde kullanılır
     * Instructor bilgisi ayrıca set edilmelidir
     * 
     * @param dto Dönüştürülecek CourseDTO
     * @return Course entity (instructor set edilmemiş)
     */
    public Course toEntity(CourseDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return Course.builder()
            .id(dto.getId())
            .code(dto.getCode())
            .name(dto.getName())
            .credit(dto.getCredit())
            .quota(dto.getQuota())
            .term(dto.getTerm())
            .active(dto.getActive() != null ? dto.getActive() : true)
            // instructor ayrıca set edilmeli
            .build();
    }
    
    /**
     * Mevcut Course entity'sini CourseDTO'dan gelen değerlerle günceller
     * 
     * @param existingCourse Mevcut Course entity
     * @param dto Güncelleme için CourseDTO
     */
    public void updateEntityFromDTO(Course existingCourse, CourseDTO dto) {
        if (existingCourse == null || dto == null) {
            return;
        }
        
        if (dto.getCode() != null) {
            existingCourse.setCode(dto.getCode());
        }
        if (dto.getName() != null) {
            existingCourse.setName(dto.getName());
        }
        if (dto.getCredit() != null) {
            existingCourse.setCredit(dto.getCredit());
        }
        if (dto.getQuota() != null) {
            existingCourse.setQuota(dto.getQuota());
        }
        if (dto.getTerm() != null) {
            existingCourse.setTerm(dto.getTerm());
        }
        if (dto.getActive() != null) {
            existingCourse.setActive(dto.getActive());
        }
    }
}

