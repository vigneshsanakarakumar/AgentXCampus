package com.agentx.campus.repository;

import com.agentx.campus.model.AttendanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AttendanceEntryRepository extends JpaRepository<AttendanceEntry, Long> {
    List<AttendanceEntry> findBySession_IdOrderByStudent_Id(Long sessionId);
    List<AttendanceEntry> findByStudent_IdOrderBySession_SessionDateDesc(Long studentId);
    Optional<AttendanceEntry> findBySession_IdAndStudent_Id(Long sessionId, Long studentId);
    List<AttendanceEntry> findByStudent_IdAndSession_SubjectCode(Long studentId, String subjectCode);
}
