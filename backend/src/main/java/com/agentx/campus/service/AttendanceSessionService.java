package com.agentx.campus.service;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handles session-based attendance creation, entry bulk-update, and aggregate recomputation.
 * Never bypasses AttendanceRecord setters (which auto-recompute percentage).
 */
@Service
public class AttendanceSessionService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceEntryRepository entryRepository;
    private final AttendanceRecordRepository recordRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyMentorSectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final NotificationAgent notificationAgent;

    public AttendanceSessionService(
            AttendanceSessionRepository sessionRepository,
            AttendanceEntryRepository entryRepository,
            AttendanceRecordRepository recordRepository,
            StudentProfileRepository studentProfileRepository,
            FacultyMentorSectionRepository sectionRepository,
            UserRepository userRepository,
            NotificationAgent notificationAgent) {
        this.sessionRepository = sessionRepository;
        this.entryRepository = entryRepository;
        this.recordRepository = recordRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.notificationAgent = notificationAgent;
    }

    @Transactional
    public Map<String, Object> createSession(Long sectionId, String dateStr, String period,
                                              String subjectCode, String subjectName,
                                              String facultyUsername) {
        FacultyMentorSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        User faculty = userRepository.findByUsername(facultyUsername)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found: " + facultyUsername));

        // Authorization: only the assigned mentor or ADMIN
        boolean isMentor = section.getFacultyProfile() != null &&
                section.getFacultyProfile().getUser() != null &&
                section.getFacultyProfile().getUser().getId().equals(faculty.getId());
        boolean isAdmin = "ADMIN".equals(faculty.getRole().name());
        if (!isMentor && !isAdmin) {
            throw new AccessDeniedException("Only the assigned mentor can create sessions for this section.");
        }

        LocalDate sessionDate;
        try {
            sessionDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            sessionDate = LocalDate.now();
        }

        AttendanceSession sess = new AttendanceSession();
        sess.setSection(section);
        sess.setFaculty(faculty);
        sess.setSubjectCode(subjectCode.trim().toUpperCase());
        sess.setSubjectName(subjectName.trim());
        sess.setSessionDate(sessionDate);
        sess.setPeriod(period.trim());
        sess.setCreatedAt(LocalDateTime.now());
        sess = sessionRepository.save(sess);

        // Default all enrolled students to PRESENT
        List<StudentProfile> students = studentProfileRepository
                .findByDepartmentAndSection(section.getDepartment(), section.getSection());

        List<AttendanceEntry> entries = new ArrayList<>();
        for (StudentProfile sp : students) {
            if (sp.getUser() != null) {
                AttendanceEntry e = new AttendanceEntry();
                e.setSession(sess);
                e.setStudent(sp.getUser());
                e.setStatus("PRESENT");
                entries.add(entryRepository.save(e));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sess.getId());
        result.put("sessionDate", sess.getSessionDate());
        result.put("period", sess.getPeriod());
        result.put("subjectCode", sess.getSubjectCode());
        result.put("subjectName", sess.getSubjectName());
        result.put("totalStudents", entries.size());
        result.put("entries", entries);
        return result;
    }

    @Transactional
    public Map<String, Object> bulkUpdateEntries(Long sessionId, List<Map<String, Object>> updates,
                                                  String facultyUsername) {
        AttendanceSession sess = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        User faculty = userRepository.findByUsername(facultyUsername)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found: " + facultyUsername));

        // Authorization
        FacultyMentorSection section = sess.getSection();
        boolean isMentor = section.getFacultyProfile() != null &&
                section.getFacultyProfile().getUser() != null &&
                section.getFacultyProfile().getUser().getId().equals(faculty.getId());
        boolean isAdmin = "ADMIN".equals(faculty.getRole().name());
        if (!isMentor && !isAdmin) {
            throw new AccessDeniedException("Only the assigned mentor can update attendance for this session.");
        }

        Set<Long> affectedStudentIds = new HashSet<>();
        for (Map<String, Object> update : updates) {
            Long studentId = Long.valueOf(update.get("studentId").toString());
            String status = update.getOrDefault("status", "PRESENT").toString().toUpperCase();
            String remarks = update.containsKey("remarks") ? update.get("remarks").toString() : null;

            entryRepository.findBySession_IdAndStudent_Id(sessionId, studentId).ifPresent(entry -> {
                entry.setStatus(status);
                entry.setRemarks(remarks);
                entryRepository.save(entry);
                affectedStudentIds.add(studentId);
            });
        }

        // Recompute AttendanceRecord for all affected students
        for (Long studentId : affectedStudentIds) {
            userRepository.findById(studentId).ifPresent(student ->
                    recomputeAttendanceRecord(student, sess.getSubjectCode(), sess.getSubjectName())
            );
        }

        List<AttendanceEntry> allEntries = entryRepository.findBySession_IdOrderByStudent_Id(sessionId);
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("updatedCount", affectedStudentIds.size());
        result.put("entries", allEntries);
        return result;
    }

    /** Recomputes AttendanceRecord for a student+subject using session entry data. */
    private void recomputeAttendanceRecord(User student, String subjectCode, String subjectName) {
        List<AttendanceEntry> allEntries =
                entryRepository.findByStudent_IdAndSession_SubjectCode(student.getId(), subjectCode);

        int total = allEntries.size();
        long attended = allEntries.stream()
                .filter(e -> "PRESENT".equals(e.getStatus())
                          || "LATE".equals(e.getStatus())
                          || "OD".equals(e.getStatus()))
                .count();

        AttendanceRecord record = recordRepository
                .findByUserAndCourseCode(student, subjectCode)
                .orElse(null);

        if (record == null) {
            record = new AttendanceRecord();
            record.setUser(student);
            record.setCourseCode(subjectCode);
            record.setCourseName(subjectName);
        }

        // Use setters — they auto-recompute percentage
        record.setTotalClasses(total);
        record.setAttendedClasses((int) attended);
        record.setLastUpdated(LocalDateTime.now());
        recordRepository.save(record);
    }

    public List<Map<String, Object>> getSessionsForSection(Long sectionId, String facultyUsername) {
        FacultyMentorSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        List<AttendanceSession> sessions =
                sessionRepository.findBySection_IdOrderBySessionDateDescCreatedAtDesc(sectionId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (AttendanceSession s : sessions) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("sessionDate", s.getSessionDate());
            m.put("period", s.getPeriod());
            m.put("subjectCode", s.getSubjectCode());
            m.put("subjectName", s.getSubjectName());
            m.put("facultyName", s.getFacultyName());
            List<AttendanceEntry> entries = entryRepository.findBySession_IdOrderByStudent_Id(s.getId());
            m.put("totalStudents", entries.size());
            m.put("presentCount", entries.stream().filter(e -> "PRESENT".equals(e.getStatus())).count());
            m.put("absentCount", entries.stream().filter(e -> "ABSENT".equals(e.getStatus())).count());
            m.put("entries", entries);
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> getStudentAttendanceHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        List<AttendanceEntry> entries =
                entryRepository.findByStudent_IdOrderBySession_SessionDateDesc(user.getId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (AttendanceEntry e : entries) {
            Map<String, Object> m = new HashMap<>();
            m.put("entryId", e.getId());
            m.put("sessionId", e.getSessionId());
            m.put("sessionDate", e.getSessionDate());
            m.put("period", e.getPeriod());
            m.put("subjectCode", e.getSubjectCode());
            m.put("subjectName", e.getSubjectName());
            m.put("status", e.getStatus());
            m.put("remarks", e.getRemarks());
            result.add(m);
        }
        return result;
    }
}
