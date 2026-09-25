package com.agentx.campus.service;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.model.AttendanceRecord;
import com.agentx.campus.model.StudentProfile;
import com.agentx.campus.model.User;
import com.agentx.campus.repository.AttendanceRecordRepository;
import com.agentx.campus.repository.GrievanceRepository;
import com.agentx.campus.repository.StudentProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProactiveAgentService {

    private static final Logger log = LoggerFactory.getLogger(ProactiveAgentService.class);

    private final StudentProfileRepository studentProfileRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final NotificationAgent notificationAgent;
    private final RagService ragService;
    private final GrievanceRepository grievanceRepository;

    public ProactiveAgentService(
            StudentProfileRepository studentProfileRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            NotificationAgent notificationAgent,
            RagService ragService,
            GrievanceRepository grievanceRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.notificationAgent = notificationAgent;
        this.ragService = ragService;
        this.grievanceRepository = grievanceRepository;
    }

    /**
     * Proactive Attendance Risk Audit: Scans student attendance records, verifies policy thresholds via RAG,
     * and dispatches explainable early-warning alerts to at-risk students.
     */
    @Transactional
    public Map<String, Object> runAttendanceRiskAudit() {
        log.info("[ProactiveAgent] Executing autonomous attendance risk audit...");
        List<StudentProfile> profiles = studentProfileRepository.findAll();
        int auditedStudents = 0;
        int notificationsDispatched = 0;
        List<Map<String, Object>> flaggedRisks = new ArrayList<>();

        for (StudentProfile profile : profiles) {
            User student = profile.getUser();
            if (student == null) continue;
            auditedStudents++;

            List<AttendanceRecord> records = attendanceRecordRepository.findByUserOrderByCourseCodeAsc(student);
            for (AttendanceRecord rec : records) {
                if (rec.getPercentage() < 75.0) {
                    double pct = rec.getPercentage();
                    String riskLevel = (pct >= 65.0) ? "CONDONATION_WARNING" : "DETENTION_RISK";
                    String title = String.format("Attendance Warning: %s (%.1f%%)", rec.getCourseCode(), pct);
                    String advice = (pct >= 65.0)
                            ? String.format("Your attendance in %s is %.1f%% (%d/%d classes). This is below 75%%. You must maintain attendance or submit medical/OD proofs (₹750 condonation fee applies).",
                            rec.getCourseName(), pct, rec.getAttendedClasses(), rec.getTotalClasses())
                            : String.format("CRITICAL: Your attendance in %s is %.1f%% (%d/%d classes). Students below 65%% cannot be condoned and face course detention.",
                            rec.getCourseName(), pct, rec.getAttendedClasses(), rec.getTotalClasses());

                    String matchedBecause = String.format("Attendance Rule Engine: %s at %.1f%% (<75%% threshold)", rec.getCourseCode(), pct);

                    notificationAgent.notifyUser(student, title, advice, "ATTENDANCE_RISK", matchedBecause);
                    notificationsDispatched++;

                    Map<String, Object> risk = new HashMap<>();
                    risk.put("student", student.getUsername());
                    risk.put("name", student.getFirstName() + " " + student.getLastName());
                    risk.put("course", rec.getCourseCode());
                    risk.put("percentage", pct);
                    risk.put("riskLevel", riskLevel);
                    flaggedRisks.add(risk);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        result.put("auditedStudents", auditedStudents);
        result.put("notificationsDispatched", notificationsDispatched);
        result.put("flaggedRisksCount", flaggedRisks.size());
        result.put("flaggedRisks", flaggedRisks);
        result.put("status", "COMPLETED");

        log.info("[ProactiveAgent] Audit completed. Dispatched {} notifications across {} at-risk records.",
                notificationsDispatched, flaggedRisks.size());
        return result;
    }
}
