package com.agentx.campus.repository;

import com.agentx.campus.model.AgentExecutionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentExecutionPlanRepository extends JpaRepository<AgentExecutionPlan, Long> {
    Optional<AgentExecutionPlan> findByTaskId(String taskId);
    List<AgentExecutionPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AgentExecutionPlan> findByStatusOrderByCreatedAtDesc(String status);
}
