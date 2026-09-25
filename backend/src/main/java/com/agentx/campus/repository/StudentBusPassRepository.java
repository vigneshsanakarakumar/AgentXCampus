package com.agentx.campus.repository;

import com.agentx.campus.model.StudentBusPass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentBusPassRepository extends JpaRepository<StudentBusPass, Long> {
    List<StudentBusPass> findByStudent_IdAndStatus(Long studentId, String status);
    Optional<StudentBusPass> findFirstByStudent_IdAndStatusOrderByValidFromDesc(Long studentId, String status);
}
