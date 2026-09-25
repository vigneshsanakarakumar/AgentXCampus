package com.agentx.campus;

import com.agentx.campus.model.CampusDocument;
import com.agentx.campus.repository.CampusDocumentRepository;
import com.agentx.campus.service.RagService;
import com.agentx.campus.service.rag.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RagFlowClientTest {

    private CampusDocumentRepository docRepo;
    private EmbeddedVectorRagProvider embeddedProvider;
    private RagFlowClient disabledRagflowClient;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        docRepo = Mockito.mock(CampusDocumentRepository.class);

        CampusDocument doc1 = new CampusDocument(
                "Autonomous Academic Regulations 2026",
                "REGULATION",
                "Regulations.",
                "1. ATTENDANCE REQUIREMENTS & CONDONATION:\n"
                        + "A candidate who has fulfilled attendance by securing not less than 75% of classes shall be eligible.\n\n"
                        + "Condonation between 65% and 74% requires medical certificate and a condonation fee of ₹750 per subject.\n\n"
                        + "Less than 65% attendance results in detention and course repetition.",
                "All Departments", "PDF", "2026.1", "Dean"
        );
        doc1.setId(1L);

        when(docRepo.findByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(doc1));

        embeddedProvider = new EmbeddedVectorRagProvider(docRepo);
        embeddedProvider.rebuildVectorIndex();

        // Configured with enabled=false to verify graceful fallback
        disabledRagflowClient = new RagFlowClient(
                "http://localhost:9380",
                "test-api-key",
                "dataset-123",
                false, // disabled
                1000,
                2000,
                2,
                new ObjectMapper()
        );

        ragService = new RagService(docRepo, disabledRagflowClient, embeddedProvider);
    }

    @Test
    @DisplayName("Should detect RAGFlow as disabled and report accurate cluster status")
    void testRagStatusWhenRagFlowDisabled() {
        RagStatusDto status = ragService.getStatus();

        assertNotNull(status);
        assertFalse(status.isRagflowEnabled(), "RAGFlow should be marked as disabled");
        assertFalse(status.isRagflowHealthy(), "RAGFlow should not be healthy when disabled");
        assertEquals("EMBEDDED", status.getActiveProvider(), "Active provider should fall back to EMBEDDED");
        assertTrue(status.getTotalDocuments() >= 1, "Embedded documents must be reported in total count");
    }

    @Test
    @DisplayName("Should transparently fall back to embedded vector provider when RAGFlow is offline/disabled")
    void testTransparentFailoverToEmbeddedProvider() {
        List<String> trace = new ArrayList<>();
        List<RagService.RagChunk> results = ragService.retrieveWithSelfCorrection(
                "What is the condonation fee if my attendance is 68%?",
                2,
                trace
        );

        assertFalse(results.isEmpty(), "Failover should successfully return institutional regulation chunks");
        RagService.RagChunk top = results.get(0);

        assertEquals("EMBEDDED", top.getProvider(), "Provider must be EMBEDDED during failover");
        assertTrue(results.stream().anyMatch(c -> c.getContent().contains("750")), "Retrieved chunks must contain ₹750 condonation fee");
        assertTrue(top.getDocumentTitle().contains("Academic Regulations 2026"));
        assertTrue(top.getCitation().contains("Page 1"), "Citation must include real page number");
    }

    @Test
    @DisplayName("Should parse and format RAGFlow retrieval results into standard citations")
    void testRagRetrievalResultFormatting() {
        RagRetrievalResult result = new RagRetrievalResult(
                "doc-ragflow-99",
                "Autonomous Academic Regulations 2026.pdf",
                "Regulation",
                "Attendance & Condonation Regulations (§ 4.2)",
                24,
                2,
                "2026.1",
                "Students securing between 65% and 74% attendance must pay ₹750 condonation fee.",
                0.92,
                "RAGFLOW"
        );

        String citation = result.getCitation();
        String compact = result.getCompactCitation();

        assertTrue(citation.contains("Page 24"), "Citation must contain page 24");
        assertTrue(citation.contains("Para 2"), "Citation must contain para 2");
        assertTrue(citation.contains("Ver: 2026.1"), "Citation must contain version 2026.1");
        assertTrue(compact.contains("📌 *Source:"), "Compact citation must follow Markdown source pill format");
    }

    @Test
    @DisplayName("Should refuse hallucination when queried for non-existent campus policy")
    void testNoResultForAbsurdQuery() {
        List<RagService.RagChunk> results = ragService.retrieveRelevantChunks("Martian alien spaceship parking protocol in campus basement", 2);

        assertTrue(results.isEmpty(), "Must return empty results rather than hallucinating an answer for unrelated queries");
    }
}
