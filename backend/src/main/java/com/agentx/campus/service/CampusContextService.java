package com.agentx.campus.service;

import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class CampusContextService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final CampusResourceRepository campusResourceRepository;
    private final GrievanceRepository grievanceRepository;
    private final CampusEventRepository campusEventRepository;

    public CampusContextService(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            FacultyProfileRepository facultyProfileRepository,
            TimetableEntryRepository timetableEntryRepository,
            CampusResourceRepository campusResourceRepository,
            GrievanceRepository grievanceRepository,
            CampusEventRepository campusEventRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.campusResourceRepository = campusResourceRepository;
        this.grievanceRepository = grievanceRepository;
        this.campusEventRepository = campusEventRepository;
    }

    public Map<String, Object> buildUserContext(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("userId", user.getId());
        ctx.put("username", user.getUsername());
        ctx.put("name", user.getFirstName() + " " + user.getLastName());
        ctx.put("role", user.getRole().toString());
        ctx.put("email", user.getEmail());

        if (user.getRole() == Role.STUDENT) {
            StudentProfile sp = studentProfileRepository.findByUser(user).orElse(null);
            if (sp != null) {
                ctx.put("rollNumber", sp.getRollNumber());
                ctx.put("department", sp.getDepartment());
                ctx.put("section", sp.getSection());
                ctx.put("semester", sp.getSemester());
                ctx.put("cgpa", sp.getCgpa());
                ctx.put("attendance", sp.getAttendanceRate());

                // Mentor lookup
                Optional<FacultyProfile> mentorOpt = facultyProfileRepository
                        .findFirstByAssignedDepartmentAndAssignedSection(sp.getDepartment(), sp.getSection());
                if (mentorOpt.isPresent()) {
                    FacultyProfile fp = mentorOpt.get();
                    User mu = fp.getUser();
                    ctx.put("mentor", Map.of(
                            "name", (mu != null ? mu.getFirstName() + " " + mu.getLastName() : "Faculty Mentor"),
                            "email", (mu != null ? mu.getEmail() : ""),
                            "designation", (fp.getDesignation() != null ? fp.getDesignation() : "Faculty"),
                            "cabin", (fp.getCabinNumber() != null ? fp.getCabinNumber() : "Block B")
                    ));
                } else {
                    ctx.put("mentor", null);
                }

                // Timetables for this student's section
                List<TimetableEntry> tt = timetableEntryRepository
                        .findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(sp.getDepartment(), sp.getSection());
                ctx.put("timetables", tt);
            }
            ctx.put("grievances", grievanceRepository.findByUserOrderByCreatedAtDesc(user));
        } else if (user.getRole() == Role.FACULTY) {
            FacultyProfile fp = facultyProfileRepository.findByUser(user).orElse(null);
            if (fp != null) {
                ctx.put("employeeId", fp.getEmployeeId());
                ctx.put("department", fp.getDepartment());
                ctx.put("assignedDepartment", fp.getAssignedDepartment());
                ctx.put("assignedSection", fp.getAssignedSection());
                ctx.put("designation", fp.getDesignation());
                ctx.put("cabinNumber", fp.getCabinNumber());

                if (fp.getAssignedDepartment() != null && fp.getAssignedSection() != null) {
                    List<StudentProfile> mentees = studentProfileRepository
                            .findByDepartmentAndSection(fp.getAssignedDepartment(), fp.getAssignedSection());
                    ctx.put("menteeCount", mentees.size());
                    ctx.put("timetables", timetableEntryRepository
                            .findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(fp.getAssignedDepartment(), fp.getAssignedSection()));
                }
            }
        }

        // Available campus resources
        ctx.put("resources", campusResourceRepository.findAll());
        return ctx;
    }

    public String formatContextForPrompt(String username) {
        Map<String, Object> ctx = buildUserContext(username);
        StringBuilder sb = new StringBuilder();
        sb.append("AUTHENTICATED USER CONTEXT (Verified Database Records):\n");
        sb.append("- User: ").append(ctx.get("name")).append(" (").append(ctx.get("username")).append("), Role: ").append(ctx.get("role")).append("\n");

        if (ctx.containsKey("rollNumber")) {
            sb.append("- Student Roll Number: ").append(ctx.get("rollNumber")).append("\n");
            sb.append("- Department: ").append(ctx.get("department")).append(", Section: ").append(ctx.get("section")).append(", Semester: ").append(ctx.get("semester")).append("\n");
            sb.append("- Attendance: ").append(ctx.get("attendance")).append("%, CGPA: ").append(ctx.get("cgpa")).append("\n");

            Object mentor = ctx.get("mentor");
            if (mentor != null) {
                Map<?, ?> mMap = (Map<?, ?>) mentor;
                sb.append("- Assigned Faculty Mentor: ").append(mMap.get("name"))
                        .append(" (Designation: ").append(mMap.get("designation"))
                        .append(", Email: ").append(mMap.get("email"))
                        .append(", Cabin: ").append(mMap.get("cabin")).append(")\n");
            } else {
                sb.append("- Assigned Faculty Mentor: None assigned yet by Department Administrator.\n");
            }

            @SuppressWarnings("unchecked")
            List<TimetableEntry> ttList = (List<TimetableEntry>) ctx.get("timetables");
            if (ttList != null && !ttList.isEmpty()) {
                sb.append("- SECTION TIMETABLE (").append(ctx.get("department")).append(" Section ").append(ctx.get("section")).append("):\n");
                for (TimetableEntry t : ttList) {
                    sb.append("  * [").append(t.getDayOfWeek()).append("] ").append(t.getStartTime()).append(" - ").append(t.getEndTime())
                            .append(": ").append(t.getSubjectCode()).append(" - ").append(t.getSubjectName())
                            .append(" (Instructor: ").append(t.getFacultyName()).append(", Room: ").append(t.getClassroom()).append(")\n");
                }
            } else {
                sb.append("- SECTION TIMETABLE: No classes currently scheduled in database for Section ").append(ctx.get("section")).append(".\n");
            }
        } else if (ctx.containsKey("employeeId")) {
            sb.append("- Faculty Employee ID: ").append(ctx.get("employeeId")).append("\n");
            sb.append("- Department: ").append(ctx.get("department")).append(", Designation: ").append(ctx.get("designation")).append("\n");
            sb.append("- Assigned Mentorship Section: ").append(ctx.get("assignedDepartment")).append(" - Section ").append(ctx.get("assignedSection")).append("\n");
            sb.append("- Total Enrolled Mentees in Section: ").append(ctx.get("menteeCount")).append("\n");
        }

        @SuppressWarnings("unchecked")
        List<CampusResource> resList = (List<CampusResource>) ctx.get("resources");
        if (resList != null && !resList.isEmpty()) {
            sb.append("- CAMPUS PHYSICAL RESOURCES:\n");
            for (CampusResource r : resList) {
                sb.append("  * ").append(r.getRoomNumber()).append(" (").append(r.getName()).append(", ").append(r.getBuilding())
                        .append(", Type: ").append(r.getType()).append(", Capacity: ").append(r.getCapacity()).append(", Status: ").append(r.getStatus()).append(")\n");
            }
        }

        return sb.toString();
    }
}
