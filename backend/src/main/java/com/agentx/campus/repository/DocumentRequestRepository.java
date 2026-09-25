package com.agentx.campus.repository;

import com.agentx.campus.model.DocumentRequest;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRequestRepository extends JpaRepository<DocumentRequest, Long> {
    List<DocumentRequest> findByStudentOrderByCreatedAtDesc(User student);
    List<DocumentRequest> findAllByOrderByCreatedAtDesc();
    List<DocumentRequest> findByStatusOrderByCreatedAtDesc(String status);
}
