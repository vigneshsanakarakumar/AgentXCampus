# AgentX Campus 🎓🤖

> **Autonomous, Multi-Agent Intelligent Campus Operating System**  
> Built with **Spring Boot 3.3.4**, **React 19 / Vite / Tailwind CSS**, **Spring Security 6 (JWT)**, **MySQL 8 + Flyway**, and **Groq LPU LLM Inference**.

---

## 🌟 Overview

**AgentX Campus** is an enterprise-grade academic operations platform powered by specialized AI agents and real-time rule engines. It coordinates student lifecycles, faculty mentorship, class timetables, session-based attendance, hostel & transportation logistics, and automated grievance resolution.

---

## 🏗️ Architecture

```
                    ┌────────────────────────────┐
                    │      React 19 Client       │
                    │  (Vite + Tailwind + Lucide)│
                    └─────────────┬──────────────┘
                                  │ HTTP / REST / JWT
                                  ▼
                    ┌────────────────────────────┐
                    │    Spring Boot 3 Core      │
                    │   Spring Security 6 + JWT  │
                    └──────┬───────┬───────┬─────┘
                           │       │       │
       ┌───────────────────┘       │       └───────────────────┐
       ▼                           ▼                           ▼
┌──────────────┐          ┌─────────────────┐         ┌─────────────────┐
│ Multi-Agent  │          │ Conflict Engine │         │  MySQL 8 DB     │
│ Orchestration│          │ & Rule Systems  │         │  (Flyway V1-V9) │
│ (Groq LPU)   │          └─────────────────┘         └─────────────────┘
└──────────────┘
```

### Multi-Agent Subsystems
- **Supervisor Agent**: Central intent classifier and task router.
- **Academic Agent**: Explains attendance records, syllabus, and assessments.
- **Grievance Agent**: Categorizes complaints, assigns urgency, routes to mentors.
- **Conflict Engine**: Prevents double-bookings across rooms, faculty schedules, and exam dates.
- **Notification Agent**: Sends context-aware alerts with explainability tags (`matched_because`).

---

## 🚀 Key Features

1. **4 Mentor Portals & 4-Class Section Management**
   - Pre-configured sections: CSE-A (Sem 3), CSE-B (Sem 4), CSE-C (Sem 5), CSE-D (Sem 6).
   - Class rosters, individual mentee tracking, and attendance summaries.
2. **Weekly Timetable (20 Periods per Section)**
   - Complete Mon–Fri class schedule for each section with conflict detection.
3. **Session-Based Attendance**
   - Subject-wise session logging, student attendance history, and real-time recalculation.
4. **Leave, On-Duty (OD), and Document Requests**
   - Mentors approve leave and OD requests with automatic cascade to attendance records.
5. **Hostel & Transport Management**
   - Room allocations (Block A Boys, Block B Girls), digital gate passes, and bus route passes.
6. **Examination Scheduler**
   - Clash detection against regular class hours and room allocations.
7. **Opportunities & Placement Board**
   - Curated internships, hackathons, and certifications filtered by year and department.
8. **Interactive OpenAPI / Swagger Documentation**
   - Live Swagger UI at `/swagger-ui/index.html`.

---

## 👥 Default Demo Credentials

### 🛡️ System Administrator
- **Username:** `admin` | **Password:** `admin`

### 👨‍🏫 Faculty Mentors (All passwords: `faculty123`)
| Username | Name | Role | Department & Section | Semester |
|---|---|---|---|---|
| `priya.m` | Dr. M. Priya | Associate Professor | CSE - Section A | Sem 3 |
| `suresh.s` | Dr. S. Suresh | Associate Professor | CSE - Section B | Sem 4 |
| `ramesh.k` | Dr. K. Ramesh | Professor & HOD | CSE - Section C | Sem 5 |
| `anand.r` | Prof. R. Anand | Assistant Professor | CSE - Section D | Sem 6 |

### 🎓 Students (All passwords: `student123`)
- **Section A (Sem 3):** `stud_a1`, `stud_a2`, `stud_a3`, `stud_a4`, `stud_a5`
- **Section B (Sem 4):** `stud_b1`, `stud_b2`, `stud_b3`, `stud_b4`, `stud_b5`
- **Section C (Sem 5):** `vasan` (`717824P361`), `stud_c2`, `stud_c3`, `stud_c4`, `stud_c5`
- **Section D (Sem 6):** `stud_d1`, `stud_d2`, `stud_d3`, `stud_d4`, `stud_d5`

---

## 🛠️ Tech Stack

- **Backend:** Java 21, Spring Boot 3.3.4, Spring Security 6, Spring Data JPA, Hibernate, Flyway Migration.
- **Frontend:** React 19, Vite, Tailwind CSS, Lucide React, Axios.
- **Database:** MySQL 8.
- **AI Inference:** Groq Cloud API (`llama-3.3-70b-versatile`).
- **API Documentation:** SpringDoc OpenAPI 3.0 / Swagger UI.

---

## 🚦 Getting Started

### Prerequisites
- JDK 21+
- Node.js 18+ and npm
- MySQL 8.0+ running on port `3306`

### 1. Database Setup
Create database `agentx_campus` in MySQL:
```sql
CREATE DATABASE IF NOT EXISTS agentx_campus;
```

### 2. Backend Setup
```bash
cd backend
./mvnw clean package -DskipTests
java -jar target/campus-core-1.0.0.jar
```
*Backend runs on `http://localhost:8080`*  
*Swagger UI available at `http://localhost:8080/swagger-ui/index.html`*

### 3. Frontend Setup
```bash
cd frontend
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173`*

---

## 📄 License
This project is open-source under the MIT License.
