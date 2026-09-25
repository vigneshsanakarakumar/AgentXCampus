package com.agentx.campus.repository;

import com.agentx.campus.model.FacultyProfile;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, Long> {
    Optional<FacultyProfile> findByUser(User user);
    Optional<FacultyProfile> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    Optional<FacultyProfile> findFirstByAssignedDepartmentAndAssignedSection(String assignedDepartment, String assignedSection);
    Optional<FacultyProfile> findFirstByAssignedDepartmentAndAssignedSectionAndIsMentorTrue(String assignedDepartment, String assignedSection);
    List<FacultyProfile> findByDepartment(String department);
    List<FacultyProfile> findAll();
}
