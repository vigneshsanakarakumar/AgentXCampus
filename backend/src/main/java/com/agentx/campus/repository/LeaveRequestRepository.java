package com.agentx.campus.repository;

import com.agentx.campus.model.LeaveRequest;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStudentOrderByCreatedAtDesc(User student);
    List<LeaveRequest> findByAssignedToUserOrderByCreatedAtDesc(User assignedTo);
    List<LeaveRequest> findByAssignedToRoleOrderByCreatedAtDesc(String role);
}
