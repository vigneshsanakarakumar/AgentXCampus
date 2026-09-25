package com.agentx.campus;

import com.agentx.campus.model.ExamSchedule;
import com.agentx.campus.model.TimetableEntry;
import com.agentx.campus.repository.ApprovalRequestRepository;
import com.agentx.campus.repository.ExamScheduleRepository;
import com.agentx.campus.repository.TimetableEntryRepository;
import com.agentx.campus.service.ConflictEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ConflictEngineTest {

    private TimetableEntryRepository timetableRepo;
    private ApprovalRequestRepository approvalRepo;
    private ExamScheduleRepository examRepo;
    private ConflictEngine conflictEngine;

    @BeforeEach
    void setUp() {
        timetableRepo = Mockito.mock(TimetableEntryRepository.class);
        approvalRepo = Mockito.mock(ApprovalRequestRepository.class);
        examRepo = Mockito.mock(ExamScheduleRepository.class);
        conflictEngine = new ConflictEngine(timetableRepo, approvalRepo, examRepo);
    }

    @Test
    @DisplayName("Should detect room double-booking conflict for exams")
    void testExamRoomConflict() {
        LocalDate examDate = LocalDate.now().plusDays(2);
        String room = "Room 302";

        ExamSchedule existingExam = new ExamSchedule();
        existingExam.setId(101L);
        existingExam.setExamDate(examDate);
        existingExam.setRoom(room);
        existingExam.setStartTime("10:00 AM");
        existingExam.setEndTime("01:00 PM");
        existingExam.setSubjectName("Computer Networks");
        existingExam.setDepartment("CSE");
        existingExam.setSection("B");

        when(examRepo.findByExamDateAndRoomOrderByStartTimeAsc(examDate, room))
                .thenReturn(List.of(existingExam));
        when(examRepo.findByExamDateAndDepartmentAndSection(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        when(timetableRepo.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());

        ExamSchedule candidate = new ExamSchedule();
        candidate.setExamDate(examDate);
        candidate.setRoom(room);
        candidate.setStartTime("10:00 AM");
        candidate.setEndTime("01:00 PM");
        candidate.setSubjectName("Database Management Systems");
        candidate.setDepartment("CSE");
        candidate.setSection("A");

        List<Map<String, Object>> conflicts = conflictEngine.checkExamConflicts(candidate, null);

        assertFalse(conflicts.isEmpty(), "Expected room conflict to be detected");
        assertEquals("EXAM_ROOM_CONFLICT", conflicts.get(0).get("type"));
        assertEquals("HIGH", conflicts.get(0).get("severity"));
    }

    @Test
    @DisplayName("Should pass cleanly when exam room and slot are completely vacant")
    void testExamNoConflict() {
        LocalDate examDate = LocalDate.now().plusDays(3);
        String room = "Room 401";

        when(examRepo.findByExamDateAndRoomOrderByStartTimeAsc(examDate, room))
                .thenReturn(Collections.emptyList());
        when(examRepo.findByExamDateAndDepartmentAndSection(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        when(timetableRepo.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());

        ExamSchedule candidate = new ExamSchedule();
        candidate.setExamDate(examDate);
        candidate.setRoom(room);
        candidate.setStartTime("09:00 AM");
        candidate.setEndTime("12:00 PM");
        candidate.setDepartment("CSE");
        candidate.setSection("C");

        List<Map<String, Object>> conflicts = conflictEngine.checkExamConflicts(candidate, null);

        assertTrue(conflicts.isEmpty(), "Expected zero conflicts for vacant room and slot");
    }

    @Test
    @DisplayName("Should detect regular timetable class overlap with exam")
    void testExamTimetableOverlap() {
        LocalDate examDate = LocalDate.of(2026, 9, 28); // A Monday
        String dayOfWeek = "Monday";

        TimetableEntry regularClass = new TimetableEntry();
        regularClass.setId(1L);
        regularClass.setDayOfWeek(dayOfWeek);
        regularClass.setStartTime("10:00 AM");
        regularClass.setEndTime("11:00 AM");
        regularClass.setSubjectName("Operating Systems");
        regularClass.setDepartment("CSE");
        regularClass.setSection("A");

        when(examRepo.findByExamDateAndRoomOrderByStartTimeAsc(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        when(examRepo.findByExamDateAndDepartmentAndSection(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        when(timetableRepo.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc("CSE", "A"))
                .thenReturn(List.of(regularClass));

        ExamSchedule candidate = new ExamSchedule();
        candidate.setExamDate(examDate);
        candidate.setRoom("Auditorium");
        candidate.setStartTime("10:00 AM");
        candidate.setEndTime("01:00 PM");
        candidate.setDepartment("CSE");
        candidate.setSection("A");
        candidate.setSubjectName("Operating Systems Midterm");

        List<Map<String, Object>> conflicts = conflictEngine.checkExamConflicts(candidate, null);

        assertFalse(conflicts.isEmpty(), "Expected timetable overlap conflict");
        assertEquals("EXAM_TIMETABLE_OVERLAP", conflicts.get(0).get("type"));
    }
}
