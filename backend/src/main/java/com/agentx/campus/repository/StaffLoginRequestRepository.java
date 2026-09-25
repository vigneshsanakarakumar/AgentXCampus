package com.agentx.campus.repository;

import com.agentx.campus.model.StaffLoginRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffLoginRequestRepository extends JpaRepository<StaffLoginRequest, Long> {
    Optional<StaffLoginRequest> findByEmail(String email);
    Optional<StaffLoginRequest> findByInviteToken(String inviteToken);
    List<StaffLoginRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<StaffLoginRequest> findAllByOrderByCreatedAtDesc();
    boolean existsByEmail(String email);
}
