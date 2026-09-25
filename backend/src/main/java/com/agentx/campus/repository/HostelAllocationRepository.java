package com.agentx.campus.repository;

import com.agentx.campus.model.HostelAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HostelAllocationRepository extends JpaRepository<HostelAllocation, Long> {
    List<HostelAllocation> findByStudent_IdAndStatus(Long studentId, String status);
    Optional<HostelAllocation> findFirstByStudent_IdAndStatusOrderByFromDateDesc(Long studentId, String status);
    List<HostelAllocation> findByStatusOrderByFromDateDesc(String status);
}
