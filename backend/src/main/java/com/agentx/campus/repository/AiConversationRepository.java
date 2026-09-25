package com.agentx.campus.repository;

import com.agentx.campus.model.AiConversation;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByUserOrderByUpdatedAtDesc(User user);
}
