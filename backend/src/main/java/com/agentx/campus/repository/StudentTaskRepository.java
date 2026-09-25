package com.agentx.campus.repository;

import com.agentx.campus.model.StudentTask;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentTaskRepository extends JpaRepository<StudentTask, Long> {
    List<StudentTask> findByUserOrderByCreatedAtDesc(User user);
    List<StudentTask> findByUserAndStatusOrderByDueDateAsc(User user, String status);
}
