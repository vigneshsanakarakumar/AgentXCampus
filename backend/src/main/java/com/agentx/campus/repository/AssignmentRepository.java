package com.agentx.campus.repository;

import com.agentx.campus.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByDepartmentAndSectionOrderByDueDateAsc(String department, String section);
    List<Assignment> findByDepartmentOrderByDueDateAsc(String department);
    List<Assignment> findBySubjectCodeOrderByDueDateAsc(String subjectCode);
    List<Assignment> findAllByOrderByDueDateAsc();
}
