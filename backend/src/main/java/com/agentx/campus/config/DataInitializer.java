package com.agentx.campus.config;

import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final CampusResourceRepository campusResourceRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final AnnouncementRepository announcementRepository;
    private final CampusEventRepository campusEventRepository;
    private final CampusDocumentRepository campusDocumentRepository;
    private final StudentTaskRepository studentTaskRepository;
    private final FacultyMentorSectionRepository facultyMentorSectionRepository;
    private final StaffLoginRequestRepository staffLoginRequestRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ODRequestRepository odRequestRepository;
    private final GatePassRequestRepository gatePassRequestRepository;
    private final NotificationRepository notificationRepository;
    private final HodProfileRepository hodProfileRepository;
    private final FacultyLeaveRequestRepository facultyLeaveRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            FacultyProfileRepository facultyProfileRepository,
            CampusResourceRepository campusResourceRepository,
            CourseRepository courseRepository,
            AssignmentRepository assignmentRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            TimetableEntryRepository timetableEntryRepository,
            AnnouncementRepository announcementRepository,
            CampusEventRepository campusEventRepository,
            CampusDocumentRepository campusDocumentRepository,
            StudentTaskRepository studentTaskRepository,
            FacultyMentorSectionRepository facultyMentorSectionRepository,
            StaffLoginRequestRepository staffLoginRequestRepository,
            LeaveRequestRepository leaveRequestRepository,
            ODRequestRepository odRequestRepository,
            GatePassRequestRepository gatePassRequestRepository,
            NotificationRepository notificationRepository,
            HodProfileRepository hodProfileRepository,
            FacultyLeaveRequestRepository facultyLeaveRequestRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.campusResourceRepository = campusResourceRepository;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.announcementRepository = announcementRepository;
        this.campusEventRepository = campusEventRepository;
        this.campusDocumentRepository = campusDocumentRepository;
        this.studentTaskRepository = studentTaskRepository;
        this.facultyMentorSectionRepository = facultyMentorSectionRepository;
        this.staffLoginRequestRepository = staffLoginRequestRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.odRequestRepository = odRequestRepository;
        this.gatePassRequestRepository = gatePassRequestRepository;
        this.notificationRepository = notificationRepository;
        this.hodProfileRepository = hodProfileRepository;
        this.facultyLeaveRequestRepository = facultyLeaveRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedCampusResources();
        seedFaculty();
        seedMentorSections();
        seedCourses();
        seedTimetable();
        seedAssignments();
        seedNotices();
        seedEvents();
        seedStudentData();
        seedKnowledgeDocuments();
    }

    private void seedUsers() {
        // Administrator account: admin / admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User(
                    "admin",
                    "admin@campus.edu",
                    passwordEncoder.encode("admin"),
                    Role.ADMIN,
                    "System",
                    "Administrator"
            );
            userRepository.save(admin);
            System.out.println("[DataInitializer] Seeded administrator: admin / admin");
        }

        // Student account: vasan
        Optional<User> vasanOpt = userRepository.findByUsername("vasan");
        User vasan;
        if (vasanOpt.isEmpty()) {
            vasan = new User(
                    "vasan",
                    "717824p361@kce.ac.in",
                    passwordEncoder.encode("student123"),
                    Role.STUDENT,
                    "Vasanth",
                    "S"
            );
            vasan = userRepository.save(vasan);
            System.out.println("[DataInitializer] Seeded student: vasan / student123");
        } else {
            vasan = vasanOpt.get();
            vasan.setPasswordHash(passwordEncoder.encode("student123"));
            vasan.setActive(true);
            userRepository.save(vasan);
        }

        if (!studentProfileRepository.existsByRollNumber("717824P361")) {
            StudentProfile profile = new StudentProfile();
            profile.setUser(vasan);
            profile.setRollNumber("717824P361");
            profile.setDepartment("Computer Science & Engineering");
            profile.setSection("C");
            profile.setYear(3);
            profile.setSemester(5);
            profile.setCgpa(8.64);
            profile.setAttendanceRate(85);
            profile.setStudentType("HOSTELLER");
            profile.setHostelBlock("Kaveri Hostel Block B");
            profile.setRoomNumber("Room 304");
            studentProfileRepository.save(profile);
            System.out.println("[DataInitializer] Seeded student profile for 717824P361 (vasan)");
        }

        // HOD account: hod.cse / faculty123 (Head of CSE)
        Optional<User> hodOpt = userRepository.findByUsername("hod.cse");
        User hod;
        if (hodOpt.isEmpty()) {
            hod = new User(
                    "hod.cse",
                    "hod.cse@campus.edu",
                    passwordEncoder.encode("faculty123"),
                    Role.HOD,
                    "Dr. Arulmozhi",
                    "V"
            );
            hod = userRepository.save(hod);
            System.out.println("[DataInitializer] Seeded HOD: hod.cse / faculty123");
        } else {
            hod = hodOpt.get();
            hod.setRole(Role.HOD);
            hod.setPasswordHash(passwordEncoder.encode("faculty123"));
            hod.setActive(true);
            userRepository.save(hod);
        }

        if (hodProfileRepository.findByUser(hod).isEmpty()) {
            HodProfile hodProfile = new HodProfile(
                    hod,
                    "Computer Science & Engineering",
                    "Block A - Room 100",
                    "+91 94432 10987"
            );
            hodProfileRepository.save(hodProfile);
            System.out.println("[DataInitializer] Seeded HodProfile for Dr. Arulmozhi V (hod.cse)");
        }
    }

    private void seedCampusResources() {
        if (campusResourceRepository.count() == 0) {
            campusResourceRepository.saveAll(List.of(
                    new CampusResource("Foundational Computing Hall", "Block A", "CS-101", "CLASSROOM", 75, "AVAILABLE"),
                    new CampusResource("Software Engineering Hall", "Block A", "CS-102", "CLASSROOM", 70, "AVAILABLE"),
                    new CampusResource("Computer Science Lecture Hall 1", "Block A", "CS-204", "CLASSROOM", 60, "AVAILABLE"),
                    new CampusResource("Advanced Algorithms Hall", "Block A", "CS-301", "CLASSROOM", 60, "AVAILABLE"),
                    new CampusResource("Systems & Networking Lab", "Block B", "Lab 1", "LAB", 40, "AVAILABLE"),
                    new CampusResource("Autonomous AI & Robotics Lab", "Block B", "Lab 2", "LAB", 35, "AVAILABLE"),
                    new CampusResource("Vikram Sarabhai Seminar Hall", "Block C", "Seminar Hall 1", "SEMINAR_HALL", 150, "AVAILABLE")
            ));
            System.out.println("[DataInitializer] Seeded campus resources (7 rooms)");
        }
    }

    private void seedFaculty() {
        // Mentor 1: Dr. K. Ramesh (Mentor for CSE Section C, Sem 5)
        User u1 = userRepository.findByUsername("ramesh.k").orElseGet(() -> {
            User u = new User("ramesh.k", "ramesh.k@campus.edu", passwordEncoder.encode("faculty123"), Role.FACULTY, "Dr. K.", "Ramesh");
            return userRepository.save(u);
        });
        FacultyProfile fp1 = facultyProfileRepository.findByUser(u1).orElseGet(() -> {
            FacultyProfile fp = new FacultyProfile();
            fp.setUser(u1);
            fp.setEmployeeId("EMP-CSE-101");
            fp.setDepartment("Computer Science & Engineering");
            fp.setDesignation("Professor & Associate Dean");
            fp.setAssignedDepartment("Computer Science & Engineering");
            fp.setAssignedSection("C");
            fp.setMentor(true);
            fp.setCabinNumber("Block A - Cabin 101");
            return facultyProfileRepository.save(fp);
        });
        fp1.setMentor(true);
        fp1.setAssignedSection("C");
        facultyProfileRepository.save(fp1);

        // Mentor 2: Dr. M. Priya (Mentor for CSE Section A, Sem 3)
        User u2 = userRepository.findByUsername("priya.m").orElseGet(() -> {
            User u = new User("priya.m", "priya.m@campus.edu", passwordEncoder.encode("faculty123"), Role.FACULTY, "Dr. M.", "Priya");
            return userRepository.save(u);
        });
        FacultyProfile fp2 = facultyProfileRepository.findByUser(u2).orElseGet(() -> {
            FacultyProfile fp = new FacultyProfile();
            fp.setUser(u2);
            fp.setEmployeeId("EMP-CSE-102");
            fp.setDepartment("Computer Science & Engineering");
            fp.setDesignation("Associate Professor");
            fp.setAssignedDepartment("Computer Science & Engineering");
            fp.setAssignedSection("A");
            fp.setMentor(true);
            fp.setCabinNumber("Block A - Cabin 204");
            return facultyProfileRepository.save(fp);
        });
        fp2.setMentor(true);
        fp2.setAssignedSection("A");
        facultyProfileRepository.save(fp2);

        // Mentor 3: Dr. S. Suresh (Mentor for CSE Section B, Sem 4)
        User u3 = userRepository.findByUsername("suresh.s").orElseGet(() -> {
            User u = new User("suresh.s", "suresh.s@campus.edu", passwordEncoder.encode("faculty123"), Role.FACULTY, "Dr. S.", "Suresh");
            return userRepository.save(u);
        });
        FacultyProfile fp3 = facultyProfileRepository.findByUser(u3).orElseGet(() -> {
            FacultyProfile fp = new FacultyProfile();
            fp.setUser(u3);
            fp.setEmployeeId("EMP-CSE-103");
            fp.setDepartment("Computer Science & Engineering");
            fp.setDesignation("Associate Professor");
            fp.setAssignedDepartment("Computer Science & Engineering");
            fp.setAssignedSection("B");
            fp.setMentor(true);
            fp.setCabinNumber("Block B - Cabin 108");
            return facultyProfileRepository.save(fp);
        });
        fp3.setMentor(true);
        fp3.setAssignedSection("B");
        facultyProfileRepository.save(fp3);

        // Mentor 4: Prof. R. Anand (Mentor for CSE Section D, Sem 6)
        User u4 = userRepository.findByUsername("anand.r").orElseGet(() -> {
            User u = new User("anand.r", "anand.r@campus.edu", passwordEncoder.encode("faculty123"), Role.FACULTY, "Prof. R.", "Anand");
            return userRepository.save(u);
        });
        FacultyProfile fp4 = facultyProfileRepository.findByUser(u4).orElseGet(() -> {
            FacultyProfile fp = new FacultyProfile();
            fp.setUser(u4);
            fp.setEmployeeId("EMP-CSE-104");
            fp.setDepartment("Computer Science & Engineering");
            fp.setDesignation("Assistant Professor");
            fp.setAssignedDepartment("Computer Science & Engineering");
            fp.setAssignedSection("D");
            fp.setMentor(true);
            fp.setCabinNumber("Block B - Cabin 202");
            return facultyProfileRepository.save(fp);
        });
        fp4.setMentor(true);
        fp4.setAssignedSection("D");
        facultyProfileRepository.save(fp4);

        // Prospective staff login request awaiting approval
        if (!staffLoginRequestRepository.existsByEmail("prof.sharma@campus.edu")) {
            StaffLoginRequest req = new StaffLoginRequest(
                    "Dr. Vikram",
                    "Sharma",
                    "prof.sharma@campus.edu",
                    "Data Science & AI",
                    "Associate Professor & AI Lab Director"
            );
            staffLoginRequestRepository.save(req);
        }

        if (facultyLeaveRequestRepository.count() == 0) {
            FacultyLeaveRequest leave = new FacultyLeaveRequest();
            leave.setFaculty(u2); // Dr. M. Priya
            leave.setLeaveType("ON_DUTY");
            leave.setFromDate(LocalDate.now().plusDays(2));
            leave.setToDate(LocalDate.now().plusDays(3));
            leave.setSubstituteFacultyName("Prof. R. Anand");
            leave.setReason("Attending IEEE International Conference on Generative AI as session speaker");
            leave.setStatus("PENDING");
            leave.setDepartment("Computer Science & Engineering");
            leave.setCreatedAt(LocalDateTime.now().minusHours(4));
            facultyLeaveRequestRepository.save(leave);
            System.out.println("[DataInitializer] Seeded sample FacultyLeaveRequest for Dr. M. Priya");
        }

        System.out.println("[DataInitializer] Seeded 4 active Faculty Mentors (priya.m -> Sec A, suresh.s -> Sec B, ramesh.k -> Sec C, anand.r -> Sec D)");
    }

    private void seedMentorSections() {
        String dept = "Computer Science & Engineering";
        String ay = "2025-2026";

        ensureMentorSection("priya.m", dept, "A", 3, ay);
        ensureMentorSection("suresh.s", dept, "B", 4, ay);
        ensureMentorSection("ramesh.k", dept, "C", 5, ay);
        ensureMentorSection("anand.r", dept, "D", 6, ay);
    }

    private void ensureMentorSection(String username, String dept, String section, int semester, String academicYear) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return;
        FacultyProfile fp = facultyProfileRepository.findByUser(user).orElse(null);
        if (fp == null) return;

        boolean exists = facultyMentorSectionRepository.existsByFacultyProfileAndDepartmentAndSectionAndSemester(fp, dept, section, semester);
        if (!exists) {
            facultyMentorSectionRepository.save(new FacultyMentorSection(fp, dept, section, semester, academicYear));
            System.out.println("[DataInitializer] Mapped mentor " + username + " to " + dept + " Section " + section + " (Sem " + semester + ")");
        }
    }

    private void seedCourses() {
        String dept = "Computer Science & Engineering";

        // Sem 3 (Sec A)
        saveCourseIfAbsent("CS201", "Data Structures & Algorithms", dept, 3, 4, "Dr. M. Priya", "Linear data structures, trees, AVL, heaps, hash tables, and algorithm complexity.");
        saveCourseIfAbsent("CS202", "Digital Principles & System Design", dept, 3, 3, "Prof. R. Anand", "Boolean algebra, combinational circuits, sequential circuits, flip-flops, and counters.");
        saveCourseIfAbsent("CS203", "Object Oriented Programming in Java", dept, 3, 4, "Dr. S. Suresh", "Classes, inheritance, polymorphism, abstract classes, exception handling, and streams.");
        saveCourseIfAbsent("CS204", "Discrete Mathematics", dept, 3, 4, "Dr. Rajesh V", "Set theory, propositional logic, predicates, relations, graph theory, and recurrence relations.");
        saveCourseIfAbsent("CS205", "Data Structures Laboratory", dept, 3, 2, "Dr. M. Priya", "Practical implementation of linked lists, stacks, queues, trees, and sorting algorithms.");

        // Sem 4 (Sec B)
        saveCourseIfAbsent("CS251", "Design & Analysis of Algorithms", dept, 4, 4, "Dr. S. Suresh", "Divide and conquer, dynamic programming, greedy algorithms, backtracking, and NP-completeness.");
        saveCourseIfAbsent("CS252", "Computer Organization & Architecture", dept, 4, 3, "Prof. R. Anand", "Instruction set architectures, ALU design, pipelining, memory hierarchy, and cache design.");
        saveCourseIfAbsent("CS253", "Software Engineering", dept, 4, 4, "Dr. M. Priya", "Agile methodologies, Scrum, requirements modeling, software architecture, and unit testing.");
        saveCourseIfAbsent("CS254", "Probability & Queuing Theory", dept, 4, 4, "Prof. Deepa N", "Random variables, probability distributions, Markov chains, and queuing models.");
        saveCourseIfAbsent("CS255", "Algorithms Laboratory", dept, 4, 2, "Dr. S. Suresh", "Hands-on implementation of graph algorithms, minimum spanning trees, and dynamic programming.");

        // Sem 5 (Sec C)
        saveCourseIfAbsent("CS301", "Database Management Systems", dept, 5, 4, "Dr. M. Priya", "Relational database models, normal forms, transaction ACID properties, concurrency control, and SQL optimization.");
        saveCourseIfAbsent("CS302", "Computer Networks", dept, 5, 3, "Prof. R. Anand", "OSI & TCP/IP layered architecture, routing protocols (OSPF, BGP), congestion control algorithms, and socket programming.");
        saveCourseIfAbsent("CS303", "Operating Systems", dept, 5, 4, "Dr. S. Suresh", "Process scheduling algorithms, synchronization with semaphores and mutexes, virtual memory paging, and file systems.");
        saveCourseIfAbsent("CS304", "Artificial Intelligence", dept, 5, 4, "Dr. Rajesh V", "State space search, A* heuristic search, adversarial minimax game trees, knowledge representation, and reasoning.");
        saveCourseIfAbsent("CS305", "Theory of Computation", dept, 5, 3, "Prof. Deepa N", "Deterministic and non-deterministic finite automata, context-free grammars, pushdown automata, and Turing machines.");
        saveCourseIfAbsent("CS306", "Cloud Computing Laboratory", dept, 5, 2, "Dr. K. Ramesh", "Hands-on containerization with Docker, Kubernetes orchestration, RESTful microservices, and serverless compute.");

        // Sem 6 (Sec D)
        saveCourseIfAbsent("CS351", "Compiler Design", dept, 6, 4, "Prof. R. Anand", "Lexical analysis, syntax analysis (LL/LR parsers), semantic analysis, intermediate code generation, and optimization.");
        saveCourseIfAbsent("CS352", "Distributed Systems", dept, 6, 4, "Dr. K. Ramesh", "Distributed consensus (Raft/Paxos), RPC frameworks, fault tolerance, replication, and distributed storage.");
        saveCourseIfAbsent("CS353", "Web Technologies & Full Stack", dept, 6, 4, "Dr. S. Suresh", "Modern frontend frameworks (React), RESTful APIs, Spring Boot, authentication, and state management.");
        saveCourseIfAbsent("CS354", "Cryptography & Network Security", dept, 6, 3, "Dr. M. Priya", "Symmetric/asymmetric ciphers, AES, RSA, digital signatures, hash functions, and network security protocols.");
        saveCourseIfAbsent("CS355", "Full Stack Development Lab", dept, 6, 2, "Prof. R. Anand", "End-to-end full stack application development with database integration, CI/CD, and deployment.");

        System.out.println("[DataInitializer] Seeded comprehensive course catalog across Semesters 3, 4, 5, and 6");
    }

    private void saveCourseIfAbsent(String code, String name, String dept, int sem, int credits, String faculty, String desc) {
        if (courseRepository.findByCourseCode(code).isEmpty()) {
            courseRepository.save(new Course(code, name, dept, sem, credits, faculty, desc));
        }
    }

    private void seedTimetable() {
        String dept = "Computer Science & Engineering";

        // Section A (Sem 3)
        if (timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(dept, "A").isEmpty()) {
            timetableEntryRepository.saveAll(List.of(
                    // Monday
                    createEntry(dept, "A", 3, "Monday", "09:00 AM", "10:00 AM", "CS201", "Data Structures & Algorithms", "Dr. M. Priya", "CS-101"),
                    createEntry(dept, "A", 3, "Monday", "10:15 AM", "11:15 AM", "CS202", "Digital Principles & System Design", "Prof. R. Anand", "CS-101"),
                    createEntry(dept, "A", 3, "Monday", "11:30 AM", "12:30 PM", "CS203", "Object Oriented Programming in Java", "Dr. S. Suresh", "CS-101"),
                    createEntry(dept, "A", 3, "Monday", "01:30 PM", "03:30 PM", "CS205", "Data Structures Laboratory", "Dr. M. Priya", "Systems & Networking Lab"),

                    // Tuesday
                    createEntry(dept, "A", 3, "Tuesday", "09:00 AM", "10:00 AM", "CS204", "Discrete Mathematics", "Dr. Rajesh V", "CS-101"),
                    createEntry(dept, "A", 3, "Tuesday", "10:15 AM", "11:15 AM", "CS201", "Data Structures & Algorithms", "Dr. M. Priya", "CS-101"),
                    createEntry(dept, "A", 3, "Tuesday", "11:30 AM", "12:30 PM", "CS202", "Digital Principles & System Design", "Prof. R. Anand", "CS-101"),
                    createEntry(dept, "A", 3, "Tuesday", "01:30 PM", "02:30 PM", "CS203", "Object Oriented Programming in Java", "Dr. S. Suresh", "CS-101"),

                    // Wednesday
                    createEntry(dept, "A", 3, "Wednesday", "09:00 AM", "10:00 AM", "CS203", "Object Oriented Programming in Java", "Dr. S. Suresh", "CS-101"),
                    createEntry(dept, "A", 3, "Wednesday", "10:15 AM", "11:15 AM", "CS204", "Discrete Mathematics", "Dr. Rajesh V", "CS-101"),
                    createEntry(dept, "A", 3, "Wednesday", "11:30 AM", "12:30 PM", "CS201", "Data Structures & Algorithms", "Dr. M. Priya", "CS-101"),
                    createEntry(dept, "A", 3, "Wednesday", "01:30 PM", "03:30 PM", "CS205", "Data Structures Laboratory", "Dr. M. Priya", "Systems & Networking Lab"),

                    // Thursday
                    createEntry(dept, "A", 3, "Thursday", "09:00 AM", "10:00 AM", "CS202", "Digital Principles & System Design", "Prof. R. Anand", "CS-101"),
                    createEntry(dept, "A", 3, "Thursday", "10:15 AM", "11:15 AM", "CS203", "Object Oriented Programming in Java", "Dr. S. Suresh", "CS-101"),
                    createEntry(dept, "A", 3, "Thursday", "11:30 AM", "12:30 PM", "CS204", "Discrete Mathematics", "Dr. Rajesh V", "CS-101"),
                    createEntry(dept, "A", 3, "Thursday", "01:30 PM", "02:30 PM", "CS201", "Data Structures & Algorithms", "Dr. M. Priya", "CS-101"),

                    // Friday
                    createEntry(dept, "A", 3, "Friday", "09:00 AM", "10:00 AM", "CS204", "Discrete Mathematics", "Dr. Rajesh V", "CS-101"),
                    createEntry(dept, "A", 3, "Friday", "10:15 AM", "11:15 AM", "CS202", "Digital Principles & System Design", "Prof. R. Anand", "CS-101"),
                    createEntry(dept, "A", 3, "Friday", "11:30 AM", "12:30 PM", "CS201", "Data Structures & Algorithms", "Dr. M. Priya", "CS-101"),
                    createEntry(dept, "A", 3, "Friday", "01:30 PM", "03:30 PM", "CS-SEM", "Technical Seminar & Communication Skills", "Dr. M. Priya", "CS-101")
            ));
            System.out.println("[DataInitializer] Seeded weekly timetable for Section A (20 periods)");
        }

        // Section B (Sem 4)
        if (timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(dept, "B").isEmpty()) {
            timetableEntryRepository.saveAll(List.of(
                    // Monday
                    createEntry(dept, "B", 4, "Monday", "09:00 AM", "10:00 AM", "CS251", "Design & Analysis of Algorithms", "Dr. S. Suresh", "CS-102"),
                    createEntry(dept, "B", 4, "Monday", "10:15 AM", "11:15 AM", "CS252", "Computer Organization & Architecture", "Prof. R. Anand", "CS-102"),
                    createEntry(dept, "B", 4, "Monday", "11:30 AM", "12:30 PM", "CS253", "Software Engineering", "Dr. M. Priya", "CS-102"),
                    createEntry(dept, "B", 4, "Monday", "01:30 PM", "03:30 PM", "CS255", "Algorithms Laboratory", "Dr. S. Suresh", "Systems & Networking Lab"),

                    // Tuesday
                    createEntry(dept, "B", 4, "Tuesday", "09:00 AM", "10:00 AM", "CS254", "Probability & Queuing Theory", "Prof. Deepa N", "CS-102"),
                    createEntry(dept, "B", 4, "Tuesday", "10:15 AM", "11:15 AM", "CS251", "Design & Analysis of Algorithms", "Dr. S. Suresh", "CS-102"),
                    createEntry(dept, "B", 4, "Tuesday", "11:30 AM", "12:30 PM", "CS252", "Computer Organization & Architecture", "Prof. R. Anand", "CS-102"),
                    createEntry(dept, "B", 4, "Tuesday", "01:30 PM", "02:30 PM", "CS253", "Software Engineering", "Dr. M. Priya", "CS-102"),

                    // Wednesday
                    createEntry(dept, "B", 4, "Wednesday", "09:00 AM", "10:00 AM", "CS253", "Software Engineering", "Dr. M. Priya", "CS-102"),
                    createEntry(dept, "B", 4, "Wednesday", "10:15 AM", "11:15 AM", "CS254", "Probability & Queuing Theory", "Prof. Deepa N", "CS-102"),
                    createEntry(dept, "B", 4, "Wednesday", "11:30 AM", "12:30 PM", "CS251", "Design & Analysis of Algorithms", "Dr. S. Suresh", "CS-102"),
                    createEntry(dept, "B", 4, "Wednesday", "01:30 PM", "03:30 PM", "CS255", "Algorithms Laboratory", "Dr. S. Suresh", "Systems & Networking Lab"),

                    // Thursday
                    createEntry(dept, "B", 4, "Thursday", "09:00 AM", "10:00 AM", "CS252", "Computer Organization & Architecture", "Prof. R. Anand", "CS-102"),
                    createEntry(dept, "B", 4, "Thursday", "10:15 AM", "11:15 AM", "CS253", "Software Engineering", "Dr. M. Priya", "CS-102"),
                    createEntry(dept, "B", 4, "Thursday", "11:30 AM", "12:30 PM", "CS254", "Probability & Queuing Theory", "Prof. Deepa N", "CS-102"),
                    createEntry(dept, "B", 4, "Thursday", "01:30 PM", "02:30 PM", "CS251", "Design & Analysis of Algorithms", "Dr. S. Suresh", "CS-102"),

                    // Friday
                    createEntry(dept, "B", 4, "Friday", "09:00 AM", "10:00 AM", "CS254", "Probability & Queuing Theory", "Prof. Deepa N", "CS-102"),
                    createEntry(dept, "B", 4, "Friday", "10:15 AM", "11:15 AM", "CS252", "Computer Organization & Architecture", "Prof. R. Anand", "CS-102"),
                    createEntry(dept, "B", 4, "Friday", "11:30 AM", "12:30 PM", "CS251", "Design & Analysis of Algorithms", "Dr. S. Suresh", "CS-102"),
                    createEntry(dept, "B", 4, "Friday", "01:30 PM", "03:30 PM", "CS-INNOV", "Innovation & Ideation Workshop", "Dr. S. Suresh", "CS-102")
            ));
            System.out.println("[DataInitializer] Seeded weekly timetable for Section B (20 periods)");
        }

        // Section C (Sem 5)
        if (timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(dept, "C").isEmpty()) {
            timetableEntryRepository.saveAll(List.of(
                    // Monday
                    createEntry(dept, "C", 5, "Monday", "09:00 AM", "10:00 AM", "CS301", "Database Management Systems", "Dr. M. Priya", "CS-204"),
                    createEntry(dept, "C", 5, "Monday", "10:15 AM", "11:15 AM", "CS302", "Computer Networks", "Prof. R. Anand", "CS-204"),
                    createEntry(dept, "C", 5, "Monday", "11:30 AM", "12:30 PM", "CS303", "Operating Systems", "Dr. S. Suresh", "CS-204"),
                    createEntry(dept, "C", 5, "Monday", "01:30 PM", "03:30 PM", "CS306", "Cloud Computing Laboratory", "Dr. K. Ramesh", "Lab 1"),

                    // Tuesday
                    createEntry(dept, "C", 5, "Tuesday", "09:00 AM", "10:00 AM", "CS304", "Artificial Intelligence", "Dr. Rajesh V", "CS-204"),
                    createEntry(dept, "C", 5, "Tuesday", "10:15 AM", "11:15 AM", "CS305", "Theory of Computation", "Prof. Deepa N", "CS-204"),
                    createEntry(dept, "C", 5, "Tuesday", "11:30 AM", "12:30 PM", "CS301", "Database Management Systems", "Dr. M. Priya", "CS-204"),
                    createEntry(dept, "C", 5, "Tuesday", "01:30 PM", "02:30 PM", "CS302", "Computer Networks", "Prof. R. Anand", "CS-204"),

                    // Wednesday
                    createEntry(dept, "C", 5, "Wednesday", "09:00 AM", "10:00 AM", "CS303", "Operating Systems", "Dr. S. Suresh", "CS-204"),
                    createEntry(dept, "C", 5, "Wednesday", "10:15 AM", "11:15 AM", "CS304", "Artificial Intelligence", "Dr. Rajesh V", "CS-204"),
                    createEntry(dept, "C", 5, "Wednesday", "11:30 AM", "12:30 PM", "CS305", "Theory of Computation", "Prof. Deepa N", "CS-204"),
                    createEntry(dept, "C", 5, "Wednesday", "01:30 PM", "03:30 PM", "CS-PROJ", "Capstone Project & Faculty Advising", "Dr. K. Ramesh", "CS-204"),

                    // Thursday
                    createEntry(dept, "C", 5, "Thursday", "09:00 AM", "10:00 AM", "CS301", "Database Management Systems", "Dr. M. Priya", "CS-204"),
                    createEntry(dept, "C", 5, "Thursday", "10:15 AM", "11:15 AM", "CS302", "Computer Networks", "Prof. R. Anand", "CS-204"),
                    createEntry(dept, "C", 5, "Thursday", "11:30 AM", "12:30 PM", "CS304", "Artificial Intelligence", "Dr. Rajesh V", "CS-204"),
                    createEntry(dept, "C", 5, "Thursday", "01:30 PM", "02:30 PM", "CS303", "Operating Systems", "Dr. S. Suresh", "CS-204"),

                    // Friday
                    createEntry(dept, "C", 5, "Friday", "09:00 AM", "10:00 AM", "CS305", "Theory of Computation", "Prof. Deepa N", "CS-204"),
                    createEntry(dept, "C", 5, "Friday", "10:15 AM", "11:15 AM", "CS301", "Database Management Systems", "Dr. M. Priya", "CS-204"),
                    createEntry(dept, "C", 5, "Friday", "11:30 AM", "12:30 PM", "CS302", "Computer Networks", "Prof. R. Anand", "CS-204"),
                    createEntry(dept, "C", 5, "Friday", "01:30 PM", "03:30 PM", "CS-AI-LAB", "Autonomous AI & Robotics Lab", "Dr. Rajesh V", "Lab 2")
            ));
            System.out.println("[DataInitializer] Seeded weekly timetable for Section C (20 periods)");
        }

        // Section D (Sem 6)
        if (timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(dept, "D").isEmpty()) {
            timetableEntryRepository.saveAll(List.of(
                    // Monday
                    createEntry(dept, "D", 6, "Monday", "09:00 AM", "10:00 AM", "CS351", "Compiler Design", "Prof. R. Anand", "CS-301"),
                    createEntry(dept, "D", 6, "Monday", "10:15 AM", "11:15 AM", "CS352", "Distributed Systems", "Dr. K. Ramesh", "CS-301"),
                    createEntry(dept, "D", 6, "Monday", "11:30 AM", "12:30 PM", "CS353", "Web Technologies & Full Stack", "Dr. S. Suresh", "CS-301"),
                    createEntry(dept, "D", 6, "Monday", "01:30 PM", "03:30 PM", "CS355", "Full Stack Development Lab", "Prof. R. Anand", "Lab 2"),

                    // Tuesday
                    createEntry(dept, "D", 6, "Tuesday", "09:00 AM", "10:00 AM", "CS354", "Cryptography & Network Security", "Dr. M. Priya", "CS-301"),
                    createEntry(dept, "D", 6, "Tuesday", "10:15 AM", "11:15 AM", "CS351", "Compiler Design", "Prof. R. Anand", "CS-301"),
                    createEntry(dept, "D", 6, "Tuesday", "11:30 AM", "12:30 PM", "CS352", "Distributed Systems", "Dr. K. Ramesh", "CS-301"),
                    createEntry(dept, "D", 6, "Tuesday", "01:30 PM", "02:30 PM", "CS353", "Web Technologies & Full Stack", "Dr. S. Suresh", "CS-301"),

                    // Wednesday
                    createEntry(dept, "D", 6, "Wednesday", "09:00 AM", "10:00 AM", "CS353", "Web Technologies & Full Stack", "Dr. S. Suresh", "CS-301"),
                    createEntry(dept, "D", 6, "Wednesday", "10:15 AM", "11:15 AM", "CS354", "Cryptography & Network Security", "Dr. M. Priya", "CS-301"),
                    createEntry(dept, "D", 6, "Wednesday", "11:30 AM", "12:30 PM", "CS351", "Compiler Design", "Prof. R. Anand", "CS-301"),
                    createEntry(dept, "D", 6, "Wednesday", "01:30 PM", "03:30 PM", "CS-MINIPROJ", "Mini-Project Work & Review", "Prof. R. Anand", "Lab 2"),

                    // Thursday
                    createEntry(dept, "D", 6, "Thursday", "09:00 AM", "10:00 AM", "CS352", "Distributed Systems", "Dr. K. Ramesh", "CS-301"),
                    createEntry(dept, "D", 6, "Thursday", "10:15 AM", "11:15 AM", "CS353", "Web Technologies & Full Stack", "Dr. S. Suresh", "CS-301"),
                    createEntry(dept, "D", 6, "Thursday", "11:30 AM", "12:30 PM", "CS354", "Cryptography & Network Security", "Dr. M. Priya", "CS-301"),
                    createEntry(dept, "D", 6, "Thursday", "01:30 PM", "02:30 PM", "CS351", "Compiler Design", "Prof. R. Anand", "CS-301"),

                    // Friday
                    createEntry(dept, "D", 6, "Friday", "09:00 AM", "10:00 AM", "CS354", "Cryptography & Network Security", "Dr. M. Priya", "CS-301"),
                    createEntry(dept, "D", 6, "Friday", "10:15 AM", "11:15 AM", "CS352", "Distributed Systems", "Dr. K. Ramesh", "CS-301"),
                    createEntry(dept, "D", 6, "Friday", "11:30 AM", "12:30 PM", "CS351", "Compiler Design", "Prof. R. Anand", "CS-301"),
                    createEntry(dept, "D", 6, "Friday", "01:30 PM", "03:30 PM", "CS355", "Full Stack Development Lab", "Prof. R. Anand", "Lab 2")
            ));
            System.out.println("[DataInitializer] Seeded weekly timetable for Section D (20 periods)");
        }
    }

    private TimetableEntry createEntry(String dept, String sec, int semester, String day, String start, String end, String code, String name, String faculty, String room) {
        TimetableEntry e = new TimetableEntry();
        e.setDepartment(dept);
        e.setSection(sec);
        e.setSemester(semester);
        e.setDayOfWeek(day);
        e.setStartTime(start);
        e.setEndTime(end);
        e.setSubjectCode(code);
        e.setSubjectName(name);
        e.setFacultyName(faculty);
        e.setClassroom(room);
        return e;
    }

    private void seedAssignments() {
        if (assignmentRepository.count() == 0) {
            String dept = "Computer Science & Engineering";
            String sec = "C";

            assignmentRepository.saveAll(List.of(
                    new Assignment(
                            "DBMS Normalization & B+ Tree Implementation",
                            "Convert given unnormalized schema into 3NF and BCNF with lossless join decomposition. Write SQL DDL and implement a disk-based B+ tree node splitting simulation.",
                            "CS301", "Database Management Systems", dept, 5, sec, "Dr. M. Priya",
                            LocalDate.now().minusDays(4), LocalDate.now().plusDays(3), "HIGH", 100
                    ),
                    new Assignment(
                            "TCP/IP Multi-Threaded Socket Server in Python",
                            "Construct a concurrent TCP server supporting multi-client chat, heartbeat keep-alive packets, and custom binary frame encoding.",
                            "CS302", "Computer Networks", dept, 5, sec, "Prof. R. Anand",
                            LocalDate.now().minusDays(3), LocalDate.now().plusDays(6), "MEDIUM", 50
                    ),
                    new Assignment(
                            "Process Scheduling Simulator (Round Robin & Multi-Level Queue)",
                            "Build an OS scheduler simulator in C/Java comparing Round Robin (quantum=2ms) against Multi-Level Feedback Queue with starvation prevention.",
                            "CS303", "Operating Systems", dept, 5, sec, "Dr. S. Suresh",
                            LocalDate.now().minusDays(2), LocalDate.now().plusDays(8), "HIGH", 100
                    ),
                    new Assignment(
                            "Minimax with Alpha-Beta Pruning on 8-Puzzle Game Tree",
                            "Implement state space heuristic evaluation for 8-puzzle game tree search with depth-limited Alpha-Beta pruning.",
                            "CS304", "Artificial Intelligence", dept, 5, sec, "Dr. Rajesh V",
                            LocalDate.now().minusDays(1), LocalDate.now().plusDays(10), "MEDIUM", 75
                    )
            ));
            System.out.println("[DataInitializer] Seeded assignments for Section C (4 active assignments)");
        }
    }

    private void seedNotices() {
        if (announcementRepository.count() == 0) {
            Announcement a1 = new Announcement();
            a1.setTitle("Continuous Internal Assessment (CIA-2) Schedule Announced");
            a1.setContent("The second Continuous Internal Assessment (CIA-2) for CSE will commence shortly. Students must maintain a minimum of 75% attendance to be eligible without condonation.");
            a1.setPriority("URGENT");
            a1.setTargetAudience("STUDENTS");
            a1.setAuthorRole("CONTROLLER_OF_EXAMINATIONS");
            a1.setCreatedAt(LocalDateTime.now().minusDays(1));

            Announcement a2 = new Announcement();
            a2.setTitle("TCS & Infosys Campus Recruitment Drive: Registration Closes Friday");
            a2.setContent("Eligible final year and pre-final year students with CGPA 7.0 and above with zero standing arrears are invited to register on the campus placement portal before 5:00 PM this Friday.");
            a2.setPriority("HIGH");
            a2.setTargetAudience("ALL");
            a2.setAuthorRole("PLACEMENT_CELL");
            a2.setCreatedAt(LocalDateTime.now().minusDays(2));

            Announcement a3 = new Announcement();
            a3.setTitle("Smart India Hackathon (SIH 2026) Institutional Pre-Qualifier Shortlist");
            a3.setContent("Congratulations to the teams shortlisted from our institution for the SIH 2026 Grand Finale. Internal mentoring sessions begin this Wednesday in the Autonomous AI & Robotics Lab.");
            a3.setPriority("HIGH");
            a3.setTargetAudience("STUDENTS");
            a3.setAuthorRole("INNOVATION_COUNCIL");
            a3.setCreatedAt(LocalDateTime.now().minusDays(3));

            Announcement a4 = new Announcement();
            a4.setTitle("Central Library Extended Timings During Assessment Weeks");
            a4.setContent("The Central Digital Library and Reference Sections will remain open until 11:00 PM on all working days throughout the upcoming examination period.");
            a4.setPriority("NORMAL");
            a4.setTargetAudience("ALL");
            a4.setAuthorRole("CHIEF_LIBRARIAN");
            a4.setCreatedAt(LocalDateTime.now().minusDays(4));

            Announcement a5 = new Announcement();
            a5.setTitle("Autonomous AI & Robotics Workshop by Google Developer Experts");
            a5.setContent("Department of Computer Science & Engineering is organizing a 2-day hands-on workshop on Agentic AI & Generative Modeling at Vikram Sarabhai Seminar Hall.");
            a5.setPriority("HIGH");
            a5.setTargetAudience("STUDENTS");
            a5.setAuthorRole("HOD_CSE");
            a5.setCreatedAt(LocalDateTime.now().minusDays(5));

            announcementRepository.saveAll(List.of(a1, a2, a3, a4, a5));
            System.out.println("[DataInitializer] Seeded institutional notices (5 announcements)");
        }
    }

    private void seedEvents() {
        if (campusEventRepository.count() == 0) {
            CampusEvent e1 = new CampusEvent();
            e1.setTitle("InnovateX 2026: National 36-Hour Hackathon");
            e1.setCategory("Hackathon");
            e1.setDescription("Flagship inter-college hackathon focusing on Generative AI, Autonomous Systems, and Smart Campus Operations. Over 50 teams competing for prizes totaling ₹3,00,000.");
            e1.setLocation("Vikram Sarabhai Seminar Hall & AI Lab");
            e1.setEventDate(LocalDate.now().plusDays(5));
            e1.setEventTime("09:00 AM");
            e1.setOrganizer("Google Developer Student Club & CSE Association");
            e1.setRegisteredCount(142);

            CampusEvent e2 = new CampusEvent();
            e2.setTitle("Generative AI & LLM Systems Hands-On Bootcamp");
            e2.setCategory("Workshop");
            e2.setDescription("Deep dive into RAG pipelines, Vector Databases, Groq LPU inference architectures, and LangGraph multi-agent workflows.");
            e2.setLocation("Autonomous AI & Robotics Lab (Block B)");
            e2.setEventDate(LocalDate.now().plusDays(8));
            e2.setEventTime("10:00 AM");
            e2.setOrganizer("Department of CSE");
            e2.setRegisteredCount(60);

            CampusEvent e3 = new CampusEvent();
            e3.setTitle("TechKriya 2026: Annual Technical Symposium");
            e3.setCategory("Symposium");
            e3.setDescription("Annual National Symposium featuring Code Golf, Reverse Engineering, Paper Presentations, and Robo-Wars.");
            e3.setLocation("Campus Auditorium & Open Air Theatre");
            e3.setEventDate(LocalDate.now().plusDays(14));
            e3.setEventTime("09:30 AM");
            e3.setOrganizer("College Student Council");
            e3.setRegisteredCount(380);

            CampusEvent e4 = new CampusEvent();
            e4.setTitle("Campus Placement Mock Technical Interview Marathon");
            e4.setCategory("Placement");
            e4.setDescription("Simulated technical interviews conducted by alumni engineers from Microsoft, Google, and Amazon.");
            e4.setLocation("Placement Cell Interview Cabins, Block A");
            e4.setEventDate(LocalDate.now().plusDays(18));
            e4.setEventTime("01:30 PM");
            e4.setOrganizer("Career Development Centre");
            e4.setRegisteredCount(95);

            campusEventRepository.saveAll(List.of(e1, e2, e3, e4));
            System.out.println("[DataInitializer] Seeded campus events (4 major events)");
        }
    }

    private void seedStudentData() {
        String dept = "Computer Science & Engineering";

        User mentorA = userRepository.findByUsername("priya.m").orElse(null);
        User mentorB = userRepository.findByUsername("suresh.s").orElse(null);
        User mentorC = userRepository.findByUsername("ramesh.k").orElse(null);
        User mentorD = userRepository.findByUsername("anand.r").orElse(null);

        // =========================================================================
        // MENTOR 1: Dr. M. Priya -> SECTION A (Semester 3, 5 students)
        // =========================================================================
        seedSingleStudent("stud_a1", "aarav.sharma@campus.edu", "Aarav", "Sharma", "717825CS101",
                dept, "A", 2, 3, 8.82, 88, "HOSTELLER", "Block A (Boys)", "A-101", mentorA,
                List.of(
                        new CourseRecord("CS201", "Data Structures & Algorithms", 40, 36),
                        new CourseRecord("CS202", "Digital Principles & System Design", 38, 33),
                        new CourseRecord("CS203", "Object Oriented Programming in Java", 42, 37),
                        new CourseRecord("CS204", "Discrete Mathematics", 40, 35),
                        new CourseRecord("CS205", "Data Structures Laboratory", 20, 18)
                ),
                List.of(
                        new TaskRecord("Implement AVL Tree rotations in Java", "Complete left/right rotation code and balance factor recomputation", "HIGH", "TODO", LocalDate.now().plusDays(3)),
                        new TaskRecord("Digital Circuits laboratory record completion", "Document truth tables and oscilloscope timing diagrams for 4-bit counter", "MEDIUM", "IN_PROGRESS", LocalDate.now().plusDays(5)),
                        new TaskRecord("Discrete Math problem set on Pigeonhole Principle", "Submit solved proofs to classroom LMS portal", "LOW", "COMPLETED", LocalDate.now().minusDays(2))
                ),
                new LeaveRecord("MEDICAL", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Viral fever recovery and medical rest prescribed by physician", "PENDING"),
                new ODRecord("Inter-College Hackathon Prelims", LocalDate.now().plusDays(8), "PSG Tech", "Representing college coding club at regional hackathon", "APPROVED"),
                new GatePassRecord("Weekend family visit to hometown", LocalDateTime.now().plusDays(1).withHour(17).withMinute(0), LocalDateTime.now().plusDays(3).withHour(20).withMinute(0), "Coimbatore", "APPROVED")
        );

        seedSingleStudent("stud_a2", "bhavya.patel@campus.edu", "Bhavya", "Patel", "717825CS102",
                dept, "A", 2, 3, 7.95, 82, "DAY_SCHOLAR", null, null, mentorA,
                List.of(
                        new CourseRecord("CS201", "Data Structures & Algorithms", 40, 33),
                        new CourseRecord("CS202", "Digital Principles & System Design", 38, 31),
                        new CourseRecord("CS203", "Object Oriented Programming in Java", 42, 34),
                        new CourseRecord("CS204", "Discrete Mathematics", 40, 32),
                        new CourseRecord("CS205", "Data Structures Laboratory", 20, 17)
                ),
                List.of(
                        new TaskRecord("Java Multi-threading Producer-Consumer Queue", "Implement thread synchronization with wait() and notifyAll()", "HIGH", "TODO", LocalDate.now().plusDays(4)),
                        new TaskRecord("Review K-Map logic simplification exercises", "Practice 4-variable and 5-variable prime implicant tables", "MEDIUM", "COMPLETED", LocalDate.now().minusDays(1))
                ),
                null,
                new ODRecord("Robotics Club Autonomous Rover Trial", LocalDate.now().plusDays(5), "Robotics Lab", "System testing for upcoming inter-collegiate challenge", "APPROVED"),
                null
        );

        seedSingleStudent("stud_a3", "chetan.kumar@campus.edu", "Chetan", "Kumar", "717825CS103",
                dept, "A", 2, 3, 9.10, 94, "HOSTELLER", "Block A (Boys)", "A-102", mentorA,
                List.of(
                        new CourseRecord("CS201", "Data Structures & Algorithms", 40, 38),
                        new CourseRecord("CS202", "Digital Principles & System Design", 38, 36),
                        new CourseRecord("CS203", "Object Oriented Programming in Java", 42, 40),
                        new CourseRecord("CS204", "Discrete Mathematics", 40, 37),
                        new CourseRecord("CS205", "Data Structures Laboratory", 20, 19)
                ),
                List.of(
                        new TaskRecord("LeetCode Tree & Graph Challenge Series", "Solve top 30 binary tree and graph traversal problems", "HIGH", "IN_PROGRESS", LocalDate.now().plusDays(6)),
                        new TaskRecord("Prepare study guide for Discrete Math CIA-2", "Graph theory, Eulerian circuits, and Planar graphs", "MEDIUM", "TODO", LocalDate.now().plusDays(8))
                ),
                null, null,
                new GatePassRecord("Technical library reference visit", LocalDateTime.now().plusDays(2).withHour(14).withMinute(0), LocalDateTime.now().plusDays(2).withHour(19).withMinute(0), "Central Town Library", "APPROVED")
        );

        seedSingleStudent("stud_a4", "divya.ramesh@campus.edu", "Divya", "Ramesh", "717825CS104",
                dept, "A", 2, 3, 8.40, 78, "DAY_SCHOLAR", null, null, mentorA,
                List.of(
                        new CourseRecord("CS201", "Data Structures & Algorithms", 40, 31),
                        new CourseRecord("CS202", "Digital Principles & System Design", 38, 30),
                        new CourseRecord("CS203", "Object Oriented Programming in Java", 42, 33),
                        new CourseRecord("CS204", "Discrete Mathematics", 40, 31),
                        new CourseRecord("CS205", "Data Structures Laboratory", 20, 16)
                ),
                List.of(
                        new TaskRecord("Object Oriented Programming Java Lab 5", "Complete custom exception handling and file I/O operations", "HIGH", "TODO", LocalDate.now().plusDays(2)),
                        new TaskRecord("Submit draft report on Synchronous Sequential Counters", "Include circuit schematics and state diagrams", "MEDIUM", "IN_PROGRESS", LocalDate.now().plusDays(4))
                ),
                new LeaveRecord("PERSONAL", LocalDate.now().plusDays(3), LocalDate.now().plusDays(3), "Attending family function in Madurai", "APPROVED"),
                null, null
        );

        seedSingleStudent("stud_a5", "eswar.prasad@campus.edu", "Eswar", "Prasad", "717825CS105",
                dept, "A", 2, 3, 6.90, 72, "DAY_SCHOLAR", null, null, mentorA,
                List.of(
                        new CourseRecord("CS201", "Data Structures & Algorithms", 40, 28),
                        new CourseRecord("CS202", "Digital Principles & System Design", 38, 27),
                        new CourseRecord("CS203", "Object Oriented Programming in Java", 42, 30),
                        new CourseRecord("CS204", "Discrete Mathematics", 40, 29),
                        new CourseRecord("CS205", "Data Structures Laboratory", 20, 15)
                ),
                List.of(
                        new TaskRecord("Attendance recovery consultation with Mentor Dr. Priya", "Discuss makeup assignments and attendance remediation roadmap", "HIGH", "TODO", LocalDate.now().plusDays(1)),
                        new TaskRecord("Submit overdue Binary Search Tree lab assignment", "Complete BST insert, delete, and search implementation", "HIGH", "IN_PROGRESS", LocalDate.now().plusDays(3))
                ),
                new LeaveRecord("MEDICAL", LocalDate.now().minusDays(5), LocalDate.now().minusDays(3), "Typhoid recovery - medical fitness certificate attached", "APPROVED"),
                null, null
        );

        // =========================================================================
        // MENTOR 2: Dr. S. Suresh -> SECTION B (Semester 4, 5 students)
        // =========================================================================
        seedSingleStudent("stud_b1", "farhan.khan@campus.edu", "Farhan", "Khan", "717825CS201",
                dept, "B", 2, 4, 8.25, 86, "HOSTELLER", "Block A (Boys)", "A-201", mentorB,
                List.of(
                        new CourseRecord("CS251", "Design & Analysis of Algorithms", 40, 35),
                        new CourseRecord("CS252", "Computer Organization & Architecture", 38, 32),
                        new CourseRecord("CS253", "Software Engineering", 40, 35),
                        new CourseRecord("CS254", "Probability & Queuing Theory", 42, 36),
                        new CourseRecord("CS255", "Algorithms Laboratory", 20, 18)
                ),
                List.of(
                        new TaskRecord("Dynamic Programming 0/1 Knapsack & LCS Implementation", "Benchmark memoization versus bottom-up tabulation in C++", "HIGH", "IN_PROGRESS", LocalDate.now().plusDays(3)),
                        new TaskRecord("Software Requirements Specification (SRS) Document", "Draft IEEE 830 compliant SRS for Student Feedback System", "MEDIUM", "TODO", LocalDate.now().plusDays(6))
                ),
                null, null,
                new GatePassRecord("Weekend outing with project team", LocalDateTime.now().plusDays(2).withHour(9).withMinute(0), LocalDateTime.now().plusDays(2).withHour(20).withMinute(0), "Brookefields Mall", "APPROVED")
        );

        seedSingleStudent("stud_b2", "gayathri.s@campus.edu", "Gayathri", "S", "717825CS202",
                dept, "B", 2, 4, 9.35, 96, "HOSTELLER", "Block B (Girls)", "B-201", mentorB,
                List.of(
                        new CourseRecord("CS251", "Design & Analysis of Algorithms", 40, 39),
                        new CourseRecord("CS252", "Computer Organization & Architecture", 38, 37),
                        new CourseRecord("CS253", "Software Engineering", 40, 38),
                        new CourseRecord("CS254", "Probability & Queuing Theory", 42, 40),
                        new CourseRecord("CS255", "Algorithms Laboratory", 20, 19)
                ),
                List.of(
                        new TaskRecord("Cache Architecture Simulator in Python", "Simulate direct-mapped, 2-way, and fully associative cache hit/miss rates", "HIGH", "COMPLETED", LocalDate.now().minusDays(1)),
                        new TaskRecord("Probability exercise set on Poisson distribution", "Solve 15 problem models for M/M/1 queuing systems", "MEDIUM", "TODO", LocalDate.now().plusDays(5))
                ),
                null,
                new ODRecord("National Coding Olympiad Regional Round", LocalDate.now().plusDays(6), "NIT Trichy", "Representing college coding team in ACM contest", "APPROVED"),
                null
        );

        seedSingleStudent("stud_b3", "harish.verma@campus.edu", "Harish", "Verma", "717825CS203",
                dept, "B", 2, 4, 7.60, 80, "DAY_SCHOLAR", null, null, mentorB,
                List.of(
                        new CourseRecord("CS251", "Design & Analysis of Algorithms", 40, 32),
                        new CourseRecord("CS252", "Computer Organization & Architecture", 38, 30),
                        new CourseRecord("CS253", "Software Engineering", 40, 32),
                        new CourseRecord("CS254", "Probability & Queuing Theory", 42, 33),
                        new CourseRecord("CS255", "Algorithms Laboratory", 20, 16)
                ),
                List.of(
                        new TaskRecord("Revise Greedy algorithms: Kruskal and Prim MST", "Implement disjoint-set union find with path compression", "HIGH", "TODO", LocalDate.now().plusDays(4)),
                        new TaskRecord("Software Engineering Sprint Retrospective Report", "Complete sprint review metrics for project sprint 2", "MEDIUM", "IN_PROGRESS", LocalDate.now().plusDays(7))
                ),
                new LeaveRecord("PERSONAL", LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), "Attending family religious ceremony in native town", "PENDING"),
                null, null
        );

        seedSingleStudent("stud_b4", "ishwarya.m@campus.edu", "Ishwarya", "M", "717825CS204",
                dept, "B", 2, 4, 8.70, 90, "DAY_SCHOLAR", null, null, mentorB,
                List.of(
                        new CourseRecord("CS251", "Design & Analysis of Algorithms", 40, 36),
                        new CourseRecord("CS252", "Computer Organization & Architecture", 38, 34),
                        new CourseRecord("CS253", "Software Engineering", 40, 36),
                        new CourseRecord("CS254", "Probability & Queuing Theory", 42, 38),
                        new CourseRecord("CS255", "Algorithms Laboratory", 20, 18)
                ),
                List.of(
                        new TaskRecord("Algorithms Lab: Bellman-Ford Shortest Path", "Test with negative edge weight graphs and cycle detection", "HIGH", "TODO", LocalDate.now().plusDays(2)),
                        new TaskRecord("Presentation on Microprogrammed Control Unit", "Create 10 slides with architecture block diagram", "MEDIUM", "COMPLETED", LocalDate.now().minusDays(2))
                ),
                null,
                new ODRecord("Women in Tech Summit 2026", LocalDate.now().plusDays(7), "IIT Madras Research Park", "Attending student leadership track and technical workshops", "APPROVED"),
                null
        );

        seedSingleStudent("stud_b5", "jayanth.r@campus.edu", "Jayanth", "R", "717825CS205",
                dept, "B", 2, 4, 7.15, 74, "HOSTELLER", "Block A (Boys)", "A-202", mentorB,
                List.of(
                        new CourseRecord("CS251", "Design & Analysis of Algorithms", 40, 29),
                        new CourseRecord("CS252", "Computer Organization & Architecture", 38, 28),
                        new CourseRecord("CS253", "Software Engineering", 40, 30),
                        new CourseRecord("CS254", "Probability & Queuing Theory", 42, 31),
                        new CourseRecord("CS255", "Algorithms Laboratory", 20, 15)
                ),
                List.of(
                        new TaskRecord("Attendance remediation meeting with Dr. Suresh", "Review attendance logs and receive makeup task list", "HIGH", "TODO", LocalDate.now().plusDays(1)),
                        new TaskRecord("Divide & Conquer Master Theorem Recurrences", "Solve 10 recurrence relation practice questions", "MEDIUM", "IN_PROGRESS", LocalDate.now().plusDays(3))
                ),
                null, null,
                new GatePassRecord("Medical checkup at city hospital", LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), LocalDateTime.now().plusDays(1).withHour(16).withMinute(0), "Ganga Hospital", "APPROVED")
        );

        // =========================================================================
        // MENTOR 3: Dr. K. Ramesh -> SECTION C (Semester 5, 5 students)
        // =========================================================================
        // 1. vasan (Vasanth S - Roll: 717824P361)
        User vasan = userRepository.findByUsername("vasan").orElse(null);
        if (vasan != null) {
            if (attendanceRecordRepository.findByUserOrderByCourseCodeAsc(vasan).isEmpty()) {
                attendanceRecordRepository.saveAll(List.of(
                        new AttendanceRecord(vasan, "CS301", "Database Management Systems", 40, 36),
                        new AttendanceRecord(vasan, "CS302", "Computer Networks", 40, 34),
                        new AttendanceRecord(vasan, "CS303", "Operating Systems", 40, 32),
                        new AttendanceRecord(vasan, "CS304", "Artificial Intelligence", 40, 38),
                        new AttendanceRecord(vasan, "CS305", "Theory of Computation", 40, 31),
                        new AttendanceRecord(vasan, "CS306", "Cloud Computing Laboratory", 20, 19)
                ));
            }
            if (studentTaskRepository.findByUserOrderByCreatedAtDesc(vasan).isEmpty()) {
                studentTaskRepository.saveAll(List.of(
                        new StudentTask(vasan, "Complete DBMS Normalization assignment submission", "Solve 3NF schema decomposition problems and upload PDF to LMS.", "HIGH", "TODO", LocalDate.now().plusDays(3)),
                        new StudentTask(vasan, "Prepare slide deck for Operating Systems semaphore presentation", "Include Dining Philosophers solution diagram and code snippets.", "MEDIUM", "IN_PROGRESS", LocalDate.now().plusDays(5)),
                        new StudentTask(vasan, "Register team for InnovateX National Hackathon", "Submitted 4-member squad details and abstract on Smart Campus Operations.", "HIGH", "COMPLETED", LocalDate.now().minusDays(1)),
                        new StudentTask(vasan, "Revise Computer Networks Module 3 (BGP & OSPF routing)", "Prepare summary flashcards for CIA-2 assessment.", "MEDIUM", "TODO", LocalDate.now().plusDays(7))
                ));
            }
            if (leaveRequestRepository.findByStudentOrderByCreatedAtDesc(vasan).isEmpty()) {
                LeaveRequest lr = new LeaveRequest();
                lr.setStudent(vasan);
                lr.setLeaveType("MEDICAL");
                lr.setFromDate(LocalDate.now().minusDays(4));
                lr.setToDate(LocalDate.now().minusDays(3));
                lr.setReason("Severe migraine and doctor recommended rest");
                lr.setStatus("APPROVED");
                lr.setAssignedToUser(mentorC);
                lr.setAssignedToRole("FACULTY");
                lr.setResolutionNotes("Approved on medical grounds.");
                lr.setResolvedAt(LocalDateTime.now().minusDays(3));
                leaveRequestRepository.save(lr);
            }
            if (gatePassRequestRepository.findByStudentOrderByCreatedAtDesc(vasan).isEmpty()) {
                GatePassRequest gp = new GatePassRequest();
                gp.setStudent(vasan);
                gp.setPurpose("Weekend visit to native residence");
                gp.setOutDateTime(LocalDateTime.now().plusDays(2).withHour(18).withMinute(0));
                gp.setExpectedReturnDateTime(LocalDateTime.now().plusDays(4).withHour(20).withMinute(0));
                gp.setDestination("Salem");
                gp.setStatus("APPROVED");
                gp.setApprovedBy(mentorC);
                gatePassRequestRepository.save(gp);
            }
            ensureWelcomeNotification(vasan, "Computer Science & Engineering", "C");
        }

        seedSingleStudent("stud_c2", "karthik.n@campus.edu", "Karthik", "N", "717824CS302",
                dept, "C", 3, 5, 8.92, 91, "DAY_SCHOLAR", null, null, mentorC,
                List.of(
                        new CourseRecord("CS301", "Database Management Systems", 40, 37),
                        new CourseRecord("CS302", "Computer Networks", 40, 36),
                        new CourseRecord("CS303", "Operating Systems", 40, 36),
                        new CourseRecord("CS304", "Artificial Intelligence", 40, 37),
                        new CourseRecord("CS305", "Theory of Computation", 40, 36),
                        new CourseRecord("CS306", "Cloud Computing Laboratory", 20, 19)
                ),
                List.of(
                        new TaskRecord("Docker multi-stage build container for Spring Boot backend", "Optimize final alpine container image footprint under 150MB", "HIGH", "IN_PROGRESS", LocalDate.now().plusDays(3)),
                        new TaskRecord("Solve DBMS SQL optimization problems using EXPLAIN ANALYZE", "Identify sequential table scans and create covering indexes", "MEDIUM", "TODO", LocalDate.now().plusDays(5))
                ),
                null,
                new ODRecord("Google Cloud Community Day Hackathon", LocalDate.now().plusDays(5), "Chennai Trade Centre", "Hackathon finals with 4-member team", "APPROVED"),
                null
        );

        seedSingleStudent("stud_c3", "lavanya.sundar@campus.edu", "Lavanya", "Sundar", "717824CS303",
                dept, "C", 3, 5, 9.45, 95, "HOSTELLER", "Block B (Girls)", "B-201", mentorC,
                List.of(
                        new CourseRecord("CS301", "Database Management Systems", 40, 39),
                        new CourseRecord("CS302", "Computer Networks", 40, 38),
                        new CourseRecord("CS303", "Operating Systems", 40, 38),
                        new CourseRecord("CS304", "Artificial Intelligence", 40, 39),
                        new CourseRecord("CS305", "Theory of Computation", 40, 37),
                        new CourseRecord("CS306", "Cloud Computing Laboratory", 20, 20)
                ),
                List.of(
                        new TaskRecord("Research survey paper draft on Multi-Agent LLM Orchestration", "Submit first revision to Dr. Ramesh for academic review", "HIGH", "IN_PROGRESS", LocalDate.now().plusDays(7)),
                        new TaskRecord("Implement A* search with Manhattan distance on 15-puzzle", "Benchmark node expansion count against BFS search", "MEDIUM", "COMPLETED", LocalDate.now().minusDays(1))
                ),
                null, null,
                new GatePassRecord("Home visit for festival celebration", LocalDateTime.now().plusDays(2).withHour(18).withMinute(0), LocalDateTime.now().plusDays(5).withHour(8).withMinute(0), "Tiruchirappalli", "APPROVED")
        );

        seedSingleStudent("stud_c4", "manoj.vignesh@campus.edu", "Manoj", "Vignesh", "717824CS304",
                dept, "C", 3, 5, 7.80, 81, "DAY_SCHOLAR", null, null, mentorC,
                List.of(
                        new CourseRecord("CS301", "Database Management Systems", 40, 32),
                        new CourseRecord("CS302", "Computer Networks", 40, 32),
                        new CourseRecord("CS303", "Operating Systems", 40, 33),
                        new CourseRecord("CS304", "Artificial Intelligence", 40, 32),
                        new CourseRecord("CS305", "Theory of Computation", 40, 32),
                        new CourseRecord("CS306", "Cloud Computing Laboratory", 20, 17)
                ),
                List.of(
                        new TaskRecord("Operating Systems virtual memory page replacement simulator", "Compare FIFO, LRU, and Optimal page replacement hit rates", "HIGH", "TODO", LocalDate.now().plusDays(4)),
                        new TaskRecord("Computer Networks Wireshark packet capture analysis", "Inspect 3-way TCP handshake and TLS Client Hello packets", "MEDIUM", "IN_PROGRESS", LocalDate.now().plusDays(6))
                ),
                new LeaveRecord("MEDICAL", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Sprained ankle during sports practice - doctor advice rest", "APPROVED"),
                null, null
        );

        seedSingleStudent("stud_c5", "nandhini.k@campus.edu", "Nandhini", "K", "717824CS305",
                dept, "C", 3, 5, 8.50, 89, "HOSTELLER", "Block B (Girls)", "B-202", mentorC,
                List.of(
                        new CourseRecord("CS301", "Database Management Systems", 40, 36),
                        new CourseRecord("CS302", "Computer Networks", 40, 35),
                        new CourseRecord("CS303", "Operating Systems", 40, 35),
                        new CourseRecord("CS304", "Artificial Intelligence", 40, 36),
                        new CourseRecord("CS305", "Theory of Computation", 40, 35),
                        new CourseRecord("CS306", "Cloud Computing Laboratory", 20, 18)
                ),
                List.of(
                        new TaskRecord("Theory of Computation NFA to DFA converter", "Write transition table subset construction matrix algorithm in Java", "HIGH", "TODO", LocalDate.now().plusDays(5)),
                        new TaskRecord("Presentation slides on Kubernetes Ingress Controllers", "Deploy sample NGINX ingress with TLS certificate automation", "MEDIUM", "COMPLETED", LocalDate.now().minusDays(3))
                ),
                null, null,
                new GatePassRecord("Technical bookstore and stationery visit", LocalDateTime.now().plusDays(2).withHour(10).withMinute(0), LocalDateTime.now().plusDays(2).withHour(18).withMinute(0), "Town Hall Road", "APPROVED")
        );

        // =========================================================================
        // MENTOR 4: Prof. R. Anand -> SECTION D (Semester 6, 5 students)
        // =========================================================================
        seedSingleStudent("stud_d1", "omkar.shinde@campus.edu", "Omkar", "Shinde", "717824CS401",
                dept, "D", 3, 6, 8.10, 84, "HOSTELLER", "Block A (Boys)", "A-301", mentorD,
                List.of(
                        new CourseRecord("CS351", "Compiler Design", 40, 34),
                        new CourseRecord("CS352", "Distributed Systems", 40, 33),
                        new CourseRecord("CS353", "Web Technologies & Full Stack", 40, 34),
                        new CourseRecord("CS354", "Cryptography & Network Security", 40, 33),
                        new CourseRecord("CS355", "Full Stack Development Lab", 20, 17)
                ),
                List.of(
                        new TaskRecord("Implement Lexical Analyzer using Lex/Flex for C grammar", "Generate token stream for keywords, identifiers, and numeric literals", "HIGH", "IN_PROGRESS", LocalDate.now().plusDays(3)),
                        new TaskRecord("Set up distributed Redis cluster with master-replica failover", "Test node failover behavior during simulated network partition", "MEDIUM", "TODO", LocalDate.now().plusDays(6))
                ),
                null, null,
                new GatePassRecord("Weekend hackathon prep at off-campus innovation lab", LocalDateTime.now().plusDays(1).withHour(8).withMinute(0), LocalDateTime.now().plusDays(2).withHour(19).withMinute(0), "Tidel Park Coimbatore", "APPROVED")
        );

        seedSingleStudent("stud_d2", "pooja.krishnan@campus.edu", "Pooja", "Krishnan", "717824CS402",
                dept, "D", 3, 6, 9.05, 92, "HOSTELLER", "Block B (Girls)", "B-302", mentorD,
                List.of(
                        new CourseRecord("CS351", "Compiler Design", 40, 37),
                        new CourseRecord("CS352", "Distributed Systems", 40, 37),
                        new CourseRecord("CS353", "Web Technologies & Full Stack", 40, 37),
                        new CourseRecord("CS354", "Cryptography & Network Security", 40, 37),
                        new CourseRecord("CS355", "Full Stack Development Lab", 20, 19)
                ),
                List.of(
                        new TaskRecord("Construct Recursive Descent Parser for arithmetic expressions", "Handle operator precedence, associativity, and syntax error recovery", "HIGH", "COMPLETED", LocalDate.now().minusDays(1)),
                        new TaskRecord("RSA 2048-bit Public Key Encryption with OAEP padding", "Generate Miller-Rabin verified primes and implement key pair generation", "HIGH", "IN_PROGRESS", LocalDate.now().plusDays(4))
                ),
                null,
                new ODRecord("ACM-ICPC Asia-Amritapuri Preliminary Round", LocalDate.now().plusDays(6), "Amrita Vishwa Vidyapeetham", "Contestant in ACM-ICPC competitive programming finals", "APPROVED"),
                null
        );

        seedSingleStudent("stud_d3", "raghav.menon@campus.edu", "Raghav", "Menon", "717824CS403",
                dept, "D", 3, 6, 7.45, 76, "DAY_SCHOLAR", null, null, mentorD,
                List.of(
                        new CourseRecord("CS351", "Compiler Design", 40, 30),
                        new CourseRecord("CS352", "Distributed Systems", 40, 31),
                        new CourseRecord("CS353", "Web Technologies & Full Stack", 40, 31),
                        new CourseRecord("CS354", "Cryptography & Network Security", 40, 30),
                        new CourseRecord("CS355", "Full Stack Development Lab", 20, 15)
                ),
                List.of(
                        new TaskRecord("Full Stack Web Application Mini-Project Sprint 1", "React UI with Spring Boot REST endpoints and JWT auth integration", "HIGH", "TODO", LocalDate.now().plusDays(2)),
                        new TaskRecord("Lamport Timestamps and Vector Clocks study questions", "Solve causal ordering examples for distributed event diagrams", "MEDIUM", "IN_PROGRESS", LocalDate.now().plusDays(5))
                ),
                new LeaveRecord("FAMILY", LocalDate.now().plusDays(3), LocalDate.now().plusDays(4), "Elder sister wedding ceremony in Palakkad", "APPROVED"),
                null, null
        );

        seedSingleStudent("stud_d4", "sneha.reddy@campus.edu", "Sneha", "Reddy", "717824CS404",
                dept, "D", 3, 6, 8.88, 93, "DAY_SCHOLAR", null, null, mentorD,
                List.of(
                        new CourseRecord("CS351", "Compiler Design", 40, 37),
                        new CourseRecord("CS352", "Distributed Systems", 40, 37),
                        new CourseRecord("CS353", "Web Technologies & Full Stack", 40, 38),
                        new CourseRecord("CS354", "Cryptography & Network Security", 40, 37),
                        new CourseRecord("CS355", "Full Stack Development Lab", 20, 19)
                ),
                List.of(
                        new TaskRecord("WebSockets Real-Time Collaborative Canvas App", "Canvas event broadcasting via Spring STOMP broker and SockJS", "HIGH", "IN_PROGRESS", LocalDate.now().plusDays(4)),
                        new TaskRecord("Differential Cryptanalysis Case Study on DES S-Boxes", "Write comprehensive comparative vulnerability paper", "MEDIUM", "TODO", LocalDate.now().plusDays(8))
                ),
                null,
                new ODRecord("Grace Hopper Celebration India 2026", LocalDate.now().plusDays(8), "BIEC Bengaluru", "Attending student scholar program and research presentation", "APPROVED"),
                null
        );

        seedSingleStudent("stud_d5", "tarun.kumar@campus.edu", "Tarun", "Kumar", "717824CS405",
                dept, "D", 3, 6, 7.75, 79, "HOSTELLER", "Block A (Boys)", "A-303", mentorD,
                List.of(
                        new CourseRecord("CS351", "Compiler Design", 40, 31),
                        new CourseRecord("CS352", "Distributed Systems", 40, 32),
                        new CourseRecord("CS353", "Web Technologies & Full Stack", 40, 32),
                        new CourseRecord("CS354", "Cryptography & Network Security", 40, 31),
                        new CourseRecord("CS355", "Full Stack Development Lab", 20, 16)
                ),
                List.of(
                        new TaskRecord("Intermediate Three-Address Code Quadruple Generation", "Compiler design laboratory assignment 3 implementation", "HIGH", "TODO", LocalDate.now().plusDays(3)),
                        new TaskRecord("Raft Leader Election and Heartbeat Simulation in Go", "Model state transitions from Follower to Candidate and Leader", "MEDIUM", "IN_PROGRESS", LocalDate.now().plusDays(7))
                ),
                null, null,
                new GatePassRecord("Project hardware components purchase", LocalDateTime.now().plusDays(2).withHour(13).withMinute(0), LocalDateTime.now().plusDays(2).withHour(19).withMinute(0), "Gandhipuram Market", "APPROVED")
        );

        System.out.println("[DataInitializer] Seeded 20 distinct students across 4 mentor sections with complete individual profiles, attendance, tasks, and requests.");
    }

    // Helper records for seeding
    private record CourseRecord(String code, String name, int total, int attended) {}
    private record TaskRecord(String title, String desc, String priority, String status, LocalDate due) {}
    private record LeaveRecord(String type, LocalDate from, LocalDate to, String reason, String status) {}
    private record ODRecord(String event, LocalDate date, String loc, String reason, String status) {}
    private record GatePassRecord(String purpose, LocalDateTime out, LocalDateTime ret, String destination, String status) {}

    private void seedSingleStudent(String username, String email, String first, String last, String roll,
                                   String dept, String sec, int year, int sem, double cgpa, int attRate,
                                   String studentType, String hostelBlock, String roomNumber, User mentor,
                                   List<CourseRecord> courses, List<TaskRecord> tasks,
                                   LeaveRecord leave, ODRecord od, GatePassRecord gp) {

        // 1. User account (password: student123)
        User student = userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User(username, email, passwordEncoder.encode("student123"), Role.STUDENT, first, last);
            return userRepository.save(u);
        });

        // 2. Student Profile
        if (!studentProfileRepository.existsByRollNumber(roll)) {
            StudentProfile profile = new StudentProfile();
            profile.setUser(student);
            profile.setRollNumber(roll);
            profile.setDepartment(dept);
            profile.setSection(sec);
            profile.setYear(year);
            profile.setSemester(sem);
            profile.setCgpa(cgpa);
            profile.setAttendanceRate(attRate);
            profile.setStudentType(studentType != null ? studentType : "DAY_SCHOLAR");
            profile.setHostelBlock(hostelBlock);
            profile.setRoomNumber(roomNumber);
            studentProfileRepository.save(profile);
        }

        // 3. Subject-wise attendance records
        if (attendanceRecordRepository.findByUserOrderByCourseCodeAsc(student).isEmpty() && courses != null) {
            List<AttendanceRecord> recs = new ArrayList<>();
            for (CourseRecord cr : courses) {
                recs.add(new AttendanceRecord(student, cr.code(), cr.name(), cr.total(), cr.attended()));
            }
            attendanceRecordRepository.saveAll(recs);
        }

        // 4. Student Tasks
        if (studentTaskRepository.findByUserOrderByCreatedAtDesc(student).isEmpty() && tasks != null) {
            List<StudentTask> tList = new ArrayList<>();
            for (TaskRecord tr : tasks) {
                tList.add(new StudentTask(student, tr.title(), tr.desc(), tr.priority(), tr.status(), tr.due()));
            }
            studentTaskRepository.saveAll(tList);
        }

        // 5. Leave Request
        if (leave != null && leaveRequestRepository.findByStudentOrderByCreatedAtDesc(student).isEmpty()) {
            LeaveRequest lr = new LeaveRequest();
            lr.setStudent(student);
            lr.setLeaveType(leave.type());
            lr.setFromDate(leave.from());
            lr.setToDate(leave.to());
            lr.setReason(leave.reason());
            lr.setStatus(leave.status());
            lr.setAssignedToUser(mentor);
            lr.setAssignedToRole("FACULTY");
            if ("APPROVED".equalsIgnoreCase(leave.status())) {
                lr.setResolutionNotes("Approved by class mentor.");
                lr.setResolvedAt(LocalDateTime.now().minusDays(1));
            }
            leaveRequestRepository.save(lr);
        }

        // 6. OD Request
        if (od != null && odRequestRepository.findByStudentOrderByCreatedAtDesc(student).isEmpty()) {
            ODRequest or = new ODRequest();
            or.setStudent(student);
            or.setEventName(od.event());
            or.setEventDate(od.date());
            or.setLocation(od.loc());
            or.setReason(od.reason());
            or.setStatus(od.status());
            or.setAssignedToUser(mentor);
            or.setAssignedToRole("FACULTY");
            if ("APPROVED".equalsIgnoreCase(od.status())) {
                or.setResolutionNotes("Approved for collegiate representation.");
                or.setResolvedAt(LocalDateTime.now().minusDays(1));
            }
            odRequestRepository.save(or);
        }

        // 7. Gate Pass Request (Hostellers)
        if (gp != null && gatePassRequestRepository.findByStudentOrderByCreatedAtDesc(student).isEmpty()) {
            GatePassRequest gpr = new GatePassRequest();
            gpr.setStudent(student);
            gpr.setPurpose(gp.purpose());
            gpr.setOutDateTime(gp.out());
            gpr.setExpectedReturnDateTime(gp.ret());
            gpr.setDestination(gp.destination());
            gpr.setStatus(gp.status());
            gpr.setApprovedBy(mentor);
            gatePassRequestRepository.save(gpr);
        }

        // 8. Notifications
        ensureWelcomeNotification(student, dept, sec);
    }

    private void ensureWelcomeNotification(User student, String dept, String sec) {
        if (notificationRepository.findByUserOrderByCreatedAtDesc(student).isEmpty()) {
            Notification n1 = new Notification(
                    student,
                    "Welcome to AgentX Campus Portal",
                    "You are enrolled in " + dept + " Section " + sec + ". Access your timetable, attendance, tasks, and requests.",
                    "CAMPUS_ALERT"
            );
            n1.setMatchedBecause("Section: " + dept + " Sec " + sec);

            Notification n2 = new Notification(
                    student,
                    "Continuous Internal Assessment (CIA-2) Approaching",
                    "Ensure your subject attendance remains above 75% to appear for exams without condonation.",
                    "TIMETABLE_UPDATE"
            );
            n2.setMatchedBecause("Section: " + dept + " Sec " + sec);

            notificationRepository.saveAll(List.of(n1, n2));
        }
    }

    private void seedKnowledgeDocuments() {
        // Upsert canonical institutional documents to ensure semantic RAG citations are always up-to-date
        campusDocumentRepository.deleteAll();

        CampusDocument doc1 = new CampusDocument(
                "Autonomous Academic Regulations 2026 (Attendance, Grading, Credit System)",
                "REGULATION",
                "Official statutory regulations governing undergraduate B.Tech/B.E. degree programs.",
                "1. ATTENDANCE REQUIREMENTS & CONDONATION:\n"
                        + "A candidate who has fulfilled attendance by securing not less than 75% of classes in each subject shall be eligible to appear for End Semester Examinations without restriction.\n\n"
                        + "Condonation of shortage of attendance between 65% and 74% (inclusive) may be granted by the Academic Council solely on valid medical grounds or approved collegiate representation, supported by an official medical certificate submitted to the department office within 3 working days. A mandatory condonation fee of ₹750 per subject must be remitted to the academic finance counter upon approval.\n\n"
                        + "Candidates who secure less than 65% attendance in any course are strictly NOT permitted to write the end semester examination under any circumstances, receive zero condonation, and must repeat the course in a subsequent academic semester.\n\n"
                        + "2. GRADING SYSTEM & CGPA CALCULATION:\n"
                        + "Grade O (Outstanding): 90-100 marks, Grade Point 10\n"
                        + "Grade A+ (Excellent): 80-89 marks, Grade Point 9\n"
                        + "Grade A (Very Good): 70-79 marks, Grade Point 8\n"
                        + "Grade B+ (Good): 60-69 marks, Grade Point 7\n"
                        + "Grade B (Above Average): 50-59 marks, Grade Point 6\n"
                        + "Grade RA (Re-Appearance): Below 50 marks, Grade Point 0\n\n"
                        + "Cumulative Grade Point Average (CGPA) is computed as sum of (Credits * Grade Points) divided by total credits earned across all semesters.",
                "All Departments", "PDF", "2026.1", "Dean of Academic Affairs"
        );

        CampusDocument doc2 = new CampusDocument(
                "Continuous Internal Assessment (CIA) & Evaluation Scheme",
                "EXAM_RULES",
                "Standard operating guidelines for CIA-1, CIA-2, lab continuous evaluation, and final weights.",
                "1. INTERNAL ASSESSMENT COMPOSITION (Total 40 Marks):\n"
                        + "• CIA-1 Written Examination: 15 Marks (Conducted after 5 weeks of instruction)\n"
                        + "• CIA-2 Written Examination: 15 Marks (Conducted after 10 weeks of instruction)\n"
                        + "• Practical Assignments, Case Studies, and Quizzes: 10 Marks\n\n"
                        + "2. END SEMESTER UNIVERSITY EXAMINATION (Total 60 Marks):\n"
                        + "Written university examination of 3 hours duration covering all five units of the curriculum.\n\n"
                        + "To pass a course, a student must secure at least 45% in the end semester examination and a minimum aggregate of 50% combined across internal and external examinations.",
                "Controller of Examinations", "PDF", "1.4", "Controller of Examinations"
        );

        CampusDocument doc3 = new CampusDocument(
                "Hostel Code of Conduct, Gate Pass Guidelines & Campus Rules",
                "CAMPUS_GUIDE",
                "Regulations governing student residential hostel blocks, curfew, visitor hours, and gate pass approvals.",
                "1. HOSTEL CURFEW & TIMINGS:\n"
                        + "All residential students must return to their respective hostel blocks by 08:30 PM on weekdays and 09:00 PM on weekends.\n\n"
                        + "Biometric attendance is registered at the hostel lobby between 08:30 PM and 09:00 PM nightly.\n\n"
                        + "2. OUTING & GATE PASS PROCEDURES:\n"
                        + "Saturday Daytime Local Outing: Hostellers are permitted local daytime outing on Saturdays between 09:00 AM and 08:30 PM without parents' call or telephone confirmation, provided they register their digital outpass at the security biometric gate and return to the hostel before the 08:30 PM curfew.\n\n"
                        + "Overnight & Extended Leave Verification: For overnight stays, leaves past 08:30 PM, or weekend home visits, hostellers CANNOT leave without parents' call. The residential warden must receive explicit verbal telephonic verification from the registered parent/guardian prior to authorizing gate departure.\n\n"
                        + "3. CAMPUS AMENITIES & WI-FI:\n"
                        + "High-speed campus Wi-Fi 6 ('Campus-AirNet') is available across academic blocks and hostels 24/7. Peer-to-peer torrent traffic is strictly throttled.",
                "Hostel Administration", "PDF", "2.0", "Chief Residential Warden"
        );

        CampusDocument doc4 = new CampusDocument(
                "Campus Placement Eligibility Criteria & Training Policy",
                "POLICY",
                "Guidelines for campus recruitments, Tier-1, Tier-2 CTC brackets, and dream company policies.",
                "1. PLACEMENT ELIGIBILITY:\n"
                        + "Students with CGPA 7.0 or higher throughout their academic tenure without standing backlogs are eligible for Tier-1 recruitment drives (CTC > 10 LPA).\n\n"
                        + "Students with CGPA 6.0 and up to 1 standing backlog may participate in Tier-2 and core engineering campus interviews.\n\n"
                        + "2. ONE-STUDENT ONE-OFFER RULE:\n"
                        + "A candidate who receives an offer with CTC less than 6 LPA may participate in Super Dream category drives (> 12 LPA). Once a Super Dream offer is secured, the candidate is marked placed and withdrawn from subsequent recruitment drives.\n\n"
                        + "3. MANDATORY ATTENDANCE IN TRAINING:\n"
                        + "Attendance in soft skills, competitive coding bootcamps, and mock interview marathons is mandatory. Missing more than 2 sessions disqualifies the candidate from the immediate subsequent drive.",
                "Career Development Centre", "PDF", "3.2", "Placement Director"
        );

        campusDocumentRepository.saveAll(List.of(doc1, doc2, doc3, doc4));
        System.out.println("[DataInitializer] Seeded RAG knowledge base documents (4 institutional documents with exact citations)");
    }
}
