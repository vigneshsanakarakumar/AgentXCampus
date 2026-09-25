package com.agentx.campus;

import com.agentx.campus.model.StudentTask;
import com.agentx.campus.model.User;
import com.agentx.campus.repository.*;
import com.agentx.campus.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SecurityAccessTest {

    private UserRepository userRepo;
    private StudentProfileRepository profileRepo;
    private FacultyProfileRepository facultyProfileRepo;
    private TimetableEntryRepository timetableRepo;
    private GrievanceRepository grievanceRepo;
    private CampusEventRepository campusEventRepo;
    private AnnouncementRepository announcementRepo;
    private AssignmentRepository assignmentRepo;
    private AttendanceRecordRepository attendanceRepo;
    private StudentTaskRepository taskRepo;
    private FacultyMentorSectionRepository mentorSectionRepo;
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        userRepo = Mockito.mock(UserRepository.class);
        profileRepo = Mockito.mock(StudentProfileRepository.class);
        facultyProfileRepo = Mockito.mock(FacultyProfileRepository.class);
        timetableRepo = Mockito.mock(TimetableEntryRepository.class);
        grievanceRepo = Mockito.mock(GrievanceRepository.class);
        campusEventRepo = Mockito.mock(CampusEventRepository.class);
        announcementRepo = Mockito.mock(AnnouncementRepository.class);
        assignmentRepo = Mockito.mock(AssignmentRepository.class);
        attendanceRepo = Mockito.mock(AttendanceRecordRepository.class);
        taskRepo = Mockito.mock(StudentTaskRepository.class);
        mentorSectionRepo = Mockito.mock(FacultyMentorSectionRepository.class);

        studentService = new StudentService(
                userRepo, profileRepo, facultyProfileRepo, timetableRepo, grievanceRepo,
                campusEventRepo, announcementRepo, assignmentRepo, attendanceRepo, taskRepo, mentorSectionRepo
        );
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when student attempts to modify another student's task")
    void testTaskOwnershipEnforcement() {
        User owner = new User();
        owner.setId(10L);
        owner.setUsername("owner.student");

        StudentTask task = new StudentTask(
                owner, "Prepare CS301 assignment", "Unit 4 ER diagrams",
                "HIGH", "TODO", LocalDate.now().plusDays(2)
        );
        task.setId(99L);

        when(taskRepo.findById(99L)).thenReturn(Optional.of(task));

        // Attempting to update or delete as 'attacker.student'
        assertThrows(AccessDeniedException.class, () -> {
            studentService.updateTaskStatus(99L, "COMPLETED", "attacker.student");
        }, "Updating another user's task must throw AccessDeniedException");

        assertThrows(AccessDeniedException.class, () -> {
            studentService.deleteTask(99L, "attacker.student");
        }, "Deleting another user's task must throw AccessDeniedException");
    }

    @Test
    @DisplayName("Should succeed when legitimate owner updates or deletes their task")
    void testLegitimateOwnerTaskModification() {
        User owner = new User();
        owner.setId(10L);
        owner.setUsername("legit.student");

        StudentTask task = new StudentTask(
                owner, "Prepare CS301 assignment", "Unit 4 ER diagrams",
                "HIGH", "TODO", LocalDate.now().plusDays(2)
        );
        task.setId(99L);

        when(taskRepo.findById(99L)).thenReturn(Optional.of(task));
        when(taskRepo.save(task)).thenReturn(task);

        assertDoesNotThrow(() -> {
            studentService.updateTaskStatus(99L, "COMPLETED", "legit.student");
        });

        assertDoesNotThrow(() -> {
            studentService.deleteTask(99L, "legit.student");
        });
    }
}
