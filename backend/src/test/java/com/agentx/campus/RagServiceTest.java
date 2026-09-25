package com.agentx.campus;

import com.agentx.campus.model.CampusDocument;
import com.agentx.campus.repository.CampusDocumentRepository;
import com.agentx.campus.service.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RagServiceTest {

    private CampusDocumentRepository docRepo;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        docRepo = Mockito.mock(CampusDocumentRepository.class);

        CampusDocument doc1 = new CampusDocument(
                "Autonomous Academic Regulations 2026",
                "REGULATION",
                "Official statutory regulations.",
                "1. ATTENDANCE REQUIREMENTS & CONDONATION:\n"
                        + "A candidate who has secured not less than 75% of classes in each subject shall be eligible to appear for End Semester Examinations.\n\n"
                        + "Condonation of shortage of attendance between 65% and 74% (inclusive) may be granted on valid medical grounds with a medical certificate submitted within 3 working days. A mandatory condonation fee of ₹750 per subject must be remitted.\n\n"
                        + "Candidates who secure less than 65% attendance in any course are strictly NOT permitted to write the end semester examination and must repeat the course.",
                "All Departments", "PDF", "2026.1", "Dean of Academic Affairs"
        );
        doc1.setId(1L);

        CampusDocument doc2 = new CampusDocument(
                "Hostel Code of Conduct & Guidelines",
                "CAMPUS_GUIDE",
                "Residential guidelines.",
                "1. HOSTEL CURFEW & TIMINGS:\n"
                        + "All residential students must return to hostel by 08:30 PM on weekdays.\n\n"
                        + "2. OUTING & GATE PASS PROCEDURES:\n"
                        + "Saturday Daytime Local Outing: Hostellers are permitted local daytime outing on Saturdays between 09:00 AM and 08:30 PM without parents' call or telephone confirmation, provided they return before curfew.\n\n"
                        + "Overnight Leave: For overnight stays, hostellers CANNOT leave without parents' call.",
                "Hostel Administration", "PDF", "2.0", "Chief Residential Warden"
        );
        doc2.setId(2L);

        when(docRepo.findByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(doc1, doc2));
        ragService = new RagService(docRepo);
        ragService.rebuildVectorIndex();
    }

    @Test
    @DisplayName("Should generate L2-normalized 256-dimensional vector embedding")
    void testComputeEmbeddingNormalization() {
        String text = "Autonomous Academic Regulations 2026 attendance condonation";
        double[] embedding = ragService.computeEmbedding(text);

        assertEquals(256, embedding.length, "Embedding vector must be 256 dimensions");

        double norm = 0.0;
        for (double v : embedding) norm += v * v;
        norm = Math.sqrt(norm);

        assertTrue(Math.abs(norm - 1.0) < 1e-4, "Embedding vector must be L2 normalized to unit length");
    }

    @Test
    @DisplayName("Should retrieve attendance condonation regulation with exact citation")
    void testRetrieveAttendanceRegulation() {
        List<RagService.RagChunk> results = ragService.retrieveRelevantChunks("What is the condonation fee if my attendance is 68%?", 2);

        assertFalse(results.isEmpty(), "Expected RAG to retrieve matching handbook chunk");
        RagService.RagChunk top = results.get(0);

        assertTrue(top.getDocumentTitle().contains("Autonomous Academic Regulations"), "Expected Academic Regulations document");
        assertTrue(top.getContent().contains("750"), "Retrieved chunk must contain ₹750 condonation fee");
        assertTrue(top.getCitation().contains("Page"), "Citation must include page number");
        assertTrue(top.getCitation().contains("Para"), "Citation must include paragraph number");
    }

    @Test
    @DisplayName("Should retrieve hostel Saturday outing rule with exact citation")
    void testRetrieveHostelOutingRule() {
        List<RagService.RagChunk> results = ragService.retrieveRelevantChunks("Can hostellers leave on Saturday without parents' call?", 2);

        assertFalse(results.isEmpty(), "Expected RAG to retrieve hostel handbook chunk");
        RagService.RagChunk top = results.get(0);

        assertTrue(top.getDocumentTitle().contains("Hostel"), "Expected Hostel Code of Conduct document");
        assertTrue(top.getContent().toLowerCase().contains("without parents' call"), "Chunk must address parent call requirement");
    }

    @Test
    @DisplayName("Self-correction loop should reformulate domain query when confidence is low")
    void testSelfCorrectionLoop() {
        List<String> traces = new ArrayList<>();
        List<RagService.RagChunk> results = ragService.retrieveWithSelfCorrection("fee for 68% attendance", 2, traces);

        assertFalse(results.isEmpty(), "Self-correction should successfully retrieve relevant chunk");
        assertTrue(results.get(0).getContent().contains("750"), "Should cite ₹750 condonation rule");
    }
}
