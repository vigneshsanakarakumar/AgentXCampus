package com.agentx.campus.service;

import com.agentx.campus.model.Grievance;
import com.agentx.campus.repository.GrievanceRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StaffService {

    private final GrievanceRepository grievanceRepository;

    public StaffService(GrievanceRepository grievanceRepository) {
        this.grievanceRepository = grievanceRepository;
    }

    public Map<String, Object> getStaffDashboard(String username) {
        Map<String, Object> response = new HashMap<>();
        List<Grievance> tickets = grievanceRepository.findAllByOrderByCreatedAtDesc();

        long openCount = tickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
        long inProgressCount = tickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long resolvedCount = tickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();

        response.put("activeTickets", openCount + inProgressCount);
        response.put("openTickets", openCount);
        response.put("inProgressTickets", inProgressCount);
        response.put("resolvedTickets", resolvedCount);
        response.put("tickets", tickets);

        // Task 4: Replace hardcoded tasks with real maintenance/infrastructure tickets
        List<Map<String, Object>> tasks = tickets.stream()
            .filter(t -> {
                String cat = t.getCategory();
                return cat != null && (
                    cat.equalsIgnoreCase("MAINTENANCE") ||
                    cat.equalsIgnoreCase("CAMPUS_FACILITIES") ||
                    cat.equalsIgnoreCase("CAMPUS_INFRASTRUCTURE") ||
                    cat.equalsIgnoreCase("IT_SUPPORT")
                );
            })
            .limit(10)
            .map(t -> {
                Map<String, Object> task = new HashMap<>();
                task.put("id", t.getId());
                task.put("task", t.getDescription() != null
                    ? (t.getDescription().length() > 100
                        ? t.getDescription().substring(0, 100) + "..." : t.getDescription())
                    : "No description");
                task.put("priority", t.getUrgency() != null ? t.getUrgency() : "MEDIUM");
                task.put("status", t.getStatus() != null ? t.getStatus() : "OPEN");
                task.put("location", t.getLocation());
                task.put("ticketNumber", t.getTicketNumber());
                return task;
            })
            .collect(java.util.stream.Collectors.toList());

        // Fallback: if no maintenance tickets, show open/in-progress tickets
        if (tasks.isEmpty()) {
            tasks = tickets.stream()
                .filter(t -> "OPEN".equals(t.getStatus()) || "IN_PROGRESS".equals(t.getStatus()))
                .limit(5)
                .map(t -> {
                    Map<String, Object> task = new HashMap<>();
                    task.put("id", t.getId());
                    task.put("task", t.getDescription() != null
                        ? (t.getDescription().length() > 100
                            ? t.getDescription().substring(0, 100) + "..." : t.getDescription())
                        : "No description");
                    task.put("priority", t.getUrgency() != null ? t.getUrgency() : "MEDIUM");
                    task.put("status", t.getStatus() != null ? t.getStatus() : "OPEN");
                    task.put("location", t.getLocation());
                    task.put("ticketNumber", t.getTicketNumber());
                    return task;
                })
                .collect(java.util.stream.Collectors.toList());
        }

        response.put("departmentTasks", tasks);

        return response;
    }
}
