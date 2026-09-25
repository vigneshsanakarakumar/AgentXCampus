package com.agentx.campus.repository;

import com.agentx.campus.model.CampusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampusEventRepository extends JpaRepository<CampusEvent, Long> {
    List<CampusEvent> findAllByOrderByEventDateAsc();

    @Query("SELECT e FROM CampusEvent e WHERE " +
           "(e.targetDepartment IS NULL OR e.targetDepartment = :dept) AND " +
           "(e.targetSection IS NULL OR e.targetSection = :section) " +
           "ORDER BY e.eventDate ASC")
    List<CampusEvent> findForStudent(@Param("dept") String dept, @Param("section") String section);

    @Query("SELECT e FROM CampusEvent e WHERE " +
           "(e.targetDepartment IS NULL OR e.targetDepartment = :dept) " +
           "ORDER BY e.eventDate ASC")
    List<CampusEvent> findForDepartment(@Param("dept") String dept);
}
