package com.agentx.campus.service;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamService {

    private final ExamScheduleRepository examRepo;
    private final ConflictEngine conflictEngine;
    private final StudentProfileRepository profileRepo;
    private final UserRepository userRepo;
    private final NotificationAgent notificationAgent;

    public ExamService(ExamScheduleRepository examRepo, ConflictEngine conflictEngine,
                       StudentProfileRepository profileRepo, UserRepository userRepo,
                       NotificationAgent notificationAgent) {
        this.examRepo = examRepo;
        this.conflictEngine = conflictEngine;
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
        this.notificationAgent = notificationAgent;
    }

    @Transactional
    public Map<String, Object> createExam(Map<String, Object> body, String creatorUsername) {
        User creator = userRepo.findByUsername(creatorUsername).orElseThrow();
        ExamSchedule exam = buildExam(body, creator);
        List<Map<String, Object>> conflicts = conflictEngine.checkExamConflicts(exam, null);
        exam = examRepo.save(exam);

        try {
            notificationAgent.notifySection(
                    exam.getDepartment(), exam.getSection(),
                    "Exam Scheduled: " + exam.getSubjectCode(),
                    exam.getExamType() + " exam for " + exam.getSubjectName()
                            + " on " + exam.getExamDate() + " (" + exam.getStartTime()
                            + " – " + exam.getEndTime() + ") in Room " + exam.getRoom(),
                    "EXAM_SCHEDULED"
            );
        } catch (Exception ignored) {}

        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("conflicts", conflicts);
        result.put("hasConflicts", !conflicts.isEmpty());
        return result;
    }

    @Transactional
    public Map<String, Object> updateExam(Long id, Map<String, Object> body, String username) {
        ExamSchedule exam = examRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + id));

        if (body.containsKey("subjectCode")) exam.setSubjectCode(body.get("subjectCode").toString().toUpperCase());
        if (body.containsKey("subjectName")) exam.setSubjectName(body.get("subjectName").toString());
        if (body.containsKey("department")) exam.setDepartment(body.get("department").toString());
        if (body.containsKey("section")) exam.setSection(body.get("section").toString());
        if (body.containsKey("examType")) exam.setExamType(body.get("examType").toString().toUpperCase());
        if (body.containsKey("room")) exam.setRoom(body.get("room").toString());
        if (body.containsKey("startTime")) exam.setStartTime(body.get("startTime").toString());
        if (body.containsKey("endTime")) exam.setEndTime(body.get("endTime").toString());
        if (body.containsKey("semester")) exam.setSemester(Integer.parseInt(body.get("semester").toString()));
        if (body.containsKey("examDate")) {
            try { exam.setExamDate(LocalDate.parse(body.get("examDate").toString())); }
            catch (Exception ignored2) {}
        }

        List<Map<String, Object>> conflicts = conflictEngine.checkExamConflicts(exam, id);
        exam = examRepo.save(exam);

        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("conflicts", conflicts);
        result.put("hasConflicts", !conflicts.isEmpty());
        return result;
    }

    @Transactional
    public void deleteExam(Long id) {
        examRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Exam not found: " + id));
        examRepo.deleteById(id);
    }

    public List<ExamSchedule> getAllExams() {
        return examRepo.findAllByOrderByExamDateAscDepartmentAsc();
    }

    public List<ExamSchedule> getStudentExams(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        StudentProfile profile = profileRepo.findByUser(user).orElse(null);
        if (profile == null) return Collections.emptyList();
        return examRepo.findByDepartmentAndSectionOrderByExamDateAscStartTimeAsc(
                profile.getDepartment(), profile.getSection());
    }

    private ExamSchedule buildExam(Map<String, Object> body, User creator) {
        ExamSchedule exam = new ExamSchedule();
        exam.setSubjectCode(body.getOrDefault("subjectCode", "CS101").toString().toUpperCase());
        exam.setSubjectName(body.getOrDefault("subjectName", "Subject").toString());
        exam.setDepartment(body.getOrDefault("department", "Computer Science & Engineering").toString());
        exam.setSection(body.getOrDefault("section", "A").toString());
        exam.setExamType(body.getOrDefault("examType", "INTERNAL").toString().toUpperCase());
        exam.setRoom(body.getOrDefault("room", "Exam Hall 1").toString());
        exam.setStartTime(body.getOrDefault("startTime", "09:00 AM").toString());
        exam.setEndTime(body.getOrDefault("endTime", "12:00 PM").toString());
        exam.setSemester(body.containsKey("semester") ? Integer.parseInt(body.get("semester").toString()) : 1);
        try { exam.setExamDate(LocalDate.parse(body.get("examDate").toString())); }
        catch (Exception e) { exam.setExamDate(LocalDate.now().plusDays(14)); }
        exam.setCreatedBy(creator);
        return exam;
    }
}
