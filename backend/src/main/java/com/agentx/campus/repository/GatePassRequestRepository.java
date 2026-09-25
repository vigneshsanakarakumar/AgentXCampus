package com.agentx.campus.repository;

import com.agentx.campus.model.GatePassRequest;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GatePassRequestRepository extends JpaRepository<GatePassRequest, Long> {
    List<GatePassRequest> findByStudentOrderByCreatedAtDesc(User student);
    List<GatePassRequest> findByAssignedToRoleOrderByCreatedAtDesc(String role);
    List<GatePassRequest> findAllByOrderByCreatedAtDesc();
}
