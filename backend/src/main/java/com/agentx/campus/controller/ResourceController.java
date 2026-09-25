package com.agentx.campus.controller;

import com.agentx.campus.model.CampusResource;
import com.agentx.campus.model.TimetableEntry;
import com.agentx.campus.repository.CampusResourceRepository;
import com.agentx.campus.repository.TimetableEntryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    private final CampusResourceRepository resourceRepository;
    private final TimetableEntryRepository timetableEntryRepository;

    public ResourceController(CampusResourceRepository resourceRepository,
                              TimetableEntryRepository timetableEntryRepository) {
        this.resourceRepository = resourceRepository;
        this.timetableEntryRepository = timetableEntryRepository;
    }

    @GetMapping
    public ResponseEntity<List<CampusResource>> getAllResources() {
        return ResponseEntity.ok(resourceRepository.findAll());
    }

    @GetMapping("/availability")
    public ResponseEntity<?> getResourceAvailability() {
        List<CampusResource> resources = resourceRepository.findAll();
        List<TimetableEntry> timetables = timetableEntryRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (CampusResource r : resources) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("name", r.getName());
            map.put("building", r.getBuilding());
            map.put("roomNumber", r.getRoomNumber());
            map.put("type", r.getType());
            map.put("capacity", r.getCapacity());
            map.put("baselineStatus", r.getStatus());

            // Check if any timetable entry is assigned to this room
            Optional<TimetableEntry> assignedClass = timetables.stream()
                    .filter(t -> t.getClassroom() != null && t.getClassroom().equalsIgnoreCase(r.getRoomNumber()))
                    .findFirst();

            if (assignedClass.isPresent()) {
                TimetableEntry tc = assignedClass.get();
                map.put("currentStatus", "OCCUPIED");
                map.put("activeClass", tc.getSubjectCode() + " - " + tc.getSubjectName());
                map.put("scheduledTime", tc.getDayOfWeek() + " " + tc.getStartTime() + " - " + tc.getEndTime());
                map.put("faculty", tc.getFacultyName());
            } else {
                map.put("currentStatus", r.getStatus());
                map.put("activeClass", "None");
                map.put("scheduledTime", "Vacant Slot");
                map.put("faculty", "N/A");
            }

            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}
