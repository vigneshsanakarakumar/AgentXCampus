package com.agentx.campus.repository;

import com.agentx.campus.model.StudentProfile;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser(User user);
    Optional<StudentProfile> findByRollNumber(String rollNumber);
    boolean existsByRollNumber(String rollNumber);
    List<StudentProfile> findByDepartmentAndSection(String department, String section);
    List<StudentProfile> findByDepartment(String department);
    long countByDepartmentAndSection(String department, String section);
}
