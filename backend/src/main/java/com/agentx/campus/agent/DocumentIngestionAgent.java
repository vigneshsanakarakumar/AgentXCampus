package com.agentx.campus.agent;

import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import com.agentx.campus.service.DocumentTextExtractor;
import com.agentx.campus.service.GroqAiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DocumentIngestionAgent {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionAgent.class);

    private final DocumentTextExtractor textExtractor;
    private final GroqAiService groqAiService;
    private final AnnouncementRepository announcementRepository;
    private final CampusEventRepository campusEventRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final NotificationAgent notificationAgent;
    private final ObjectMapper objectMapper;

    public DocumentIngestionAgent(
            DocumentTextExtractor textExtractor,
            GroqAiService groqAiService,
            AnnouncementRepository announcementRepository,
            CampusEventRepository campusEventRepository,
            TimetableEntryRepository timetableEntryRepository,
            StudentProfileRepository studentProfileRepository,
            FacultyProfileRepository facultyProfileRepository,
            NotificationAgent notificationAgent,
            ObjectMapper objectMapper) {
        this.textExtractor = textExtractor;
        this.groqAiService = groqAiService;
        this.announcementRepository = announcementRepository;
        this.campusEventRepository = campusEventRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.notificationAgent = notificationAgent;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> ingestDocument(byte[] fileBytes, String filename, String textOverride,
                                             String uploaderUsername, String departmentScope, String sectionScope) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        // 1. Extract text from document
        String rawText = textOverride != null && !textOverride.isBlank()
                ? textOverride.trim()
                : textExtractor.extractText(fileBytes, filename);

        if (rawText.isBlank()) {
            result.put("success", false);
            result.put("error", "No readable text content could be extracted from the uploaded document.");
            return result;
        }

        result.put("extractedRawLength", rawText.length());
        result.put("rawSnippet", rawText.length() > 200 ? rawText.substring(0, 200) + "..." : rawText);

        // 2. Structured LLM Classification via Groq
        String prompt = "You are the AgentX Campus Autonomous Document Ingestion & Classification Agent.\n"
                + "Analyze the document text and classify its operational intent, target audience, and metadata.\n"
                + "Contextual hints: Department = '" + (departmentScope != null ? departmentScope : "Computer Science & Engineering")
                + "', Section hint = '" + (sectionScope != null ? sectionScope : "") + "'.\n\n"
                + "Return ONLY a valid JSON object matching this schema:\n"
                + "{\n"
                + "  \"documentType\": \"LEAVE_CIRCULAR | SEMINAR_NOTICE | EXAM_NOTICE | CLASS_TIMETABLE | FACULTY_TIMETABLE | CAMPUS_EVENT | GENERAL_NOTICE\",\n"
                + "  \"title\": \"Clear descriptive title of notice\",\n"
                + "  \"summary\": \"Detailed summary of circular contents\",\n"
                + "  \"targetAudience\": \"ALL | DEPARTMENT | SECTION | FACULTY | STUDENTS\",\n"
                + "  \"targetDepartment\": \"e.g. Computer Science & Engineering or null\",\n"
                + "  \"targetSection\": \"A | B | C | D or null (IMPORTANT: If the text specifically says Section A or Sec-A, specify 'A')\",\n"
                + "  \"priority\": \"URGENT | HIGH | NORMAL\",\n"
                + "  \"eventDate\": \"YYYY-MM-DD or null\",\n"
                + "  \"eventTime\": \"HH:MM AM/PM or null\",\n"
                + "  \"venue\": \"Classroom or Seminar Hall location or null\",\n"
                + "  \"timetableRows\": [\n"
                + "     {\n"
                + "       \"dayOfWeek\": \"Monday | Tuesday | Wednesday | Thursday | Friday\",\n"
                + "       \"startTime\": \"09:00 AM\",\n"
                + "       \"endTime\": \"10:00 AM\",\n"
                + "       \"subjectCode\": \"CS301\",\n"
                + "       \"subjectName\": \"Database Management Systems\",\n"
                + "       \"classroom\": \"CS-204\",\n"
                + "       \"facultyName\": \"Dr. Priya\"\n"
                + "     }\n"
                + "  ]\n"
                + "}\n"
                + "CRITICAL RULES:\n"
                + "- If document is a leave circular or holiday order, set documentType='LEAVE_CIRCULAR', targetAudience='ALL' or 'DEPARTMENT', targetSection=null.\n"
                + "- If document announces a seminar, workshop, or event for a specific section (e.g. Section A), set documentType='SEMINAR_NOTICE', targetSection='A', targetAudience='SECTION'.\n"
                + "- If document is a weekly timetable with schedules, set documentType='CLASS_TIMETABLE' or 'FACULTY_TIMETABLE' and populate timetableRows.\n";

        String jsonResponse = groqAiService.generateStructuredJson(prompt, rawText);
        JsonNode parsed = null;

        if (jsonResponse != null) {
            try {
                parsed = objectMapper.readTree(jsonResponse);
            } catch (Exception ex) {
                log.warn("Failed to parse Groq classification JSON: {}", ex.getMessage());
            }
        }

        // Fallback heuristic classification if LLM JSON parse failed
        String docType = (parsed != null && parsed.has("documentType"))
                ? parsed.get("documentType").asText("GENERAL_NOTICE")
                : detectFallbackType(rawText);

        String title = (parsed != null && parsed.has("title"))
                ? parsed.get("title").asText("Campus Notice")
                : (filename != null ? filename.replaceFirst("\\.[^.]+$", "") : "Campus Circular");

        String summary = (parsed != null && parsed.has("summary"))
                ? parsed.get("summary").asText(rawText)
                : rawText;

        String audience = (parsed != null && parsed.has("targetAudience"))
                ? parsed.get("targetAudience").asText("ALL")
                : "ALL";

        String dept = (parsed != null && parsed.has("targetDepartment") && !parsed.get("targetDepartment").isNull())
                ? parsed.get("targetDepartment").asText(departmentScope)
                : (departmentScope != null ? departmentScope : "Computer Science & Engineering");

        String sec = (parsed != null && parsed.has("targetSection") && !parsed.get("targetSection").isNull())
                ? parsed.get("targetSection").asText(sectionScope)
                : sectionScope;

        // If rawText specifically contains Section A/B/C/D keywords, prioritize section detection
        if (sec == null || sec.isBlank() || "null".equalsIgnoreCase(sec)) {
            sec = extractSectionKeyword(rawText);
        }

        String priority = (parsed != null && parsed.has("priority"))
                ? parsed.get("priority").asText("NORMAL")
                : "NORMAL";

        String eventDateStr = (parsed != null && parsed.has("eventDate") && !parsed.get("eventDate").isNull())
                ? parsed.get("eventDate").asText()
                : null;

        String eventTime = (parsed != null && parsed.has("eventTime") && !parsed.get("eventTime").isNull())
                ? parsed.get("eventTime").asText()
                : "10:00 AM";

        String venue = (parsed != null && parsed.has("venue") && !parsed.get("venue").isNull())
                ? parsed.get("venue").asText()
                : "Vikram Sarabhai Seminar Hall";

        // 3. Automated Operational Action Dispatch
        String actionTaken;
        int recipientsCount = 0;

        if ("CLASS_TIMETABLE".equalsIgnoreCase(docType) || "FACULTY_TIMETABLE".equalsIgnoreCase(docType)
                || (parsed != null && parsed.has("timetableRows") && parsed.get("timetableRows").isArray() && parsed.get("timetableRows").size() > 0)) {

            int rowsAdded = 0;
            if (parsed != null && parsed.has("timetableRows")) {
                for (JsonNode row : parsed.get("timetableRows")) {
                    TimetableEntry te = new TimetableEntry();
                    te.setDepartment(dept);
                    te.setSection(sec != null ? sec : (sectionScope != null ? sectionScope : "A"));
                    te.setDayOfWeek(row.path("dayOfWeek").asText("Monday"));
                    te.setStartTime(row.path("startTime").asText("09:00 AM"));
                    te.setEndTime(row.path("endTime").asText("10:00 AM"));
                    te.setSubjectCode(row.path("subjectCode").asText("CS301"));
                    te.setSubjectName(row.path("subjectName").asText("Subject Lecture"));
                    te.setClassroom(row.path("classroom").asText(venue != null ? venue : "CS-204"));
                    te.setFacultyName(row.path("facultyName").asText("Staff"));
                    timetableEntryRepository.save(te);
                    rowsAdded++;
                }
            }
            actionTaken = "Updated " + rowsAdded + " timetable entries for " + dept + (sec != null ? " Section " + sec : "");
            if (sec != null) {
                notificationAgent.notifySection(dept, sec, "Timetable Updated: " + title, "New timetable schedule has been imported and applied.", "TIMETABLE_UPDATE");
                recipientsCount = studentProfileRepository.findByDepartmentAndSection(dept, sec).size();
            }

        } else if ("SEMINAR_NOTICE".equalsIgnoreCase(docType) || "CAMPUS_EVENT".equalsIgnoreCase(docType)) {
            // Create CampusEvent
            CampusEvent event = new CampusEvent();
            event.setTitle(title);
            event.setCategory("Seminar");
            event.setDescription(summary);
            event.setLocation(venue != null ? venue : "Seminar Hall");
            event.setEventDate(eventDateStr != null ? parseDateSafe(eventDateStr) : LocalDate.now().plusDays(4));
            event.setEventTime(eventTime != null ? eventTime : "10:00 AM");
            event.setOrganizer(dept + (sec != null ? " Sec " + sec : ""));
            event.setTargetDepartment(dept);
            event.setTargetSection(sec);
            event.setTargetAudience(sec != null ? "SECTION" : "DEPARTMENT");
            campusEventRepository.save(event);

            // Create Announcement
            Announcement a = new Announcement();
            a.setTitle(title);
            a.setContent(summary + (venue != null ? " | Venue: " + venue : "") + (eventDateStr != null ? " | Date: " + eventDateStr : ""));
            a.setPriority(priority);
            a.setAuthorRole("HOD");
            a.setTargetDepartment(dept);
            a.setTargetSection(sec);
            a.setTargetAudience(sec != null ? "SECTION" : "DEPARTMENT");
            announcementRepository.save(a);

            // Targeted Notification:
            // "if its a seminar for A sec student then the code automatically send only to A sec std"
            if (sec != null && !sec.isBlank()) {
                notificationAgent.notifySection(dept, sec, "New Seminar: " + title, summary, "CAMPUS_ALERT");
                recipientsCount = studentProfileRepository.findByDepartmentAndSection(dept, sec).size();
                actionTaken = "Auto-detected target [Section " + sec + "]. Scheduled seminar and notified ONLY " + dept + " Section " + sec + " students (" + recipientsCount + " students).";
            } else {
                notificationAgent.notifyDepartment(dept, "Department Seminar: " + title, summary, "CAMPUS_ALERT");
                recipientsCount = studentProfileRepository.findByDepartment(dept).size();
                actionTaken = "Scheduled department-wide seminar for all sections in " + dept + " (" + recipientsCount + " students).";
            }

        } else {
            // LEAVE_CIRCULAR, EXAM_NOTICE, GENERAL_NOTICE
            // "if its a leave circular then update all"
            Announcement a = new Announcement();
            a.setTitle(title);
            a.setContent(summary);
            a.setPriority(priority);
            a.setAuthorRole("HOD");
            a.setTargetDepartment("LEAVE_CIRCULAR".equalsIgnoreCase(docType) ? null : dept);
            a.setTargetSection(sec);
            a.setTargetAudience("LEAVE_CIRCULAR".equalsIgnoreCase(docType) ? "ALL" : (sec != null ? "SECTION" : "DEPARTMENT"));
            announcementRepository.save(a);

            if ("LEAVE_CIRCULAR".equalsIgnoreCase(docType)) {
                // Broadcast to all
                notificationAgent.notifyAllUsers("Institution Circular: " + title, summary, "CAMPUS_ALERT");
                recipientsCount = (int) studentProfileRepository.count();
                actionTaken = "Broadcast institutional leave circular to ALL students and faculty across campus (" + recipientsCount + " users).";
            } else if (sec != null && !sec.isBlank()) {
                notificationAgent.notifySection(dept, sec, "Notice for Section " + sec + ": " + title, summary, "CAMPUS_ALERT");
                recipientsCount = studentProfileRepository.findByDepartmentAndSection(dept, sec).size();
                actionTaken = "Dispatched notice exclusively to " + dept + " Section " + sec + " (" + recipientsCount + " students).";
            } else {
                notificationAgent.notifyDepartment(dept, "Department Circular: " + title, summary, "CAMPUS_ALERT");
                recipientsCount = studentProfileRepository.findByDepartment(dept).size();
                actionTaken = "Dispatched circular to all sections in " + dept + " (" + recipientsCount + " students).";
            }
        }

        long latency = System.currentTimeMillis() - startTime;

        result.put("success", true);
        result.put("documentType", docType);
        result.put("title", title);
        result.put("summary", summary);
        result.put("targetAudience", audience);
        result.put("targetDepartment", dept);
        result.put("targetSection", sec);
        result.put("actionTaken", actionTaken);
        result.put("recipientsNotified", recipientsCount);
        result.put("venue", venue);
        result.put("eventDate", eventDateStr);
        result.put("eventTime", eventTime);
        result.put("latencyMs", latency);

        return result;
    }

    private String detectFallbackType(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("holiday") || lower.contains("leave") || lower.contains("closed") || lower.contains("re-open")) {
            return "LEAVE_CIRCULAR";
        }
        if (lower.contains("seminar") || lower.contains("guest lecture") || lower.contains("symposium") || lower.contains("workshop")) {
            return "SEMINAR_NOTICE";
        }
        if (lower.contains("timetable") || lower.contains("schedule") || lower.contains("period")) {
            return "CLASS_TIMETABLE";
        }
        if (lower.contains("exam") || lower.contains("cia") || lower.contains("assessment")) {
            return "EXAM_NOTICE";
        }
        return "GENERAL_NOTICE";
    }

    private String extractSectionKeyword(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("section a") || lower.contains("sec a") || lower.contains("sec-a") || lower.contains("cse a") || lower.contains("cse-a")) return "A";
        if (lower.contains("section b") || lower.contains("sec b") || lower.contains("sec-b") || lower.contains("cse b") || lower.contains("cse-b")) return "B";
        if (lower.contains("section c") || lower.contains("sec c") || lower.contains("sec-c") || lower.contains("cse c") || lower.contains("cse-c")) return "C";
        if (lower.contains("section d") || lower.contains("sec d") || lower.contains("sec-d") || lower.contains("cse d") || lower.contains("cse-d")) return "D";
        return null;
    }

    private LocalDate parseDateSafe(String dateStr) {
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception ex) {
            return LocalDate.now().plusDays(5);
        }
    }
}
