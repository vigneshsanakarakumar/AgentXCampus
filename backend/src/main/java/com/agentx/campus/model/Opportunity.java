package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities")
public class Opportunity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;
    /** INTERNSHIP | HACKATHON | WORKSHOP | CERTIFICATION | PLACEMENT_DRIVE | COMPETITION */
    @Column(nullable = false, length = 50) private String type;

    @Column(name = "department_filter", length = 100) private String departmentFilter;
    @Column(name = "year_filter") private Integer yearFilter;
    @Column(name = "eligibility_text", columnDefinition = "TEXT") private String eligibilityText;
    @Column(name = "external_link", length = 500) private String externalLink;
    private LocalDate deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by")
    @JsonIgnore
    private User postedBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "is_active", nullable = false) private boolean isActive = true;

    public Opportunity() {}
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getType() { return type; }
    public void setType(String v) { type = v; }
    public String getDepartmentFilter() { return departmentFilter; }
    public void setDepartmentFilter(String v) { departmentFilter = v; }
    public Integer getYearFilter() { return yearFilter; }
    public void setYearFilter(Integer v) { yearFilter = v; }
    public String getEligibilityText() { return eligibilityText; }
    public void setEligibilityText(String v) { eligibilityText = v; }
    public String getExternalLink() { return externalLink; }
    public void setExternalLink(String v) { externalLink = v; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate v) { deadline = v; }
    @JsonIgnore public User getPostedBy() { return postedBy; }
    public void setPostedBy(User v) { postedBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean v) { isActive = v; }

    // Computed
    public String getPostedByName() { return postedBy != null ? (postedBy.getFirstName() + " " + postedBy.getLastName()).trim() : "Admin"; }
}
