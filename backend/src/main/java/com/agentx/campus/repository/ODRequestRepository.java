package com.agentx.campus.repository;

import com.agentx.campus.model.ODRequest;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ODRequestRepository extends JpaRepository<ODRequest, Long> {
    List<ODRequest> findByStudentOrderByCreatedAtDesc(User student);
    List<ODRequest> findByAssignedToUserOrderByCreatedAtDesc(User assignedTo);
    List<ODRequest> findByAssignedToRoleOrderByCreatedAtDesc(String role);
}
