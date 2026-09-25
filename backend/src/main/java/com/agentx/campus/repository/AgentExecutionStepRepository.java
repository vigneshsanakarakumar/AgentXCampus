package com.agentx.campus.repository;

import com.agentx.campus.model.AgentExecutionStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentExecutionStepRepository extends JpaRepository<AgentExecutionStep, Long> {
    List<AgentExecutionStep> findByExecutionPlan_TaskIdOrderByStepNumberAsc(String taskId);
}
