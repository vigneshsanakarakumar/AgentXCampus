package com.agentx.campus.repository;

import com.agentx.campus.model.CampusDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampusDocumentRepository extends JpaRepository<CampusDocument, Long> {
    List<CampusDocument> findByActiveTrueOrderByCreatedAtDesc();
    List<CampusDocument> findByCategoryAndActiveTrue(String category);
    List<CampusDocument> findByDepartmentAndActiveTrue(String department);
    List<CampusDocument> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titleKeyword, String descKeyword
    );
}
