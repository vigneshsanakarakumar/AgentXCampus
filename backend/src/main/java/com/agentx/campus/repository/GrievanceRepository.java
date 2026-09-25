package com.agentx.campus.repository;

import com.agentx.campus.model.Grievance;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, Long> {
    List<Grievance> findByUserOrderByCreatedAtDesc(User user);
    List<Grievance> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);

    List<Grievance> findByAssignedToUserOrderByCreatedAtDesc(User user);
    List<Grievance> findByAssignedToRoleOrderByCreatedAtDesc(String role);
    List<Grievance> findByUser_RoleOrderByCreatedAtDesc(com.agentx.campus.model.Role role);
    List<Grievance> findByStatusOrderByCreatedAtDesc(String status);
    List<Grievance> findByUser_RoleAndStatusOrderByCreatedAtDesc(com.agentx.campus.model.Role role, String status);
    long countByAssignedToUserAndStatus(User user, String status);
}
