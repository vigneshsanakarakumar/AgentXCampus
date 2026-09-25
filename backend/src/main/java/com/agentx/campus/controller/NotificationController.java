package com.agentx.campus.controller;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.model.Notification;
import com.agentx.campus.model.User;
import com.agentx.campus.repository.UserRepository;
import com.agentx.campus.security.JwtTokenProvider;
import com.agentx.campus.service.NotificationStreamService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationAgent notificationAgent;
    private final UserRepository userRepository;
    private final NotificationStreamService notificationStreamService;
    private final JwtTokenProvider jwtTokenProvider;

    public NotificationController(NotificationAgent notificationAgent,
                                  UserRepository userRepository,
                                  NotificationStreamService notificationStreamService,
                                  JwtTokenProvider jwtTokenProvider) {
        this.notificationAgent = notificationAgent;
        this.userRepository = userRepository;
        this.notificationStreamService = notificationStreamService;
        this.jwtTokenProvider = jwtTokenProvider;
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

    /**
     * Real-time SSE notification stream.
     * Supports authentication via standard Spring Security context or token query parameter for browser EventSource.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@RequestParam(required = false) String token, Authentication auth) {
        String username = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            username = auth.getName();
        } else if (token != null && jwtTokenProvider.validateToken(token)) {
            username = jwtTokenProvider.getUsernameFromToken(token);
        }

        if (username == null) {
            throw new AccessDeniedException("Authentication required for live notification stream");
        }

        return notificationStreamService.registerEmitter(username);
    }
}
