package com.agentx.campus.service;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.dto.CourseworkRequestDto;
import com.agentx.campus.dto.TimetableRequest;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class CampusToolRegistry {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final AnnouncementRepository announcementRepository;
    private final CampusEventRepository campusEventRepository;
    private final CampusResourceRepository campusResourceRepository;
    private final StudentTaskRepository studentTaskRepository;
    private final GrievanceRepository grievanceRepository;
    private final RagService ragService;
    private final FacultyMentorSectionRepository facultyMentorSectionRepository;
    private final ConflictEngine conflictEngine;
    private final NotificationAgent notificationAgent;
    private final AttendanceEntryRepository attendanceEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ODRequestRepository odRequestRepository;

    public CampusToolRegistry(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            FacultyProfileRepository facultyProfileRepository,
            CourseRepository courseRepository,
            AssignmentRepository assignmentRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            TimetableEntryRepository timetableEntryRepository,
            AnnouncementRepository announcementRepository,
            CampusEventRepository campusEventRepository,
            CampusResourceRepository campusResourceRepository,
            StudentTaskRepository studentTaskRepository,
            GrievanceRepository grievanceRepository,
            RagService ragService,
            FacultyMentorSectionRepository facultyMentorSectionRepository,
            ConflictEngine conflictEngine,
            NotificationAgent notificationAgent,
            AttendanceEntryRepository attendanceEntryRepository,
            LeaveRequestRepository leaveRequestRepository,
            ODRequestRepository odRequestRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.announcementRepository = announcementRepository;
        this.campusEventRepository = campusEventRepository;
        this.campusResourceRepository = campusResourceRepository;
        this.studentTaskRepository = studentTaskRepository;
        this.grievanceRepository = grievanceRepository;
        this.ragService = ragService;
        this.facultyMentorSectionRepository = facultyMentorSectionRepository;
        this.conflictEngine = conflictEngine;
        this.notificationAgent = notificationAgent;
        this.attendanceEntryRepository = attendanceEntryRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.odRequestRepository = odRequestRepository;
    }

    public Map<String, Object> getStudentProfile(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Collections.emptyMap();

        Map<String, Object> map = new HashMap<>();
        map.put("name", user.getFirstName() + " " + user.getLastName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole().toString());

        StudentProfile sp = studentProfileRepository.findByUser(user).orElse(null);
        if (sp != null) {
            map.put("rollNumber", sp.getRollNumber());
            map.put("department", sp.getDepartment());
            map.put("section", sp.getSection());
            map.put("year", sp.getYear());
            map.put("semester", sp.getSemester());
            map.put("cgpa", sp.getCgpa());
            map.put("attendanceRate", sp.getAttendanceRate());
            map.put("hostel", sp.getHostelBlock() + " - " + sp.getRoomNumber());
        }
        return map;
    }

    public List<AttendanceRecord> getAttendance(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Collections.emptyList();
        return attendanceRecordRepository.findByUserOrderByCourseCodeAsc(user);
    }

    public List<Assignment> getAssignments(String department, String section) {
        if (department == null || department.isEmpty()) {
            return assignmentRepository.findAllByOrderByDueDateAsc();
        }
        if (section == null || section.isEmpty()) {
            return assignmentRepository.findByDepartmentOrderByDueDateAsc(department);
        }
        return assignmentRepository.findByDepartmentAndSectionOrderByDueDateAsc(department, section);
    }

    public List<TimetableEntry> getTodaySchedule(String department, String section) {
        String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        // If Sunday, show Monday schedule preview
        if ("Sunday".equalsIgnoreCase(today)) {
            today = "Monday";
        }
        return timetableEntryRepository.findByDepartmentAndSectionAndDayOfWeekOrderByStartTimeAsc(department, section, today);
    }

    public List<TimetableEntry> getWeeklySchedule(String department, String section) {
        return timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(department, section);
    }

    public List<Announcement> getNotices() {
        return announcementRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public List<CampusEvent> getUpcomingEvents() {
        return campusEventRepository.findAllByOrderByEventDateAsc();
    }

    public List<Course> getCourses(String department) {
        if (department == null || department.isEmpty()) {
            return courseRepository.findAll();
        }
        return courseRepository.findByDepartmentOrderBySemesterAscCourseCodeAsc(department);
    }

    public List<RagService.RagChunk> searchKnowledgeBase(String query) {
        return ragService.retrieveRelevantChunks(query, 3);
    }

    public List<StudentTask> getStudentTasks(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Collections.emptyList();
        return studentTaskRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public StudentTask createStudentTask(String username, String title, String priority, LocalDate dueDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        StudentTask task = new StudentTask(user, title, "Created by AgentX Campus Planner", priority != null ? priority : "MEDIUM", "TODO", dueDate);
        return studentTaskRepository.save(task);
    }

    public List<CampusResource> getCampusResources() {
        return campusResourceRepository.findAll();
    }

    @Transactional
    public Grievance createGrievance(String username, String category, String location, String description) {
        return createGrievance(username, category, location, description, "HIGH", null);
    }

    @Transactional
    public Grievance createGrievance(String username, String category, String location, String description, String urgency, String department) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        String ticketNumber = "GRV-" + (1000 + (int)(Math.random() * 9000));
        Grievance g = new Grievance();
        g.setTicketNumber(ticketNumber);
        g.setUser(user);
        g.setCategory(category != null && !category.trim().isEmpty() ? category.trim() : "CAMPUS_FACILITIES");
        g.setUrgency(urgency != null && !urgency.trim().isEmpty() ? urgency.trim().toUpperCase() : "HIGH");
        g.setStatus("OPEN");
        g.setLocation(location != null && !location.trim().isEmpty() ? location.trim() : "Campus");
        g.setDescription(description != null ? description.trim() : "");
        g.setCreatedAt(LocalDateTime.now());

        // SMART COMPLAINT ROUTING LOGIC:
        if (user.getRole() == Role.STUDENT) {
            // Look up student's department, section, and class mentor
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
                g.setDepartment(department != null ? department : (sp != null ? sp.getDepartment() : "Campus Administration"));
            }
        } else if (user.getRole() == Role.FACULTY) {
            // When FACULTY files a complaint, route to ADMIN
            g.setAssignedToRole("ADMIN");
            g.setAssignedTo("Administration");
            g.setRoutedTo("ADMIN");
            g.setDepartment(department != null ? department : "Campus Infrastructure & Maintenance");
        } else {
            g.setAssignedToRole("ADMIN");
            g.setAssignedTo("Administration");
            g.setRoutedTo("ADMIN");
            g.setDepartment(department != null ? department : "Campus Operations");
        }

        return grievanceRepository.save(g);
    }

    // --- TYPED MENTOR SECTION TOOLS ---

    public FacultyMentorSection verifyMentorSectionAccess(Long sectionId, String requesterUsername) {
        if (requesterUsername == null || requesterUsername.trim().isEmpty()) {
            throw new AccessDeniedException("Unauthorized: Missing requester context.");
        }

        User user = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + requesterUsername));

        FacultyMentorSection mapping = facultyMentorSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor section assignment not found with ID: " + sectionId));

        // ADMINs have campus-wide governance override
        if (user.getRole() == Role.ADMIN) {
            return mapping;
        }

        if (user.getRole() == Role.FACULTY) {
            FacultyProfile fp = facultyProfileRepository.findByUser(user).orElse(null);
            if (fp != null && Boolean.TRUE.equals(fp.isMentor())) {
                // Direct mapping check
                if (mapping.getFacultyProfile() != null && mapping.getFacultyProfile().getId().equals(fp.getId())) {
                    return mapping;
                }
                // Fallback: only if mapping has unassigned facultyProfile and profile matches dept and section
                if (mapping.getFacultyProfile() == null
                        && fp.getAssignedDepartment() != null && fp.getAssignedSection() != null
                        && fp.getAssignedDepartment().equalsIgnoreCase(mapping.getDepartment())
                        && fp.getAssignedSection().equalsIgnoreCase(mapping.getSection())) {
                    return mapping;
                }
            }
        }

        throw new AccessDeniedException("Unauthorized: Only the assigned mentor of " 
                + mapping.getDepartment() + " Section " + mapping.getSection() + " or an Administrator can access this section.");
    }

    public List<Map<String, Object>> getClassRoster(Long sectionId, String requesterUsername) {
        FacultyMentorSection mapping = verifyMentorSectionAccess(sectionId, requesterUsername);
        List<StudentProfile> students = studentProfileRepository.findByDepartmentAndSection(
                mapping.getDepartment(), mapping.getSection()
        );

        List<Map<String, Object>> roster = new ArrayList<>();
        for (StudentProfile sp : students) {
            User u = sp.getUser();
            Map<String, Object> item = new HashMap<>();
            item.put("studentId", sp.getId());
            item.put("userId", u != null ? u.getId() : null);
            item.put("name", u != null ? (u.getFirstName() + " " + u.getLastName()) : "Student");
            item.put("rollNumber", sp.getRollNumber());
            item.put("email", u != null ? u.getEmail() : "");
            item.put("department", sp.getDepartment());
            item.put("section", sp.getSection());
            item.put("semester", sp.getSemester());
            item.put("attendancePercentage", sp.getAttendanceRate());
            item.put("attendanceRate", sp.getAttendanceRate());
            item.put("cgpa", sp.getCgpa());

            // Derived letter grade according to academic standards
            double cgpa = sp.getCgpa();
            String grade = cgpa >= 9.0 ? "O" :
                           cgpa >= 8.0 ? "A+" :
                           cgpa >= 7.0 ? "A" :
                           cgpa >= 6.0 ? "B+" :
                           cgpa >= 5.0 ? "B" : "RA";
            item.put("grade", grade);
            item.put("letterGrade", grade);
            item.put("academicStanding", sp.getAttendanceRate() >= 75 ? "GOOD_STANDING" : "ATTENDANCE_DEFICIT");
            item.put("standing", sp.getAttendanceRate() >= 75 ? "GOOD_STANDING" : "ATTENDANCE_DEFICIT");

            // Subject-wise attendance from AcademicAgent data model
            if (u != null) {
                List<AttendanceRecord> subjAtt = attendanceRecordRepository.findByUserOrderByCourseCodeAsc(u);
                List<Map<String, Object>> subjList = new ArrayList<>();
                for (AttendanceRecord ar : subjAtt) {
                    Map<String, Object> sm = new HashMap<>();
                    sm.put("courseCode", ar.getCourseCode());
                    sm.put("courseName", ar.getCourseName());
                    sm.put("attended", ar.getAttendedClasses());
                    sm.put("total", ar.getTotalClasses());
                    sm.put("percentage", ar.getPercentage());
                    subjList.add(sm);
                }
                item.put("subjectAttendance", subjList);
            } else {
                item.put("subjectAttendance", Collections.emptyList());
            }

            roster.add(item);
        }

        // Sort by roll number ascending
        roster.sort(Comparator.comparing(m -> String.valueOf(m.getOrDefault("rollNumber", ""))));
        return roster;
    }

    public Map<String, Object> getClassAttendanceSummary(Long sectionId, String requesterUsername) {
        FacultyMentorSection mapping = verifyMentorSectionAccess(sectionId, requesterUsername);
        List<StudentProfile> students = studentProfileRepository.findByDepartmentAndSection(
                mapping.getDepartment(), mapping.getSection()
        );

        int total = students.size();
        double avgAtt = total > 0
                ? students.stream().mapToInt(StudentProfile::getAttendanceRate).average().orElse(0.0)
                : 0.0;
        long goodStanding = students.stream().filter(s -> s.getAttendanceRate() >= 75).count();
        long atRisk = students.stream().filter(s -> s.getAttendanceRate() < 75).count();
        double avgCgpa = total > 0
                ? students.stream().mapToDouble(StudentProfile::getCgpa).average().orElse(0.0)
                : 0.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("sectionId", mapping.getId());
        summary.put("department", mapping.getDepartment());
        summary.put("section", mapping.getSection());
        summary.put("semester", mapping.getSemester());
        summary.put("academicYear", mapping.getAcademicYear());
        summary.put("totalStudents", total);
        summary.put("averageAttendance", Math.round(avgAtt * 10.0) / 10.0);
        summary.put("goodStandingCount", goodStanding);
        summary.put("atRiskCount", atRisk);
        summary.put("averageCgpa", Math.round(avgCgpa * 100.0) / 100.0);
        return summary;
    }

    @Transactional
    public Assignment assignCourseworkToSection(Long sectionId, CourseworkRequestDto req, String requesterUsername) {
        FacultyMentorSection mapping = verifyMentorSectionAccess(sectionId, requesterUsername);

        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Assignment title is required.");
        }
        if (req.getSubjectCode() == null || req.getSubjectCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject code is required.");
        }

        User mentorUser = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new IllegalArgumentException("Mentor user not found: " + requesterUsername));

        String facultyName = (mentorUser.getFirstName() + " " + mentorUser.getLastName()).trim();
        String subjectName = req.getSubjectName() != null && !req.getSubjectName().trim().isEmpty()
                ? req.getSubjectName().trim()
                : req.getSubjectCode().trim().toUpperCase();

        LocalDate dueDate = req.getDueDate() != null ? req.getDueDate() : LocalDate.now().plusDays(7);
        String priority = req.getPriority() != null ? req.getPriority().toUpperCase() : "MEDIUM";
        int maxMarks = req.getMaxMarks() > 0 ? req.getMaxMarks() : 100;

        // 1. Create and save the section Assignment entity
        Assignment asg = new Assignment(
                req.getTitle().trim(),
                req.getDescription() != null ? req.getDescription().trim() : "",
                req.getSubjectCode().trim().toUpperCase(),
                subjectName,
                mapping.getDepartment(),
                mapping.getSemester() != null ? mapping.getSemester() : 5,
                mapping.getSection(),
                facultyName,
                LocalDate.now(),
                dueDate,
                priority,
                maxMarks
        );
        asg = assignmentRepository.save(asg);

        // 2. Attach a task directly to every enrolled student in that section
        List<StudentProfile> students = studentProfileRepository.findByDepartmentAndSection(
                mapping.getDepartment(), mapping.getSection()
        );

        for (StudentProfile sp : students) {
            if (sp.getUser() != null) {
                StudentTask task = new StudentTask(
                        sp.getUser(),
                        req.getTitle().trim(),
                        req.getSubjectCode().trim().toUpperCase() + ": " + (req.getDescription() != null ? req.getDescription().trim() : ""),
                        priority,
                        "TODO",
                        dueDate
                );
                studentTaskRepository.save(task);
            }
        }

        // 3. Notify all students in this section about the new assignment
        try {
            notificationAgent.notifySection(
                    mapping.getDepartment(),
                    mapping.getSection(),
                    "New Coursework: " + asg.getTitle(),
                    "New assignment assigned in " + asg.getSubjectName() + " (" + asg.getSubjectCode() + ") by " + facultyName + ". Due: " + dueDate,
                    "ASSIGNMENT"
            );
        } catch (Exception ignored) {}

        return asg;
    }

    public List<Assignment> getSectionAssignments(Long sectionId, String requesterUsername) {
        FacultyMentorSection mapping = verifyMentorSectionAccess(sectionId, requesterUsername);
        return assignmentRepository.findByDepartmentAndSectionOrderByDueDateAsc(
                mapping.getDepartment(), mapping.getSection()
        );
    }

    public List<TimetableEntry> getMyTimetable(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Set<Long> seenIds = new HashSet<>();
        List<TimetableEntry> result = new ArrayList<>();

        // 1. Match by faculty user ID
        List<TimetableEntry> byUserId = timetableEntryRepository.findByFacultyUserIdOrderByDayOfWeekAscStartTimeAsc(user.getId());
        for (TimetableEntry t : byUserId) {
            if (seenIds.add(t.getId())) {
                result.add(t);
            }
        }

        // 2. Match by faculty name pattern
        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        List<TimetableEntry> allEntries = timetableEntryRepository.findAllByOrderByDepartmentAscSectionAscDayOfWeekAscStartTimeAsc();
        for (TimetableEntry t : allEntries) {
            if (t.getFacultyName() != null && seenIds.add(t.getId())) {
                String fn = t.getFacultyName().trim();
                boolean matches = fn.equalsIgnoreCase(fullName) ||
                                  (!lastName.isEmpty() && fn.toLowerCase().contains(lastName.toLowerCase())) ||
                                  (user.getFirstName() != null && fn.toLowerCase().contains(user.getFirstName().toLowerCase()));
                if (matches) {
                    result.add(t);
                } else {
                    seenIds.remove(t.getId());
                }
            }
        }

        List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        result.sort((a, b) -> {
            int dayA = a.getDayOfWeek() != null ? days.indexOf(a.getDayOfWeek()) : 99;
            int dayB = b.getDayOfWeek() != null ? days.indexOf(b.getDayOfWeek()) : 99;
            int dayComp = Integer.compare(dayA, dayB);
            if (dayComp != 0) return dayComp;
            String timeA = a.getStartTime() != null ? a.getStartTime() : "";
            String timeB = b.getStartTime() != null ? b.getStartTime() : "";
            return timeA.compareTo(timeB);
        });

        return result;
    }

    public List<TimetableEntry> getFacultyTodaySchedule(String username) {
        String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        if ("Sunday".equalsIgnoreCase(today)) {
            today = "Monday";
        }
        final String day = today;
        return getMyTimetable(username).stream()
                .filter(t -> t.getDayOfWeek() != null && t.getDayOfWeek().equalsIgnoreCase(day))
                .toList();
    }

    public List<TimetableEntry> getSectionTimetable(Long sectionId, String requesterUsername) {
        FacultyMentorSection mapping = facultyMentorSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));
        return timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(
                mapping.getDepartment(), mapping.getSection()
        );
    }

    @Transactional
    public Map<String, Object> addSectionTimetablePeriod(Long sectionId, TimetableRequest req, String requesterUsername) {
        FacultyMentorSection mapping = verifyMentorSectionAccess(sectionId, requesterUsername);

        TimetableEntry entry = new TimetableEntry();
        entry.setDepartment(mapping.getDepartment());
        entry.setSection(mapping.getSection());
        entry.setSemester(mapping.getSemester() != null ? mapping.getSemester() : 1);
        entry.setSubjectCode(req.getSubjectCode() != null ? req.getSubjectCode().trim().toUpperCase() : "CS101");
        entry.setSubjectName(req.getSubjectName() != null ? req.getSubjectName().trim() : "Course");
        entry.setFacultyName(req.getFacultyName() != null && !req.getFacultyName().trim().isEmpty()
                ? req.getFacultyName().trim() : "Faculty");
        entry.setClassroom(req.getClassroom() != null && !req.getClassroom().trim().isEmpty()
                ? req.getClassroom().trim() : "CS-204");
        entry.setDayOfWeek(req.getDayOfWeek() != null && !req.getDayOfWeek().trim().isEmpty()
                ? req.getDayOfWeek().trim() : "Monday");
        entry.setStartTime(req.getStartTime() != null && !req.getStartTime().trim().isEmpty()
                ? req.getStartTime().trim() : "09:00 AM");
        entry.setEndTime(req.getEndTime() != null && !req.getEndTime().trim().isEmpty()
                ? req.getEndTime().trim() : "10:00 AM");

        List<Map<String, Object>> conflicts = conflictEngine.checkEntryConflicts(entry, null);
        TimetableEntry saved = timetableEntryRepository.save(entry);

        // Notify students of this section about schedule update
        try {
            notificationAgent.notifySection(
                    mapping.getDepartment(),
                    mapping.getSection(),
                    "Timetable Updated: " + entry.getSubjectCode(),
                    "New class added: " + entry.getSubjectName() + " on " + entry.getDayOfWeek() + " (" + entry.getStartTime() + " - " + entry.getEndTime() + ") in Room " + entry.getClassroom(),
                    "TIMETABLE_UPDATE"
            );
        } catch (Exception ignored) {}

        Map<String, Object> response = new HashMap<>();
        response.put("entry", saved);
        response.put("conflicts", conflicts);
        response.put("hasConflicts", !conflicts.isEmpty());
        return response;
    }

    @Transactional
    public Map<String, Object> updateSectionTimetable(Long sectionId, Long entryId, TimetableRequest req, String requesterUsername) {
        FacultyMentorSection mapping = verifyMentorSectionAccess(sectionId, requesterUsername);

        TimetableEntry entry = timetableEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found: " + entryId));

        if (!entry.getDepartment().equalsIgnoreCase(mapping.getDepartment()) ||
            !entry.getSection().equalsIgnoreCase(mapping.getSection())) {
            throw new AccessDeniedException("Unauthorized: Entry does not belong to section " + mapping.getDepartment() + " Sec " + mapping.getSection());
        }

        if (req.getSubjectCode() != null) entry.setSubjectCode(req.getSubjectCode().trim().toUpperCase());
        if (req.getSubjectName() != null) entry.setSubjectName(req.getSubjectName().trim());
        if (req.getFacultyName() != null) entry.setFacultyName(req.getFacultyName().trim());
        if (req.getClassroom() != null) entry.setClassroom(req.getClassroom().trim());
        if (req.getDayOfWeek() != null) entry.setDayOfWeek(req.getDayOfWeek().trim());
        if (req.getStartTime() != null) entry.setStartTime(req.getStartTime().trim());
        if (req.getEndTime() != null) entry.setEndTime(req.getEndTime().trim());
        if (req.getSemester() > 0) entry.setSemester(req.getSemester());

        List<Map<String, Object>> conflicts = conflictEngine.checkEntryConflicts(entry, entryId);
        TimetableEntry saved = timetableEntryRepository.save(entry);

        // Notify students of this section about period change
        try {
            notificationAgent.notifySection(
                    mapping.getDepartment(),
                    mapping.getSection(),
                    "Timetable Period Changed: " + entry.getSubjectCode(),
                    "Schedule change: " + entry.getSubjectName() + " on " + entry.getDayOfWeek() + " (" + entry.getStartTime() + " - " + entry.getEndTime() + ") in Room " + entry.getClassroom(),
                    "TIMETABLE_UPDATE"
            );
        } catch (Exception ignored) {}

        Map<String, Object> response = new HashMap<>();
        response.put("entry", saved);
        response.put("conflicts", conflicts);
        response.put("hasConflicts", !conflicts.isEmpty());
        return response;
    }

    @Transactional
    public void deleteSectionTimetablePeriod(Long sectionId, Long entryId, String requesterUsername) {
        FacultyMentorSection mapping = verifyMentorSectionAccess(sectionId, requesterUsername);

        TimetableEntry entry = timetableEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found: " + entryId));

        if (!entry.getDepartment().equalsIgnoreCase(mapping.getDepartment()) ||
            !entry.getSection().equalsIgnoreCase(mapping.getSection())) {
            throw new AccessDeniedException("Unauthorized: Entry does not belong to section " + mapping.getDepartment() + " Sec " + mapping.getSection());
        }

        timetableEntryRepository.delete(entry);

        // Notify students of this section about period cancellation
        try {
            notificationAgent.notifySection(
                    mapping.getDepartment(),
                    mapping.getSection(),
                    "Timetable Period Cancelled: " + entry.getSubjectCode(),
                    "Class period on " + entry.getDayOfWeek() + " (" + entry.getStartTime() + " - " + entry.getEndTime() + ") for " + entry.getSubjectName() + " has been cancelled.",
                    "TIMETABLE_UPDATE"
            );
        } catch (Exception ignored) {}
    }

    public List<Map<String, Object>> checkTimetableConflicts(TimetableRequest req, Long excludeId) {
        TimetableEntry entry = new TimetableEntry();
        entry.setDepartment(req.getDepartment());
        entry.setSection(req.getSection());
        entry.setSubjectCode(req.getSubjectCode());
        entry.setSubjectName(req.getSubjectName());
        entry.setFacultyName(req.getFacultyName());
        entry.setClassroom(req.getClassroom());
        entry.setDayOfWeek(req.getDayOfWeek());
        entry.setStartTime(req.getStartTime());
        entry.setEndTime(req.getEndTime());
        return conflictEngine.checkEntryConflicts(entry, excludeId);
    }

    public Map<String, Object> explainAttendanceEntry(String username, String dateHint, String subjectHint) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Map.of("found", false, "message", "User not found");

        List<AttendanceEntry> entries = attendanceEntryRepository.findByStudent_IdOrderBySession_SessionDateDesc(user.getId());

        AttendanceEntry target = entries.stream()
                .filter(e -> {
                    boolean dateMatch = dateHint == null || dateHint.isEmpty() ||
                            (e.getSession() != null && (
                                    e.getSession().getSessionDate().toString().contains(dateHint) ||
                                    e.getSession().getSessionDate().getDayOfWeek().toString().toLowerCase().contains(dateHint.toLowerCase())
                            ));
                    boolean subjectMatch = subjectHint == null || subjectHint.isEmpty() ||
                            (e.getSession() != null && (
                                    e.getSession().getSubjectCode().toLowerCase().contains(subjectHint.toLowerCase()) ||
                                    e.getSession().getSubjectName().toLowerCase().contains(subjectHint.toLowerCase())
                            ));
                    return dateMatch && subjectMatch;
                })
                .findFirst().orElse(null);

        Map<String, Object> result = new HashMap<>();
        if (target == null) {
            result.put("found", false);
            result.put("message", "No attendance entry found matching the given criteria.");
            return result;
        }

        result.put("found", true);
        result.put("date", target.getSession() != null ? target.getSession().getSessionDate() : null);
        result.put("subject", target.getSession() != null ? target.getSession().getSubjectName() : null);
        result.put("subjectCode", target.getSession() != null ? target.getSession().getSubjectCode() : null);
        result.put("period", target.getSession() != null ? target.getSession().getPeriod() : null);
        result.put("status", target.getStatus());
        result.put("remarks", target.getRemarks());

        try {
            LocalDate sessionDate = target.getSession() != null ? target.getSession().getSessionDate() : null;
            if (sessionDate != null) {
                List<LeaveRequest> leaves = leaveRequestRepository.findByStudentOrderByCreatedAtDesc(user);
                LeaveRequest linkedLeave = leaves.stream()
                        .filter(l -> !sessionDate.isBefore(l.getFromDate()) && !sessionDate.isAfter(l.getToDate()))
                        .findFirst().orElse(null);
                if (linkedLeave != null) {
                    result.put("linkedRequest", Map.of("type", "LEAVE", "leaveType", linkedLeave.getLeaveType(), "status", linkedLeave.getStatus(), "reason", linkedLeave.getReason()));
                }

                List<ODRequest> ods = odRequestRepository.findByStudentOrderByCreatedAtDesc(user);
                ODRequest linkedOD = ods.stream()
                        .filter(o -> o.getEventDate().equals(sessionDate))
                        .findFirst().orElse(null);
                if (linkedOD != null) {
                    result.put("linkedRequest", Map.of("type", "OD", "eventName", linkedOD.getEventName(), "status", linkedOD.getStatus()));
                }
            }
        } catch (Exception ignored) {}

        return result;
    }
}
