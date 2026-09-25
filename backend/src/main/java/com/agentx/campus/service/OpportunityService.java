package com.agentx.campus.service;

import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepo;
    private final UserRepository userRepo;
    private final StudentProfileRepository profileRepo;

    public OpportunityService(OpportunityRepository opportunityRepo,
                               UserRepository userRepo,
                               StudentProfileRepository profileRepo) {
        this.opportunityRepo = opportunityRepo;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
    }

    @Transactional
    public Opportunity createOpportunity(Map<String, Object> body, String adminUsername) {
        User admin = userRepo.findByUsername(adminUsername).orElseThrow();

        Opportunity opp = new Opportunity();
        opp.setTitle(body.getOrDefault("title", "Opportunity").toString());
        opp.setDescription(body.getOrDefault("description", "").toString());
        opp.setType(body.getOrDefault("type", "INTERNSHIP").toString().toUpperCase());
        opp.setDepartmentFilter(body.containsKey("departmentFilter")
                ? body.get("departmentFilter").toString() : null);
        opp.setYearFilter(body.containsKey("yearFilter") && body.get("yearFilter") != null
                ? Integer.parseInt(body.get("yearFilter").toString()) : null);
        opp.setEligibilityText(body.containsKey("eligibilityText")
                ? body.get("eligibilityText").toString() : null);
        opp.setExternalLink(body.containsKey("externalLink")
                ? body.get("externalLink").toString() : null);
        if (body.containsKey("deadline") && body.get("deadline") != null) {
            try { opp.setDeadline(LocalDate.parse(body.get("deadline").toString())); }
            catch (Exception ignored) {}
        }
        opp.setPostedBy(admin);
        opp.setActive(true);
        return opportunityRepo.save(opp);
    }

    public List<Opportunity> getAllOpportunitiesAdmin() {
        return opportunityRepo.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Opportunity updateOpportunity(Long id, Map<String, Object> body) {
        Opportunity opp = opportunityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));

        if (body.containsKey("title")) opp.setTitle(body.get("title").toString());
        if (body.containsKey("description")) opp.setDescription(body.get("description").toString());
        if (body.containsKey("type")) opp.setType(body.get("type").toString().toUpperCase());
        if (body.containsKey("departmentFilter")) opp.setDepartmentFilter(
                body.get("departmentFilter") != null ? body.get("departmentFilter").toString() : null);
        if (body.containsKey("yearFilter")) opp.setYearFilter(
                body.get("yearFilter") != null ? Integer.parseInt(body.get("yearFilter").toString()) : null);
        if (body.containsKey("eligibilityText")) opp.setEligibilityText(body.get("eligibilityText").toString());
        if (body.containsKey("externalLink")) opp.setExternalLink(body.get("externalLink").toString());
        if (body.containsKey("deadline") && body.get("deadline") != null) {
            try { opp.setDeadline(LocalDate.parse(body.get("deadline").toString())); }
            catch (Exception ignored) {}
        }
        if (body.containsKey("isActive")) opp.setActive(Boolean.parseBoolean(body.get("isActive").toString()));
        return opportunityRepo.save(opp);
    }

    @Transactional
    public void deleteOpportunity(Long id) {
        Opportunity opp = opportunityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));
        opp.setActive(false); // soft delete
        opportunityRepo.save(opp);
    }

    public List<Opportunity> getStudentOpportunities(String username, String type) {
        User user = userRepo.findByUsername(username).orElseThrow();
        StudentProfile profile = profileRepo.findByUser(user).orElse(null);

        String dept = profile != null ? profile.getDepartment() : null;
        Integer year = profile != null ? profile.getYear() : null;

        List<Opportunity> all = (type != null && !type.isEmpty())
                ? opportunityRepo.findByIsActiveTrueAndTypeOrderByCreatedAtDesc(type.toUpperCase())
                : opportunityRepo.findByIsActiveTrueOrderByCreatedAtDesc();

        return all.stream().filter(opp -> {
            boolean deptMatch = opp.getDepartmentFilter() == null
                    || dept == null
                    || opp.getDepartmentFilter().equalsIgnoreCase(dept);
            boolean yearMatch = opp.getYearFilter() == null
                    || year == null
                    || opp.getYearFilter().equals(year);
            return deptMatch && yearMatch;
        }).collect(Collectors.toList());
    }

    public Optional<Opportunity> getOpportunityById(Long id) {
        return opportunityRepo.findById(id);
    }
}
