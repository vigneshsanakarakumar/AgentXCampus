package com.agentx.campus.repository;

import com.agentx.campus.model.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findBySection_IdOrderBySessionDateDescCreatedAtDesc(Long sectionId);
    List<AttendanceSession> findByFaculty_IdOrderBySessionDateDesc(Long facultyId);
}
