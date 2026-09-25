package com.agentx.campus.repository;

import com.agentx.campus.model.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findAllByOrderByRequestedAtDesc();
    long countByStatus(String status);
}
