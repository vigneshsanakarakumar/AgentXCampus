package com.agentx.campus.repository;

import com.agentx.campus.model.AiConversation;
import com.agentx.campus.model.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findByConversationOrderByTimestampAsc(AiConversation conversation);
}
