package com.agentx.campus.service;
import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.dto.CourseworkRequestDto;
import com.agentx.campus.dto.GrievanceRequest;
import com.agentx.campus.dto.TimetableRequest;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FacultyService {

    private final UserRepository userRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final AnnouncementRepository announcementRepository;
    private final FacultyMentorSectionRepository facultyMentorSectionRepository;
    private final GrievanceRepository grievanceRepository;
    private final CampusToolRegistry campusToolRegistry;
    private final NotificationAgent notificationAgent;

    public FacultyService(UserRepository userRepository,
                          FacultyProfileRepository facultyProfileRepository,
                          StudentProfileRepository studentProfileRepository,
                          TimetableEntryRepository timetableEntryRepository,
                          AnnouncementRepository announcementRepository,
                          FacultyMentorSectionRepository facultyMentorSectionRepository,
                          GrievanceRepository grievanceRepository,
                          CampusToolRegistry campusToolRegistry,
                          NotificationAgent notificationAgent) {
        this.userRepository = userRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.announcementRepository = announcementRepository;
        this.facultyMentorSectionRepository = facultyMentorSectionRepository;
        this.grievanceRepository = grievanceRepository;
        this.campusToolRegistry = campusToolRegistry;
        this.notificationAgent = notificationAgent;
    }

    public Map<String, Object> getFacultyDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        FacultyProfile profile = facultyProfileRepository.findByUser(user).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "role", user.getRole()
        ));

        String assignedDept = profile != null ? profile.getAssignedDepartment() : null;
        String assignedSec = profile != null ? profile.getAssignedSection() : null;

        response.put("employeeId", profile != null ? profile.getEmployeeId() : "N/A");
        response.put("department", profile != null ? profile.getDepartment() : "Unassigned");
        response.put("designation", profile != null ? profile.getDesignation() : "Faculty");
        response.put("assignedDepartment", assignedDept);
        response.put("assignedSection", assignedSec);
        response.put("isMentor", profile != null && profile.isMentor());
        response.put("cabinNumber", profile != null ? profile.getCabinNumber() : "Faculty Cabin");

        List<Map<String, Object>> menteesList = new ArrayList<>();
        List<TimetableEntry> timetables = Collections.emptyList();

        if (assignedDept != null && assignedSec != null) {
            List<StudentProfile> students = studentProfileRepository.findByDepartmentAndSection(assignedDept, assignedSec);
            for (StudentProfile sp : students) {
                User su = sp.getUser();
                Map<String, Object> sMap = new HashMap<>();
                sMap.put("id", sp.getId());
                sMap.put("userId", su != null ? su.getId() : null);
                sMap.put("name", (su != null ? su.getFirstName() + " " + su.getLastName() : "Student"));
                sMap.put("username", su != null ? su.getUsername() : "");
                sMap.put("email", su != null ? su.getEmail() : "");
                sMap.put("rollNumber", sp.getRollNumber());
                sMap.put("department", sp.getDepartment());
                sMap.put("section", sp.getSection());
                sMap.put("cgpa", sp.getCgpa());
                sMap.put("attendance", sp.getAttendanceRate());
                menteesList.add(sMap);
            }

            timetables = timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(assignedDept, assignedSec);
        }

        response.put("mentees", menteesList);
        response.put("totalStudents", menteesList.size());
        response.put("timetables", timetables);

        // Current day timetable count
        String currentDay = LocalDate.now().getDayOfWeek().toString(); // e.g. MONDAY
        long todayCount = timetables.stream()
                .filter(t -> t.getDayOfWeek() != null && t.getDayOfWeek().equalsIgnoreCase(currentDay))
                .count();
        response.put("todaysClasses", todayCount > 0 ? todayCount : (long) timetables.size());

        List<FacultyMentorSection> mentorSections = profile != null
                ? facultyMentorSectionRepository.findByFacultyProfile(profile)
                : Collections.emptyList();
        response.put("mentorSections", mentorSections);

        List<Announcement> announcements = announcementRepository.findAllByOrderByCreatedAtDesc();
        response.put("announcements", announcements);

        return response;
    }

    public List<FacultyMentorSection> getFacultyMentorSections(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        FacultyProfile profile = facultyProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Faculty profile not found for user: " + username));
        List<FacultyMentorSection> sections = facultyMentorSectionRepository.findByFacultyProfile(profile);
        if (sections.isEmpty() && Boolean.TRUE.equals(profile.isMentor())
                && profile.getAssignedDepartment() != null && profile.getAssignedSection() != null) {
            FacultyMentorSection fms = new FacultyMentorSection(
                    profile,
                    profile.getAssignedDepartment(),
                    profile.getAssignedSection(),
                    5,
                    "2025-2026"
            );
            fms = facultyMentorSectionRepository.save(fms);
            sections = new ArrayList<>();
            sections.add(fms);
        }
        return sections;
    }

    public List<Map<String, Object>> getMenteesBySection(String username, Long sectionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        FacultyProfile profile = facultyProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Faculty profile not found for user: " + username));

        FacultyMentorSection mapping = facultyMentorSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor section assignment not found: " + sectionId));

        if (!mapping.getFacultyProfile().getId().equals(profile.getId())) {
            throw new AccessDeniedException("Unauthorized: You do not mentor this section.");
        }

        List<StudentProfile> students = studentProfileRepository.findByDepartmentAndSection(mapping.getDepartment(), mapping.getSection());
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentProfile sp : students) {
            User su = sp.getUser();
            Map<String, Object> map = new HashMap<>();
            map.put("id", sp.getId());
            map.put("name", su != null ? su.getFirstName() + " " + su.getLastName() : "Student");
            map.put("username", su != null ? su.getUsername() : "");
            map.put("email", su != null ? su.getEmail() : "");
            map.put("rollNumber", sp.getRollNumber());
            map.put("department", sp.getDepartment());
            map.put("section", sp.getSection());
            map.put("semester", sp.getSemester());
            map.put("cgpa", sp.getCgpa());
            map.put("attendance", sp.getAttendanceRate());
            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> getMentees(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        FacultyProfile profile = facultyProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Faculty profile not found for user: " + username));

        if (profile.getAssignedDepartment() == null || profile.getAssignedSection() == null) {
            return Collections.emptyList();
        }

        List<StudentProfile> students = studentProfileRepository.findByDepartmentAndSection(
                profile.getAssignedDepartment(), profile.getAssignedSection()
        );
        List<Map<String, Object>> list = new ArrayList<>();
        for (StudentProfile sp : students) {
            User su = sp.getUser();
            Map<String, Object> map = new HashMap<>();
            map.put("id", sp.getId());
            map.put("name", su != null ? su.getFirstName() + " " + su.getLastName() : "Student");
            map.put("rollNumber", sp.getRollNumber());
            map.put("department", sp.getDepartment());
            map.put("section", sp.getSection());
            map.put("cgpa", sp.getCgpa());
            map.put("attendance", sp.getAttendanceRate());
            map.put("email", su != null ? su.getEmail() : "");
            list.add(map);
        }
        return list;
    }

    public List<TimetableEntry> getMyTimetable(String username) {
        return campusToolRegistry.getMyTimetable(username);
    }

    public List<TimetableEntry> getSectionTimetable(Long sectionId, String username) {
        return campusToolRegistry.getSectionTimetable(sectionId, username);
    }

    public Map<String, Object> addSectionTimetablePeriod(Long sectionId, TimetableRequest req, String username) {
        return campusToolRegistry.addSectionTimetablePeriod(sectionId, req, username);
    }

    public Map<String, Object> updateSectionTimetable(Long sectionId, Long entryId, TimetableRequest req, String username) {
        return campusToolRegistry.updateSectionTimetable(sectionId, entryId, req, username);
    }

    public void deleteSectionTimetablePeriod(Long sectionId, Long entryId, String username) {
        campusToolRegistry.deleteSectionTimetablePeriod(sectionId, entryId, username);
    }

    public List<Map<String, Object>> checkTimetableConflicts(TimetableRequest req, Long excludeId) {
        return campusToolRegistry.checkTimetableConflicts(req, excludeId);
    }

    public List<TimetableEntry> getTimetable(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        FacultyProfile profile = facultyProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Faculty profile not found for user: " + username));

        if (profile.getAssignedDepartment() == null || profile.getAssignedSection() == null) {
            return Collections.emptyList();
        }

        return timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(
                profile.getAssignedDepartment(), profile.getAssignedSection()
        );
    }

    @Transactional
    public TimetableEntry createTimetableEntry(String username, TimetableRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        FacultyProfile profile = facultyProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Faculty profile not found for user: " + username));

        String assignedDept = profile.getAssignedDepartment();
        String assignedSec = profile.getAssignedSection();

        if (assignedDept == null || assignedSec == null) {
            throw new AccessDeniedException("Faculty is not yet assigned to any Department/Section by the Administrator.");
        }

        // Strict Server-Side Authorization: faculty can ONLY create timetable for assigned dept & section
        if (!assignedDept.equalsIgnoreCase(req.getDepartment().trim()) ||
            !assignedSec.equalsIgnoreCase(req.getSection().trim())) {
            throw new AccessDeniedException("Unauthorized: You are only permitted to manage timetables for "
                    + assignedDept + " - Section " + assignedSec);
        }

        TimetableEntry entry = new TimetableEntry();
        entry.setDepartment(assignedDept);
        entry.setSection(assignedSec.toUpperCase());
        entry.setSemester(req.getSemester() > 0 ? req.getSemester() : 1);
        entry.setSubjectCode(req.getSubjectCode().trim().toUpperCase());
        entry.setSubjectName(req.getSubjectName().trim());
        entry.setFacultyName(req.getFacultyName() != null && !req.getFacultyName().trim().isEmpty() 
                ? req.getFacultyName().trim() 
                : (user.getFirstName() + " " + user.getLastName()));
        entry.setFacultyUserId(user.getId());
        entry.setDayOfWeek(req.getDayOfWeek() != null ? req.getDayOfWeek().trim() : "Monday");
        entry.setStartTime(req.getStartTime() != null ? req.getStartTime().trim() : "09:00 AM");
        entry.setEndTime(req.getEndTime() != null ? req.getEndTime().trim() : "10:00 AM");
        entry.setClassroom(req.getClassroom() != null ? req.getClassroom().trim() : "Room 101");

        return timetableEntryRepository.save(entry);
    }

    @Transactional
    public void deleteTimetableEntry(String username, Long timetableId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        FacultyProfile profile = facultyProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Faculty profile not found for user: " + username));

        TimetableEntry entry = timetableEntryRepository.findById(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found: " + timetableId));

        if (!entry.getDepartment().equalsIgnoreCase(profile.getAssignedDepartment()) ||
            !entry.getSection().equalsIgnoreCase(profile.getAssignedSection())) {
            throw new AccessDeniedException("Unauthorized: You cannot delete timetables belonging to other departments or sections.");
        }

        timetableEntryRepository.delete(entry);
    }

    public List<Map<String, Object>> getClassRoster(Long sectionId, String username) {
        return campusToolRegistry.getClassRoster(sectionId, username);
    }

    public Map<String, Object> getClassAttendanceSummary(Long sectionId, String username) {
        return campusToolRegistry.getClassAttendanceSummary(sectionId, username);
    }

    public Assignment assignCourseworkToSection(Long sectionId, CourseworkRequestDto req, String username) {
        return campusToolRegistry.assignCourseworkToSection(sectionId, req, username);
    }

    public List<Assignment> getSectionAssignments(Long sectionId, String username) {
        return campusToolRegistry.getSectionAssignments(sectionId, username);
    }

    // --- MENTOR GRIEVANCE & COMPLAINT OPERATIONS ---

    public List<Grievance> getMentorComplaints(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        FacultyProfile profile = facultyProfileRepository.findByUser(user).orElse(null);

        // 1. Complaints directly assigned to this mentor user
        List<Grievance> direct = grievanceRepository.findByAssignedToUserOrderByCreatedAtDesc(user);
        Set<Long> seenIds = new HashSet<>();
        List<Grievance> results = new ArrayList<>();
        for (Grievance g : direct) {
            seenIds.add(g.getId());
            results.add(g);
        }

        // 2. Complaints submitted by students in mentor's assigned section(s)
        if (profile != null) {
            List<FacultyMentorSection> sections = facultyMentorSectionRepository.findByFacultyProfile(profile);
            for (FacultyMentorSection sec : sections) {
                List<StudentProfile> students = studentProfileRepository.findByDepartmentAndSection(sec.getDepartment(), sec.getSection());
                for (StudentProfile sp : students) {
                    if (sp.getUser() != null) {
                        List<Grievance> studentGrievances = grievanceRepository.findByUserOrderByCreatedAtDesc(sp.getUser());
                        for (Grievance g : studentGrievances) {
                            if (!seenIds.contains(g.getId())) {
                                seenIds.add(g.getId());
                                results.add(g);
                            }
                        }
                    }
                }
            }
        }

        results.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return results;
    }

    @Transactional
    public Grievance updateGrievanceStatus(Long grievanceId, String newStatus, String resolutionNotes, String facultyUsername) {
        User facultyUser = userRepository.findByUsername(facultyUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + facultyUsername));
        FacultyProfile profile = facultyProfileRepository.findByUser(facultyUser).orElse(null);

        Grievance g = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + grievanceId));

        // Security check: is faculty the assigned resolver or mentor of filer's section?
        boolean isAssigned = g.getAssignedToUser() != null && g.getAssignedToUser().getId().equals(facultyUser.getId());
        boolean isMentorOfStudent = false;
        if (!isAssigned && profile != null && g.getUser() != null) {
            StudentProfile sp = studentProfileRepository.findByUser(g.getUser()).orElse(null);
            if (sp != null && sp.getDepartment() != null && sp.getSection() != null) {
                List<FacultyMentorSection> sections = facultyMentorSectionRepository.findByFacultyProfile(profile);
                isMentorOfStudent = sections.stream().anyMatch(s ->
                        s.getDepartment().equalsIgnoreCase(sp.getDepartment()) &&
                        s.getSection().equalsIgnoreCase(sp.getSection())
                );
            }
        }

        if (!isAssigned && !isMentorOfStudent && facultyUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Unauthorized: You are not authorized to resolve this complaint.");
        }

        if (newStatus != null && !newStatus.trim().isEmpty()) {
            String upper = newStatus.trim().toUpperCase();
            g.setStatus(upper);
            if ("RESOLVED".equals(upper)) {
                g.setResolvedAt(LocalDateTime.now());
                if (g.getUser() != null) {
                    try {
                        String notes = (resolutionNotes != null && !resolutionNotes.trim().isEmpty())
                                ? " Remarks: " + resolutionNotes.trim() : "";
                        notificationAgent.notifyUser(
                                g.getUser(),
                                "Complaint #" + g.getTicketNumber() + " Resolved",
                                "Your grievance regarding '" + (g.getDescription() != null ? g.getDescription() : "your report")
                                        + "' has been marked RESOLVED by your mentor." + notes,
                                "GRIEVANCE_RESOLVED"
                        );
                    } catch (Exception ignored) {}
                }
            }
        }

        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            g.setResolutionNotes(resolutionNotes.trim());
        }

        return grievanceRepository.save(g);
    }

    @Transactional
    public Grievance createFacultyGrievance(String facultyUsername, GrievanceRequest req) {
        return campusToolRegistry.createGrievance(
                facultyUsername,
                req.getCategory(),
                req.getLocation(),
                req.getDescription(),
                req.getUrgency(),
                req.getDepartment()
        );
    }
}
