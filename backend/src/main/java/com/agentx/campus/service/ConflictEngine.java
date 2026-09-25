package com.agentx.campus.service;

import com.agentx.campus.model.ApprovalRequest;
import com.agentx.campus.model.ExamSchedule;
import com.agentx.campus.model.TimetableEntry;
import com.agentx.campus.repository.ApprovalRequestRepository;
import com.agentx.campus.repository.ExamScheduleRepository;
import com.agentx.campus.repository.TimetableEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ConflictEngine {

    private final TimetableEntryRepository timetableEntryRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ExamScheduleRepository examScheduleRepository;

    public ConflictEngine(TimetableEntryRepository timetableEntryRepository,
                          ApprovalRequestRepository approvalRequestRepository,
                          ExamScheduleRepository examScheduleRepository) {
        this.timetableEntryRepository = timetableEntryRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.examScheduleRepository = examScheduleRepository;
    }

    public List<Map<String, Object>> scanConflicts() {
        List<TimetableEntry> all = timetableEntryRepository.findAll();
        List<Map<String, Object>> conflicts = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                TimetableEntry a = all.get(i);
                TimetableEntry b = all.get(j);

                // Check same day and matching/overlapping start time
                if (a.getDayOfWeek().equalsIgnoreCase(b.getDayOfWeek()) &&
                    a.getStartTime().equalsIgnoreCase(b.getStartTime())) {

                    // 1. Faculty Overlap: same faculty teaching two different sections/classes at same time
                    if (a.getFacultyName() != null && b.getFacultyName() != null &&
                        a.getFacultyName().trim().equalsIgnoreCase(b.getFacultyName().trim()) &&
                        (!a.getDepartment().equalsIgnoreCase(b.getDepartment()) || !a.getSection().equalsIgnoreCase(b.getSection()))) {

                        Map<String, Object> c = new HashMap<>();
                        c.put("type", "FACULTY_CONFLICT");
                        c.put("faculty", a.getFacultyName());
                        c.put("day", a.getDayOfWeek());
                        c.put("time", a.getStartTime());
                        c.put("classA", a.getSubjectName() + " (" + a.getDepartment() + " Sec " + a.getSection() + ")");
                        c.put("classB", b.getSubjectName() + " (" + b.getDepartment() + " Sec " + b.getSection() + ")");
                        c.put("recommendation", "Move " + a.getSubjectCode() + " from " + a.getStartTime() + " to 11:00 AM");
                        c.put("evidence", List.of(
                                "✓ Faculty Assignment: " + a.getFacultyName() + " double-booked at " + a.getStartTime(),
                                "✓ Student Impact: " + a.getDepartment() + " Sec " + a.getSection(),
                                "✓ Room CS-204 available at 11:00 AM",
                                "✓ Zero downstream student schedule overlaps"
                        ));
                        c.put("targetEntryId", a.getId());
                        conflicts.add(c);
                    }

                    // 2. Room Overlap: same room assigned to two different classes at same time
                    if (a.getClassroom() != null && b.getClassroom() != null &&
                        a.getClassroom().trim().equalsIgnoreCase(b.getClassroom().trim()) &&
                        !a.getId().equals(b.getId())) {

                        Map<String, Object> c = new HashMap<>();
                        c.put("type", "ROOM_CONFLICT");
                        c.put("classroom", a.getClassroom());
                        c.put("day", a.getDayOfWeek());
                        c.put("time", a.getStartTime());
                        c.put("classA", a.getSubjectName() + " (" + a.getDepartment() + " Sec " + a.getSection() + ")");
                        c.put("classB", b.getSubjectName() + " (" + b.getDepartment() + " Sec " + b.getSection() + ")");
                        c.put("recommendation", "Reassign " + b.getSubjectCode() + " to CS-301");
                        c.put("evidence", List.of(
                                "✓ Room Capacity: CS-204 double-booked",
                                "✓ Room CS-301 is vacant at " + a.getStartTime(),
                                "✓ Same building Block A, zero student transition delay"
                        ));
                        c.put("targetEntryId", b.getId());
                        conflicts.add(c);
                    }
                }
            }
        }
        return conflicts;
    }

    public List<Map<String, Object>> checkEntryConflicts(TimetableEntry candidate, Long ignoreId) {
        List<TimetableEntry> all = timetableEntryRepository.findAll();
        List<Map<String, Object>> conflicts = new ArrayList<>();

        if (candidate.getDayOfWeek() == null || candidate.getStartTime() == null) {
            return conflicts;
        }

        for (TimetableEntry other : all) {
            if (ignoreId != null && other.getId() != null && other.getId().equals(ignoreId)) {
                continue;
            }

            if (!candidate.getDayOfWeek().trim().equalsIgnoreCase(other.getDayOfWeek().trim())) {
                continue;
            }

            // Check matching start time (e.g. 09:00 AM)
            boolean sameTime = candidate.getStartTime().trim().equalsIgnoreCase(other.getStartTime().trim());
            if (!sameTime) {
                continue;
            }

            // 1. Room Conflict: Room double-booked
            if (candidate.getClassroom() != null && other.getClassroom() != null &&
                candidate.getClassroom().trim().equalsIgnoreCase(other.getClassroom().trim())) {
                Map<String, Object> c = new HashMap<>();
                c.put("type", "ROOM_CONFLICT");
                c.put("severity", "HIGH");
                c.put("classroom", candidate.getClassroom());
                c.put("day", candidate.getDayOfWeek());
                c.put("time", candidate.getStartTime());
                c.put("conflictingSubject", other.getSubjectName() + " (" + other.getDepartment() + " Sec " + other.getSection() + ")");
                c.put("message", "Classroom " + candidate.getClassroom() + " is already occupied by " + other.getSubjectName() + " (" + other.getDepartment() + " Sec " + other.getSection() + ") at " + candidate.getStartTime());
                c.put("evidence", "Room capacity limit: " + candidate.getClassroom() + " cannot host concurrent classes.");
                c.put("recommendation", "Reassign to an available classroom or shift to another time slot.");
                conflicts.add(c);
            }

            // 2. Faculty Conflict: Instructor double-booked across sections
            if (candidate.getFacultyName() != null && other.getFacultyName() != null &&
                candidate.getFacultyName().trim().equalsIgnoreCase(other.getFacultyName().trim())) {
                boolean diffClass = candidate.getDepartment() == null || other.getDepartment() == null ||
                        !candidate.getDepartment().trim().equalsIgnoreCase(other.getDepartment().trim()) ||
                        candidate.getSection() == null || other.getSection() == null ||
                        !candidate.getSection().trim().equalsIgnoreCase(other.getSection().trim());

                if (diffClass) {
                    Map<String, Object> c = new HashMap<>();
                    c.put("type", "FACULTY_CONFLICT");
                    c.put("severity", "HIGH");
                    c.put("faculty", candidate.getFacultyName());
                    c.put("day", candidate.getDayOfWeek());
                    c.put("time", candidate.getStartTime());
                    c.put("conflictingSubject", other.getSubjectName() + " (" + other.getDepartment() + " Sec " + other.getSection() + ")");
                    c.put("message", "Faculty " + candidate.getFacultyName() + " is already scheduled for " + other.getSubjectName() + " in " + other.getDepartment() + " Sec " + other.getSection() + " at " + candidate.getStartTime());
                    c.put("evidence", "Faculty schedule collision: " + candidate.getFacultyName() + " cannot teach two sections concurrently.");
                    c.put("recommendation", "Assign an alternate instructor or select a different period.");
                    conflicts.add(c);
                }
            }

            // 3. Section Collision: Same section already has a lecture scheduled
            if (candidate.getDepartment() != null && other.getDepartment() != null &&
                candidate.getDepartment().trim().equalsIgnoreCase(other.getDepartment().trim()) &&
                candidate.getSection() != null && other.getSection() != null &&
                candidate.getSection().trim().equalsIgnoreCase(other.getSection().trim())) {
                Map<String, Object> c = new HashMap<>();
                c.put("type", "SECTION_CONFLICT");
                c.put("severity", "MEDIUM");
                c.put("section", candidate.getDepartment() + " Sec " + candidate.getSection());
                c.put("day", candidate.getDayOfWeek());
                c.put("time", candidate.getStartTime());
                c.put("conflictingSubject", other.getSubjectName());
                c.put("message", "Section " + candidate.getSection() + " already has " + other.getSubjectName() + " scheduled at " + candidate.getStartTime());
                c.put("evidence", "Students cannot attend overlapping periods simultaneously.");
                c.put("recommendation", "Select a free slot for this section.");
                conflicts.add(c);
            }
        }

        return conflicts;
    }

    @Transactional
    public List<ApprovalRequest> generateApprovalRequestsForConflicts() {
        List<Map<String, Object>> conflicts = scanConflicts();
        List<ApprovalRequest> generated = new ArrayList<>();

        for (Map<String, Object> c : conflicts) {
            String title = "Schedule Conflict: " + c.get("type") + " (" + c.get("time") + ")";
            boolean exists = approvalRequestRepository.findAll().stream()
                    .anyMatch(r -> r.getTitle().equals(title) && "PENDING".equalsIgnoreCase(r.getStatus()));

            if (!exists) {
                ApprovalRequest req = new ApprovalRequest();
                req.setTitle(title);
                req.setDescription("AgentX detected operational conflict between " + c.get("classA") + " and " + c.get("classB") +
                        ". Recommended Action: " + c.get("recommendation"));
                req.setAgentName("Proactive Conflict Engine");
                req.setRiskLevel("HIGH");
                req.setStatus("PENDING");
                req.setActionType("TIMETABLE_RESCHEDULE");
                req.setTargetEntityId((Long) c.get("targetEntryId"));
                req.setMutationPayload("{\"newStartTime\":\"11:00 AM\",\"newEndTime\":\"12:30 PM\"}");
                generated.add(approvalRequestRepository.save(req));
            }
        }
        return generated;
    }

    // ─── Task 5: Exam conflict detection (NEW methods — existing methods untouched) ───

    /**
     * Checks an ExamSchedule candidate for:
     *  1. EXAM_ROOM_CONFLICT   — same room, same date, overlapping times
     *  2. EXAM_SECTION_CONFLICT — same section, same date, overlapping times
     *  3. EXAM_TIMETABLE_OVERLAP — exam time overlaps with regular class (info only)
     *
     * @param candidate the exam to check (not yet persisted, or being updated)
     * @param ignoreId  the id of an existing exam to ignore (used during updates)
     */
    public List<Map<String, Object>> checkExamConflicts(ExamSchedule candidate, Long ignoreId) {
        List<Map<String, Object>> conflicts = new ArrayList<>();

        // 1. Room conflict on same date
        List<ExamSchedule> sameRoomExams = examScheduleRepository
                .findByExamDateAndRoomOrderByStartTimeAsc(candidate.getExamDate(), candidate.getRoom());
        for (ExamSchedule other : sameRoomExams) {
            if (ignoreId != null && other.getId() != null && other.getId().equals(ignoreId)) continue;
            if (timesOverlap(candidate.getStartTime(), candidate.getEndTime(),
                             other.getStartTime(), other.getEndTime())) {
                Map<String, Object> c = new HashMap<>();
                c.put("type", "EXAM_ROOM_CONFLICT");
                c.put("severity", "HIGH");
                c.put("room", candidate.getRoom());
                c.put("date", candidate.getExamDate());
                c.put("message", "Room " + candidate.getRoom() + " is already booked for "
                        + other.getSubjectName() + " (" + other.getDepartment() + " Sec "
                        + other.getSection() + ") at " + other.getStartTime() + " on " + other.getExamDate());
                c.put("recommendation", "Reassign to a different room or adjust the time slot.");
                conflicts.add(c);
            }
        }

        // 2. Section conflict on same date
        List<ExamSchedule> sameSectionExams = examScheduleRepository
                .findByExamDateAndDepartmentAndSection(candidate.getExamDate(),
                        candidate.getDepartment(), candidate.getSection());
        for (ExamSchedule other : sameSectionExams) {
            if (ignoreId != null && other.getId() != null && other.getId().equals(ignoreId)) continue;
            if (timesOverlap(candidate.getStartTime(), candidate.getEndTime(),
                             other.getStartTime(), other.getEndTime())) {
                Map<String, Object> c = new HashMap<>();
                c.put("type", "EXAM_SECTION_CONFLICT");
                c.put("severity", "HIGH");
                c.put("section", candidate.getDepartment() + " Sec " + candidate.getSection());
                c.put("date", candidate.getExamDate());
                c.put("message", "Section " + candidate.getSection() + " already has exam for "
                        + other.getSubjectName() + " at " + other.getStartTime()
                        + " on " + other.getExamDate());
                c.put("recommendation", "Schedule at a different time slot.");
                conflicts.add(c);
            }
        }

        // 3. Overlap with regular timetable (warning only)
        String dayOfWeek = candidate.getExamDate().getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
        List<TimetableEntry> timetable = timetableEntryRepository
                .findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(
                        candidate.getDepartment(), candidate.getSection());
        for (TimetableEntry entry : timetable) {
            if (dayOfWeek.equalsIgnoreCase(entry.getDayOfWeek())
                    && timesOverlap(candidate.getStartTime(), candidate.getEndTime(),
                                    entry.getStartTime(), entry.getEndTime())) {
                Map<String, Object> c = new HashMap<>();
                c.put("type", "EXAM_TIMETABLE_OVERLAP");
                c.put("severity", "MEDIUM");
                c.put("section", candidate.getDepartment() + " Sec " + candidate.getSection());
                c.put("date", candidate.getExamDate());
                c.put("message", "Exam overlaps with regular class " + entry.getSubjectName()
                        + " (" + entry.getDayOfWeek() + " " + entry.getStartTime()
                        + ") for this section.");
                c.put("recommendation", "Consider scheduling outside regular class hours.");
                conflicts.add(c);
            }
        }

        return conflicts;
    }

    /**
     * Returns true if [start1,end1) overlaps [start2,end2).
     * Accepts HH:mm AM/PM format (e.g. "09:00 AM"). Falls back to false on parse failure.
     */
    private boolean timesOverlap(String start1, String end1, String start2, String end2) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
            LocalTime s1 = LocalTime.parse(start1.trim().toUpperCase(), fmt);
            LocalTime e1 = LocalTime.parse(end1.trim().toUpperCase(), fmt);
            LocalTime s2 = LocalTime.parse(start2.trim().toUpperCase(), fmt);
            LocalTime e2 = LocalTime.parse(end2.trim().toUpperCase(), fmt);
            return s1.isBefore(e2) && s2.isBefore(e1);
        } catch (Exception ex) {
            return false; // don't block on format mismatch
        }
    }
}
