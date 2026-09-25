package com.agentx.campus.repository;

import com.agentx.campus.model.Role;
import com.agentx.campus.model.User;
import com.agentx.campus.model.UserRegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRegistrationRequestRepository extends JpaRepository<UserRegistrationRequest, Long> {
    List<UserRegistrationRequest> findByDepartmentOrderByCreatedAtDesc(String department);
    List<UserRegistrationRequest> findByDepartmentAndStatusOrderByCreatedAtDesc(String department, String status);
    List<UserRegistrationRequest> findByDepartmentAndRoleAndStatusOrderByCreatedAtDesc(String department, Role role, String status);
    Optional<UserRegistrationRequest> findByUsername(String username);
    Optional<UserRegistrationRequest> findByEmail(String email);
    Optional<UserRegistrationRequest> findByUser(User user);
    long countByDepartmentAndStatus(String department, String status);
}
