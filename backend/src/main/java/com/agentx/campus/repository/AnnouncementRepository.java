package com.agentx.campus.repository;

import com.agentx.campus.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByCreatedAtDesc();
    List<Announcement> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT a FROM Announcement a WHERE " +
           "(a.targetDepartment IS NULL OR a.targetDepartment = :dept) AND " +
           "(a.targetSection IS NULL OR a.targetSection = :section) " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findForStudent(@Param("dept") String dept, @Param("section") String section);

    @Query("SELECT a FROM Announcement a WHERE " +
           "(a.targetDepartment IS NULL OR a.targetDepartment = :dept) " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findForDepartment(@Param("dept") String dept);
}
