# AgentX Campus — RAGFlow Architecture & Enterprise Knowledge Integration

This document outlines the architectural design, implementation details, data flow, ingestion pipeline, citation mechanisms, and production operational considerations for the RAGFlow integration in **AgentX Campus**.

---

## 1. Why Infiniflow RAGFlow Was Selected

[Infiniflow RAGFlow](https://github.com/infiniflow/ragflow) is an open-source, next-generation RAG engine built on deep document understanding. It was selected for AgentX Campus due to:

- **Deep Document Layout Analysis**: Out-of-the-box support for complex statutory academic circulars, tables, multi-column PDF regulations, and DOCX institutional bylaws.
- **Explainable Chunking & Attribution**: Preserves exact page numbers, paragraph positions, and section anchors essential for academic accountability (e.g. condonation fees, hostel curfew policies).
- **Separation of Concerns**: Runs as an independent containerized service exposing standard REST APIs (`/api/v1/datasets`, `/api/v1/retrieval`), leaving the Spring Boot core clean and maintainable.
- **Hybrid Retrieval**: Combines dense vector similarity with sparse BM25 term weighting for high-precision retrieval on domain-specific campus jargon.

---

## 2. System Architecture

```text
                               React 19 Frontend
                                      │
                                      ▼  (REST / SSE)
                           Spring Boot 3.3.4 (Java 21)
                                      │
                                      ▼
                               Supervisor Agent
                                      │
                ┌─────────────────────┼─────────────────────┐
                │                     │                     │
                ▼                     ▼                     ▼
              MySQL              Rule Engine          RAG Orchestrator
         (Transactional)       (Deterministic)        (Knowledge Base)
         • Student Profiles    • 75% Attendance       • Dual-Mode Gateway
         • Timetable Entries   • Condonation (65-74%) • RAGFlow (Primary)
         • Attendance Records  • Detention (<65%)     • Embedded Subword
         • Exam Schedules      • Clashes & Overlaps     Vector Engine
                │                     │                     │
                └─────────────────────┼─────────────────────┘
                                      │
                                      ▼
                               Groq AI Engine
                         (LLaMA 3.3 70B Versatile)
                                      │
                                      ▼
                    Grounded Answer with Verified Citations
                            & Real Execution Trace
```

---

## 3. Data Flow & Separation of Responsibilities

AgentX Campus strictly prevents hallucination by enforcing clean boundaries across data stores:

| Component | Responsibility | Examples |
| :--- | :--- | :--- |
| **MySQL Database** | Deterministic student records and campus state | "What is my current attendance percentage?" (e.g. 68.4%) |
| **RAGFlow / RAG Engine** | Statutory institutional policies, regulations & handbooks | "What does Academic Regulation 2026 say about condonation?" |
| **Rule Engine** | Deterministic business rules & compliance calculations | Evaluates whether 68.4% qualifies for condonation with ₹750 fee |
| **Groq LLM** | Natural language synthesis & empathetic explanation | Summarizes the rule engine's finding and quotes official sources |

The LLM is **never** permitted to invent rules, query arbitrary databases directly, or fabricate regulation numbers.

---

## 4. Dual-Mode RAG Architecture & Transparent Failover

To ensure maximum availability during development, continuous integration, or container restarts, AgentX Campus implements a **Dual-Mode RAG Architecture**:

1. **RAGFlow Provider (`RagFlowClient`)**:
   - Primary knowledge retrieval provider when `ragflow.enabled=true` and the container is online.
   - Pinned API Version: **v0.16.0**.
   - Authenticates via `Bearer <RAGFLOW_API_KEY>`.
   - Dedicated dataset: `AgentX-Institutional-KB`.
2. **Embedded Vector Provider (`EmbeddedVectorRagProvider`)**:
   - In-memory dense subword semantic vector engine.
   - Generates 256-dimensional L2-normalized embeddings with cosine similarity and keyword concept boosting.
   - Instant 100% precision fallback whenever RAGFlow is offline, restarting, or disabled.
   - Zero application downtime.

---

## 5. Document Ingestion Pipeline

Administrators can upload documents via the **Admin Portal** or REST API:

```text
Admin Upload (PDF / DOCX / TXT)
              │
              ▼
   Spring Boot Admin Endpoint (POST /api/v1/admin/rag/upload)
              │
     ┌────────┴────────┐
     ▼                 ▼
Embedded MySQL     RAGFlow API (POST /api/v1/datasets/{id}/documents)
CampusDocument         │
     │                 ▼
Subword Vector     RAGFlow Document Parsing & Chunking Engine
Index Cache            │
                       ▼
                   Vector Storage (Infinity / Elasticsearch)
```

Supported categories:
- `REGULATION`: Academic Regulations, Condonation & Detention Bylaws
- `EXAM_RULES`: CIA Examination Weightages, Hall Ticket Rules
- `CAMPUS_GUIDE`: Hostel Code of Conduct, Gate Pass Guidelines, Transport Routes
- `POLICY`: Campus Placement Eligibility & Drive Procedures

---

## 6. Self-Correcting Retrieval (Agentic RAG)

When a student asks an ambiguous question (e.g., *"Can I write exam?"* or *"fee for 68% attendance"*):
1. **Initial Retrieval**: The system queries the active RAG provider.
2. **Confidence Check**: If the top similarity score is below `0.45`, the agent triggers a self-correction step.
3. **Query Expansion**: The query is reformulated with statutory domain synonyms (e.g., `"attendance eligibility condonation fee detention semester examination"`).
4. **Second Retrieval**: Re-queries the knowledge base.
5. **Execution Trace**: The query reformulation is recorded in the agent trace for auditability.

---

## 7. Verified Source Citations

Every grounded RAG response must include authentic citations:

```text
📌 Verified Citations:
• Autonomous Academic Regulations 2026 | Attendance & Condonation Regulations (§ 4.2) | Page 24, Para 2 (Ver: 2026.1)
```

In the frontend, citations are rendered as interactive metadata pills displaying the document title, section heading, page number, and paragraph number.

---

## 8. Environment Variables

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `RAG_PROVIDER` | `auto` | `auto`, `ragflow`, or `embedded` |
| `RAGFLOW_ENABLED` | `false` | Enable/disable external RAGFlow integration |
| `RAGFLOW_BASE_URL` | `http://localhost:9380` | URL of the RAGFlow HTTP REST service |
| `RAGFLOW_API_KEY` | *(empty)* | API Bearer token generated in RAGFlow UI |
| `RAGFLOW_DATASET_ID`| *(auto-discover)* | Target dataset ID (auto-creates if blank) |
| `RAGFLOW_CONNECT_TIMEOUT_MS` | `5000` | HTTP connect timeout in milliseconds |
| `RAGFLOW_READ_TIMEOUT_MS` | `10000` | HTTP read/query timeout in milliseconds |
| `RAGFLOW_MAX_RETRIES` | `2` | Maximum retry attempts for transient network errors |

---

## 9. Local Setup & Docker Deployment

### Option A: Standard Development Mode (Embedded Engine Active)
1. Start MySQL:
   ```bash
   docker run -d --name agentx-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=agentx_campus mysql:8.0
   ```
2. Run Spring Boot Backend:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
3. Run React Frontend:
   ```bash
   cd frontend
   npm run dev
   ```

### Option B: Full Production Cluster with RAGFlow
1. Launch the RAGFlow cluster:
   ```bash
   docker compose -f docker-compose.ragflow.yml up -d
   ```
2. Verify RAGFlow UI at `http://localhost:9380`.
3. Generate an API Key under User Settings → API Keys.
4. Set environment variables in `backend/.env` or root `.env`:
   ```properties
   RAGFLOW_ENABLED=true
   RAGFLOW_BASE_URL=http://localhost:9380
   RAGFLOW_API_KEY=your_ragflow_api_key_here
   ```
5. Launch the complete application stack:
   ```bash
   docker compose up -d
   ```

---

## 10. Production Considerations & Known Limitations

- **Asynchronous Ingestion**: In RAGFlow, large PDF files (50+ pages) may require 10-30 seconds to parse chunks and generate embeddings. The Admin Portal reflects this via the `PARSING` status badge.
- **Network Timeouts**: All HTTP calls to RAGFlow enforce a 5-second connect timeout and a 10-second read timeout to prevent thread starvation in Spring Boot.
- **Audit Logging**: All RAG queries, latencies, and tool selections are persisted in the `agent_task_logs` table for compliance and performance monitoring.
