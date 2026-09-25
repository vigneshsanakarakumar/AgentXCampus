package com.agentx.campus.repository;

import com.agentx.campus.model.AgentTaskLog;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentTaskLogRepository extends JpaRepository<AgentTaskLog, Long> {
    List<AgentTaskLog> findTop10ByUserOrderByCreatedAtDesc(User user);
    List<AgentTaskLog> findTop10ByOrderByCreatedAtDesc();
}
