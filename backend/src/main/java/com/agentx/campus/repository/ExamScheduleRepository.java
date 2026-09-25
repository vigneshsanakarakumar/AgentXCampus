package com.agentx.campus.repository;

import com.agentx.campus.model.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {
    List<ExamSchedule> findByDepartmentAndSectionOrderByExamDateAscStartTimeAsc(String dept, String section);
    List<ExamSchedule> findByDepartmentAndSectionAndSemesterOrderByExamDateAsc(String dept, String section, int semester);
    List<ExamSchedule> findAllByOrderByExamDateAscDepartmentAsc();
    List<ExamSchedule> findByExamDateAndRoomOrderByStartTimeAsc(LocalDate date, String room);
    List<ExamSchedule> findByExamDateAndDepartmentAndSection(LocalDate date, String dept, String section);
}
