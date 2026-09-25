package com.agentx.campus.repository;

import com.agentx.campus.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByDepartmentOrderBySemesterAscCourseCodeAsc(String department);
    List<Course> findByDepartmentAndSemester(String department, int semester);
    boolean existsByCourseCode(String courseCode);
}
