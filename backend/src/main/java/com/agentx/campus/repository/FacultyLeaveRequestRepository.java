package com.agentx.campus.repository;

import com.agentx.campus.model.FacultyLeaveRequest;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyLeaveRequestRepository extends JpaRepository<FacultyLeaveRequest, Long> {
    List<FacultyLeaveRequest> findByDepartmentOrderByCreatedAtDesc(String department);
    List<FacultyLeaveRequest> findByFacultyOrderByCreatedAtDesc(User faculty);
    List<FacultyLeaveRequest> findByDepartmentAndStatusOrderByCreatedAtDesc(String department, String status);
}
