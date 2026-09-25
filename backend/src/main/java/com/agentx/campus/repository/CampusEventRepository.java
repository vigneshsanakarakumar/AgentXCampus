package com.agentx.campus.repository;

import com.agentx.campus.model.CampusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampusEventRepository extends JpaRepository<CampusEvent, Long> {
    List<CampusEvent> findAllByOrderByEventDateAsc();
}
