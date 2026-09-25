package com.agentx.campus.controller;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.model.Notification;
import com.agentx.campus.model.User;
import com.agentx.campus.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationAgent notificationAgent;
    private final UserRepository userRepository;

    public NotificationController(NotificationAgent notificationAgent, UserRepository userRepository) {
        this.notificationAgent = notificationAgent;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(notificationAgent.getNotificationsForUser(user));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(Authentication authentication, @PathVariable Long id) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        notificationAgent.markAsRead(id, user);
        return ResponseEntity.ok(Map.of("message", "Marked as read", "id", id));
    }
}
