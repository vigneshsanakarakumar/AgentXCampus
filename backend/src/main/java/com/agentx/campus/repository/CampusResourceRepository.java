package com.agentx.campus.repository;

import com.agentx.campus.model.CampusResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampusResourceRepository extends JpaRepository<CampusResource, Long> {
    Optional<CampusResource> findByRoomNumber(String roomNumber);
    List<CampusResource> findByBuilding(String building);
    List<CampusResource> findByType(String type);
    List<CampusResource> findByStatus(String status);
}
