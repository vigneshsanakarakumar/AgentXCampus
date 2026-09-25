package com.agentx.campus.service;

import com.agentx.campus.dto.GrievanceRequest;
import com.agentx.campus.dto.TaskRequest;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final GrievanceRepository grievanceRepository;
    private final CampusEventRepository campusEventRepository;
    private final AnnouncementRepository announcementRepository;
    private final AssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentTaskRepository studentTaskRepository;
    private final FacultyMentorSectionRepository facultyMentorSectionRepository;

    public StudentService(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository,
                          FacultyProfileRepository facultyProfileRepository,
                          TimetableEntryRepository timetableEntryRepository,
                          GrievanceRepository grievanceRepository,
                          CampusEventRepository campusEventRepository,
                          AnnouncementRepository announcementRepository,
                          AssignmentRepository assignmentRepository,
                          AttendanceRecordRepository attendanceRecordRepository,
                          StudentTaskRepository studentTaskRepository,
                          FacultyMentorSectionRepository facultyMentorSectionRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.grievanceRepository = grievanceRepository;
        this.campusEventRepository = campusEventRepository;
        this.announcementRepository = announcementRepository;
        this.assignmentRepository = assignmentRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.studentTaskRepository = studentTaskRepository;
        this.facultyMentorSectionRepository = facultyMentorSectionRepository;
    }

    public Map<String, Object> getStudentDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "role", user.getRole()
        ));

        List<TimetableEntry> timetables = Collections.emptyList();
        List<TimetableEntry> todayClasses = Collections.emptyList();
        Map<String, Object> mentorInfo = null;

        String dept = "Computer Science & Engineering";
        String sec = "C";

        if (profile != null) {
            dept = profile.getDepartment();
            sec = profile.getSection();

            response.put("rollNumber", profile.getRollNumber());
            response.put("department", profile.getDepartment());
            response.put("section", profile.getSection());
            response.put("year", profile.getYear());
            response.put("semester", profile.getSemester());
            response.put("cgpa", profile.getCgpa());
            response.put("attendance", profile.getAttendanceRate());
            response.put("hostel", profile.getHostelBlock() != null ? (profile.getHostelBlock() + " - " + profile.getRoomNumber()) : "Not Assigned");

            // Mentor Lookup: Check FacultyMentorSection mapping table first, fallback to profile
            Optional<FacultyMentorSection> mappingOpt = facultyMentorSectionRepository
                    .findFirstByDepartmentAndSection(profile.getDepartment(), profile.getSection());

            if (mappingOpt.isPresent()) {
                FacultyProfile fp = mappingOpt.get().getFacultyProfile();
                User mentorUser = fp.getUser();
                Map<String, Object> mentorMap = new HashMap<>();
                mentorMap.put("name", (mentorUser != null ? mentorUser.getFirstName() + " " + mentorUser.getLastName() : "Faculty Mentor"));
                mentorMap.put("email", mentorUser != null ? mentorUser.getEmail() : "");
                mentorMap.put("designation", fp.getDesignation() != null ? fp.getDesignation() : "Faculty");
                mentorMap.put("cabinNumber", fp.getCabinNumber() != null ? fp.getCabinNumber() : "Block B");
                mentorMap.put("department", fp.getDepartment());
                mentorMap.put("assignedSection", profile.getSection());
                mentorInfo = mentorMap;
            } else {
                Optional<FacultyProfile> mentorOpt = facultyProfileRepository
                        .findFirstByAssignedDepartmentAndAssignedSection(profile.getDepartment(), profile.getSection());
                if (mentorOpt.isPresent()) {
                    FacultyProfile fp = mentorOpt.get();
                    User mentorUser = fp.getUser();
                    Map<String, Object> mentorMap = new HashMap<>();
                    mentorMap.put("name", (mentorUser != null ? mentorUser.getFirstName() + " " + mentorUser.getLastName() : "Faculty Mentor"));
                    mentorMap.put("email", mentorUser != null ? mentorUser.getEmail() : "");
                    mentorMap.put("designation", fp.getDesignation() != null ? fp.getDesignation() : "Faculty");
                    mentorMap.put("cabinNumber", fp.getCabinNumber() != null ? fp.getCabinNumber() : "Block B");
                    mentorMap.put("department", fp.getDepartment());
                    mentorMap.put("assignedSection", fp.getAssignedSection());
                    mentorInfo = mentorMap;
                }
            }

            // Timetables
            timetables = timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(dept, sec);

            // Today's classes
            String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            if ("Sunday".equalsIgnoreCase(today) || "Saturday".equalsIgnoreCase(today)) {
                today = "Monday"; // Show Monday as preview on weekends
            }
            todayClasses = timetableEntryRepository.findByDepartmentAndSectionAndDayOfWeekOrderByStartTimeAsc(dept, sec, today);
        } else {
            response.put("rollNumber", "N/A");
            response.put("department", dept);
            response.put("section", sec);
            response.put("attendance", 85);
            response.put("cgpa", 8.5);
        }

        response.put("mentor", mentorInfo);
        response.put("timetables", timetables);
        response.put("todayClasses", todayClasses);

        // Next class lookup
        if (!todayClasses.isEmpty()) {
            TimetableEntry next = todayClasses.get(0);
            Map<String, Object> nextClass = new HashMap<>();
            nextClass.put("subject", next.getSubjectName());
            nextClass.put("code", next.getSubjectCode());
            nextClass.put("time", next.getStartTime() + " - " + next.getEndTime());
            nextClass.put("room", next.getClassroom());
            nextClass.put("faculty", next.getFacultyName());
            nextClass.put("day", next.getDayOfWeek());
            response.put("nextClass", nextClass);
        } else if (!timetables.isEmpty()) {
            TimetableEntry next = timetables.get(0);
            Map<String, Object> nextClass = new HashMap<>();
            nextClass.put("subject", next.getSubjectName());
            nextClass.put("code", next.getSubjectCode());
            nextClass.put("time", next.getStartTime() + " - " + next.getEndTime());
            nextClass.put("room", next.getClassroom());
            nextClass.put("faculty", next.getFacultyName());
            nextClass.put("day", next.getDayOfWeek());
            response.put("nextClass", nextClass);
        } else {
            response.put("nextClass", null);
        }

        List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByUserOrderByCourseCodeAsc(user);
        List<Assignment> assignments = assignmentRepository.findByDepartmentAndSectionOrderByDueDateAsc(dept, sec);
        if (assignments.isEmpty()) {
            assignments = assignmentRepository.findByDepartmentOrderByDueDateAsc(dept);
        }

        List<Grievance> grievances = grievanceRepository.findByUserOrderByCreatedAtDesc(user);
        List<CampusEvent> events = campusEventRepository.findForStudent(dept, sec);
        List<Announcement> announcements = announcementRepository.findForStudent(dept, sec);
        List<StudentTask> tasks = studentTaskRepository.findByUserOrderByCreatedAtDesc(user);

        response.put("attendanceRecords", attendanceRecords);
        response.put("assignments", assignments);
        response.put("grievances", grievances);
        response.put("events", events);
        response.put("announcements", announcements);
        response.put("tasks", tasks);

        response.put("upcomingEventsCount", events.size());
        response.put("openRequestsCount", grievances.stream().filter(g -> !"RESOLVED".equals(g.getStatus())).count());
        response.put("pendingAssignmentsCount", assignments.stream().filter(a -> !"SUBMITTED".equals(a.getStatus())).count());
        response.put("pendingTasksCount", tasks.stream().filter(t -> !"COMPLETED".equals(t.getStatus())).count());

        return response;
    }

    public List<TimetableEntry> getTimetable(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for user: " + username));
        return timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(
                profile.getDepartment(), profile.getSection()
        );
    }

    public List<Assignment> getAssignments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);
        if (profile != null) {
            return assignmentRepository.findByDepartmentAndSectionOrderByDueDateAsc(profile.getDepartment(), profile.getSection());
        }
        return assignmentRepository.findAllByOrderByDueDateAsc();
    }

    public List<StudentTask> getTasks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return studentTaskRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public StudentTask createTask(String username, TaskRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        StudentTask task = new StudentTask(
                user,
                req.getTitle(),
                req.getDescription(),
                req.getPriority() != null ? req.getPriority() : "MEDIUM",
                "TODO",
                req.getDueDate() != null ? req.getDueDate() : LocalDate.now().plusDays(3)
        );
        return studentTaskRepository.save(task);
    }

    @Transactional
    public StudentTask updateTaskStatus(Long taskId, String status) {
        StudentTask task = studentTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        task.setStatus(status);
        return studentTaskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        studentTaskRepository.deleteById(taskId);
    }

    @Transactional
    public Grievance createGrievance(String username, GrievanceRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        String ticketNumber = "GRV-" + (1000 + (int)(Math.random() * 9000));
        Grievance g = new Grievance();
        g.setTicketNumber(ticketNumber);
        g.setUser(user);
        g.setCategory(req.getCategory() != null ? req.getCategory() : "GENERAL");
        g.setUrgency(req.getUrgency() != null ? req.getUrgency() : "MEDIUM");
        g.setStatus("OPEN");
        g.setLocation(req.getLocation() != null ? req.getLocation() : "Campus");
        g.setDescription(req.getDescription());
        g.setCreatedAt(LocalDateTime.now());

        // SMART COMPLAINT ROUTING: Assign student complaint to their class mentor
        StudentProfile sp = studentProfileRepository.findByUser(user).orElse(null);
        FacultyProfile mentorFp = null;
        if (sp != null && sp.getDepartment() != null && sp.getSection() != null) {
            List<FacultyMentorSection> mappings = facultyMentorSectionRepository.findByDepartmentAndSection(sp.getDepartment(), sp.getSection());
            if (!mappings.isEmpty() && mappings.get(0).getFacultyProfile() != null) {
                mentorFp = mappings.get(0).getFacultyProfile();
            }
            if (mentorFp == null) {
                mentorFp = facultyProfileRepository.findFirstByAssignedDepartmentAndAssignedSectionAndIsMentorTrue(sp.getDepartment(), sp.getSection()).orElse(null);
            }
        }

        if (mentorFp != null && mentorFp.getUser() != null) {
            User mentorUser = mentorFp.getUser();
            String mentorName = (mentorUser.getFirstName() + " " + mentorUser.getLastName()).trim();
            g.setAssignedToUser(mentorUser);
            g.setAssignedToRole("FACULTY");
            g.setAssignedTo(mentorName + " (Mentor)");
            g.setRoutedTo("FACULTY_MENTOR");
            g.setDepartment(sp.getDepartment() != null ? sp.getDepartment() : "Academic Department");
        } else {
            g.setAssignedToRole("ADMIN");
            g.setAssignedTo("Administration");
            g.setRoutedTo("ADMIN");
            g.setDepartment(req.getDepartment() != null ? req.getDepartment() : "Campus Operations");
        }

        return grievanceRepository.save(g);
    }
}
