package com.agentx.campus.repository;

import com.agentx.campus.model.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(String department, String section);
    List<TimetableEntry> findByDepartmentOrderBySectionAscDayOfWeekAscStartTimeAsc(String department);
    List<TimetableEntry> findByDepartmentAndSectionAndDayOfWeekOrderByStartTimeAsc(String department, String section, String dayOfWeek);
    List<TimetableEntry> findAllByOrderByDepartmentAscSectionAscDayOfWeekAscStartTimeAsc();
    List<TimetableEntry> findByFacultyUserIdOrderByDayOfWeekAscStartTimeAsc(Long facultyUserId);
    List<TimetableEntry> findByFacultyNameIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(String facultyName);
    List<TimetableEntry> findByFacultyNameContainingIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(String facultyName);
    List<TimetableEntry> findByDayOfWeekAndStartTime(String dayOfWeek, String startTime);
}
