package com.agentx.campus.repository;

import com.agentx.campus.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
    List<Opportunity> findByIsActiveTrueOrderByCreatedAtDesc();
    List<Opportunity> findByIsActiveTrueAndTypeOrderByCreatedAtDesc(String type);
    List<Opportunity> findAllByOrderByCreatedAtDesc();
}
