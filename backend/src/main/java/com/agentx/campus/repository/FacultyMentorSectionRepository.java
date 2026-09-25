package com.agentx.campus.repository;

import com.agentx.campus.model.FacultyMentorSection;
import com.agentx.campus.model.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyMentorSectionRepository extends JpaRepository<FacultyMentorSection, Long> {
    List<FacultyMentorSection> findByFacultyProfile(FacultyProfile facultyProfile);
    List<FacultyMentorSection> findByFacultyProfileId(Long facultyProfileId);
    List<FacultyMentorSection> findByFacultyProfile_User_Username(String username);
    Optional<FacultyMentorSection> findFirstByDepartmentAndSection(String department, String section);
    Optional<FacultyMentorSection> findFirstByDepartmentAndSectionAndSemester(String department, String section, Integer semester);
    List<FacultyMentorSection> findByDepartmentAndSection(String department, String section);
    boolean existsByFacultyProfileAndDepartmentAndSectionAndSemester(FacultyProfile facultyProfile, String department, String section, Integer semester);
}
