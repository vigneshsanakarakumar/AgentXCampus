package com.agentx.campus.agent;

import com.agentx.campus.model.Notification;
import com.agentx.campus.model.StudentProfile;
import com.agentx.campus.model.User;
import com.agentx.campus.repository.NotificationRepository;
import com.agentx.campus.repository.StudentProfileRepository;
import com.agentx.campus.service.NotificationStreamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationAgent {

    private final NotificationRepository notificationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NotificationStreamService notificationStreamService;

    public NotificationAgent(NotificationRepository notificationRepository,
                             StudentProfileRepository studentProfileRepository,
                             NotificationStreamService notificationStreamService) {
        this.notificationRepository = notificationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.notificationStreamService = notificationStreamService;
    }

    @Transactional
    public Notification notifyUser(User user, String title, String message, String type) {
        return notifyUser(user, title, message, type, null);
    }

    /** Overload with explainability — sets matchedBecause on the notification and pushes SSE real-time alert. */
    @Transactional
    public Notification notifyUser(User user, String title, String message, String type, String matchedBecause) {
        Notification n = new Notification(user, title, message, type);
        if (matchedBecause != null) n.setMatchedBecause(matchedBecause);
        Notification saved = notificationRepository.save(n);

        // Immediate real-time WebSocket / SSE push to active client
        if (user != null && user.getUsername() != null) {
            try {
                notificationStreamService.sendRealtimeNotification(user.getUsername(), saved);
            } catch (Exception ignored) {}
        }

        return saved;
    }

    @Transactional
    public void notifySection(String department, String section, String title, String message, String type) {
        List<StudentProfile> students = studentProfileRepository.findByDepartmentAndSection(department, section);
        String matchedBecause = "Section: " + department + " Sec " + section;
        for (StudentProfile sp : students) {
            if (sp.getUser() != null) {
                notifyUser(sp.getUser(), title, message, type, matchedBecause);
            }
        }
    }

    @Transactional
    public void notifyDepartment(String department, String title, String message, String type) {
        List<StudentProfile> students = studentProfileRepository.findByDepartment(department);
        String matchedBecause = "Department: " + department;
        for (StudentProfile sp : students) {
            if (sp.getUser() != null) {
                notifyUser(sp.getUser(), title, message, type, matchedBecause);
            }
        }
    }

    @Transactional
    public void notifyAllUsers(String title, String message, String type) {
        List<StudentProfile> students = studentProfileRepository.findAll();
        String matchedBecause = "Institution-wide Announcement";
        for (StudentProfile sp : students) {
            if (sp.getUser() != null) {
                notifyUser(sp.getUser(), title, message, type, matchedBecause);
            }
        }
    }

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markAsRead(Long id, User user) {
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }
}
