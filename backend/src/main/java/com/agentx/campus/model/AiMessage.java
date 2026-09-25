package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_messages")
public class AiMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AiConversation conversation;

    @Column(nullable = false, length = 20)
    private String sender; // USER or AGENT

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String message;

    private String agentUsed;
    private String sourcesUsed;
    private Double confidence = 0.95;

    private LocalDateTime timestamp = LocalDateTime.now();

    public AiMessage() {}

    public AiMessage(AiConversation conversation, String sender, String message, String agentUsed, String sourcesUsed, Double confidence) {
        this.conversation = conversation;
        this.sender = sender;
        this.message = message;
        this.agentUsed = agentUsed;
        this.sourcesUsed = sourcesUsed;
        this.confidence = confidence;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    @JsonIgnore
    public AiConversation getConversation() { return conversation; }
    public void setConversation(AiConversation conversation) { this.conversation = conversation; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAgentUsed() { return agentUsed; }
    public void setAgentUsed(String agentUsed) { this.agentUsed = agentUsed; }
    public String getSourcesUsed() { return sourcesUsed; }
    public void setSourcesUsed(String sourcesUsed) { this.sourcesUsed = sourcesUsed; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
