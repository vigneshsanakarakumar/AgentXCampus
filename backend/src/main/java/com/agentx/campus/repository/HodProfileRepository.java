package com.agentx.campus.repository;

import com.agentx.campus.model.HodProfile;
import com.agentx.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HodProfileRepository extends JpaRepository<HodProfile, Long> {
    Optional<HodProfile> findByUser(User user);
    Optional<HodProfile> findByDepartment(String department);
    Optional<HodProfile> findByUserUsername(String username);
}
