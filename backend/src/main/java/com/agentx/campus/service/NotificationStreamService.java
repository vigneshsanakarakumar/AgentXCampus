package com.agentx.campus.service;

import com.agentx.campus.model.Notification;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationStreamService {

    // Active emitters grouped by username
    private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter registerEmitter(String username) {
        // 30 minute timeout
        SseEmitter emitter = new SseEmitter(1800_000L);

        userEmitters.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(username, emitter));
        emitter.onTimeout(() -> removeEmitter(username, emitter));
        emitter.onError(e -> removeEmitter(username, emitter));

        // Initial handshake event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("status", "CONNECTED", "username", username)));
        } catch (IOException e) {
            removeEmitter(username, emitter);
        }

        return emitter;
    }

    private void removeEmitter(String username, SseEmitter emitter) {
        List<SseEmitter> list = userEmitters.get(username);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                userEmitters.remove(username);
            }
        }
    }

    public void sendRealtimeNotification(String username, Notification notification) {
        List<SseEmitter> list = userEmitters.get(username);
        if (list == null || list.isEmpty()) return;

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", notification.getId());
        payload.put("title", notification.getTitle());
        payload.put("message", notification.getMessage());
        payload.put("type", notification.getType());
        payload.put("matchedBecause", notification.getMatchedBecause() != null ? notification.getMatchedBecause() : "");
        payload.put("createdAt", notification.getCreatedAt().toString());

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(payload));
            } catch (Exception ex) {
                deadEmitters.add(emitter);
            }
        }

        for (SseEmitter dead : deadEmitters) {
            removeEmitter(username, dead);
        }
    }
}
