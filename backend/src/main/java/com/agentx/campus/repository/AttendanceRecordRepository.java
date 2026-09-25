package com.agentx.campus.repository;

import com.agentx.campus.model.AttendanceRecord;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByUserOrderByCourseCodeAsc(User user);
    Optional<AttendanceRecord> findByUserAndCourseCode(User user, String courseCode);
}
