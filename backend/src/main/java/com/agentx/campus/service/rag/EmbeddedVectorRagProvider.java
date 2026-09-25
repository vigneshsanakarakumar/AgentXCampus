package com.agentx.campus.service.rag;

import com.agentx.campus.model.CampusDocument;
import com.agentx.campus.repository.CampusDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Embedded Semantic Vector RAG Engine.
 * Provides resilient, in-memory cosine similarity retrieval over dense subword embeddings
 * extracted from statutory campus documents stored in MySQL.
 */
@Component
public class EmbeddedVectorRagProvider implements RagProvider {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedVectorRagProvider.class);
    private static final int VECTOR_DIMENSION = 256;

    private final CampusDocumentRepository documentRepository;
    private final List<IndexedChunk> vectorIndex = Collections.synchronizedList(new ArrayList<>());
    private volatile long lastIndexTime = 0;

    public static class IndexedChunk {
        private final Long documentId;
        private final String documentTitle;
        private final String category;
        private final String sectionTitle;
        private final int pageNumber;
        private final int paragraphNumber;
        private final String version;
        private final String content;
        private final double[] embedding;
        private double score;

        public IndexedChunk(Long documentId, String documentTitle, String category,
                            String sectionTitle, int pageNumber, int paragraphNumber,
                            String version, String content, double[] embedding) {
            this.documentId = documentId;
            this.documentTitle = documentTitle;
            this.category = category;
            this.sectionTitle = sectionTitle;
            this.pageNumber = pageNumber;
            this.paragraphNumber = paragraphNumber;
            this.version = version;
            this.content = content;
            this.embedding = embedding;
        }

        public Long getDocumentId() { return documentId; }
        public String getDocumentTitle() { return documentTitle; }
        public String getCategory() { return category; }
        public String getSectionTitle() { return sectionTitle; }
        public int getPageNumber() { return pageNumber; }
        public int getParagraphNumber() { return paragraphNumber; }
        public String getVersion() { return version; }
        public String getContent() { return content; }
        public double[] getEmbedding() { return embedding; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }

    public EmbeddedVectorRagProvider(CampusDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public String getProviderName() {
        return "EMBEDDED";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<RagRetrievalResult> retrieve(String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        ensureIndexFreshness();
        if (vectorIndex.isEmpty()) {
            return Collections.emptyList();
        }

        double[] queryVector = computeEmbedding(query);
        String lowerQuery = query.toLowerCase();
        Set<String> queryKeywords = extractKeywords(lowerQuery);

        List<IndexedChunk> scored = new ArrayList<>();
        for (IndexedChunk chunk : vectorIndex) {
            double cosineSim = dotProduct(queryVector, chunk.getEmbedding());
            double score = cosineSim;

            String lowerContent = chunk.getContent().toLowerCase();
            String lowerSection = chunk.getSectionTitle().toLowerCase();
            String lowerTitle = chunk.getDocumentTitle().toLowerCase();

            boolean hasKeywordMatch = false;
            for (String kw : queryKeywords) {
                if (lowerSection.contains(kw) || lowerTitle.contains(kw) || lowerContent.contains(kw)) {
                    hasKeywordMatch = true;
                    if (lowerSection.contains(kw)) score += 0.35;
                    if (lowerContent.contains(kw)) score += 0.15;
                }
            }

            if (lowerQuery.contains("condonation") && lowerContent.contains("condonation")) {
                score += 0.40;
                hasKeywordMatch = true;
            }
            if (lowerQuery.contains("attendance") && lowerContent.contains("attendance")) {
                score += 0.25;
                hasKeywordMatch = true;
            }
            if (lowerQuery.contains("hostel") && lowerContent.contains("hostel")) {
                score += 0.30;
                hasKeywordMatch = true;
            }
            if (lowerQuery.contains("visitor") && lowerContent.contains("visitor")) {
                score += 0.35;
                hasKeywordMatch = true;
            }
            if (lowerQuery.contains("gate pass") && lowerContent.contains("gate pass")) {
                score += 0.35;
                hasKeywordMatch = true;
            }
            if (lowerQuery.contains("exam") && lowerContent.contains("hall ticket")) {
                score += 0.30;
                hasKeywordMatch = true;
            }

            chunk.setScore(score);
            // Require either a relevant keyword match with score > 0.20 or high pure cosine similarity > 0.45
            if ((hasKeywordMatch && score > 0.20) || score > 0.45) {
                scored.add(chunk);
            }
        }

        scored.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        List<RagRetrievalResult> results = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            IndexedChunk c = scored.get(i);
            results.add(new RagRetrievalResult(
                    String.valueOf(c.getDocumentId()),
                    c.getDocumentTitle(),
                    c.getCategory(),
                    c.getSectionTitle(),
                    c.getPageNumber(),
                    c.getParagraphNumber(),
                    c.getVersion(),
                    c.getContent(),
                    c.getScore(),
                    "EMBEDDED"
            ));
        }

        return results;
    }

    @Override
    public RagDocumentUploadResult uploadDocument(String filename, byte[] content, String contentType, Map<String, Object> metadata) {
        try {
            String text = new String(content, StandardCharsets.UTF_8);
            String title = (String) metadata.getOrDefault("title", filename);
            String category = (String) metadata.getOrDefault("category", "General");
            String department = (String) metadata.getOrDefault("department", "All");
            String version = (String) metadata.getOrDefault("version", "2026.1");

            CampusDocument doc = new CampusDocument(title, category, "Uploaded institutional policy", text, department, "PDF", version, "Admin");
            CampusDocument saved = documentRepository.save(doc);

            // Invalidate index to rebuild on next query
            lastIndexTime = 0;

            return new RagDocumentUploadResult(
                    String.valueOf(saved.getId()),
                    saved.getTitle(),
                    "INDEXED",
                    "Document successfully indexed in Embedded Knowledge Engine.",
                    extractParagraphs(text).size()
            );
        } catch (Exception ex) {
            log.error("Failed to upload document to Embedded RAG: {}", ex.getMessage(), ex);
            return new RagDocumentUploadResult(null, filename, "FAILED", ex.getMessage(), 0);
        }
    }

    @Override
    public List<RagDocumentInfoDto> listDocuments() {
        List<CampusDocument> docs = documentRepository.findByActiveTrueOrderByCreatedAtDesc();
        if (docs.isEmpty()) {
            docs = documentRepository.findAll();
        }
        return docs.stream().map(d -> new RagDocumentInfoDto(
                String.valueOf(d.getId()),
                d.getTitle(),
                d.getCategory(),
                "All",
                "2026.1",
                "INDEXED",
                extractParagraphs(d.getContent()).size(),
                "EMBEDDED",
                d.getCreatedAt() != null ? d.getCreatedAt().toString() : LocalDateTime.now().toString()
        )).collect(Collectors.toList());
    }

    @Override
    public boolean deleteDocument(String documentId) {
        try {
            Long id = Long.valueOf(documentId);
            if (documentRepository.existsById(id)) {
                documentRepository.deleteById(id);
                lastIndexTime = 0;
                return true;
            }
        } catch (Exception ex) {
            log.warn("Failed to delete document from Embedded RAG: {}", ex.getMessage());
        }
        return false;
    }

    public synchronized void rebuildVectorIndex() {
        lastIndexTime = 0;
        ensureIndexFreshness();
    }

    public synchronized void ensureIndexFreshness() {
        if (System.currentTimeMillis() - lastIndexTime < 30_000 && !vectorIndex.isEmpty()) {
            return;
        }

        List<CampusDocument> docs = documentRepository.findByActiveTrueOrderByCreatedAtDesc();
        if (docs.isEmpty()) {
            docs = documentRepository.findAll();
        }
        List<IndexedChunk> newIndex = new ArrayList<>();

        for (CampusDocument doc : docs) {
            String content = doc.getContent();
            List<String> paragraphs = extractParagraphs(content);
            int page = 1;
            int para = 1;

            for (String p : paragraphs) {
                String sectionTitle = extractSectionHeading(p, doc.getTitle());
                double[] embedding = computeEmbedding(p);
                newIndex.add(new IndexedChunk(
                        doc.getId(),
                        doc.getTitle(),
                        doc.getCategory(),
                        sectionTitle,
                        page,
                        para,
                        "2026.1",
                        p.trim(),
                        embedding
                ));
                para++;
                if (para % 4 == 0) page++;
            }
        }

        vectorIndex.clear();
        vectorIndex.addAll(newIndex);
        lastIndexTime = System.currentTimeMillis();
        log.info("Refreshed Embedded RAG Vector Index: {} semantic chunks from {} documents",
                vectorIndex.size(), docs.size());
    }

    private List<String> extractParagraphs(String content) {
        if (content == null || content.trim().isEmpty()) return Collections.emptyList();
        String[] parts = content.split("(?m)^\\s*$\\n|\\r?\\n\\r?\\n");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (trimmed.length() >= 20) {
                result.add(trimmed);
            }
        }
        if (result.isEmpty()) result.add(content.trim());
        return result;
    }

    private String extractSectionHeading(String paragraph, String fallback) {
        Pattern pattern = Pattern.compile("^(§?\\s*\\d+(\\.\\d+)*\\s*[-:–]?\\s*[^.\\n]+)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(paragraph);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        if (paragraph.length() > 60) {
            int dot = paragraph.indexOf('.');
            if (dot > 10 && dot < 60) {
                return paragraph.substring(0, dot).trim();
            }
        }
        return fallback;
    }

    public double[] computeEmbedding(String text) {
        double[] vec = new double[VECTOR_DIMENSION];
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = normalized.split("\\s+");

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            int h1 = Math.abs(token.hashCode());
            vec[h1 % VECTOR_DIMENSION] += 1.0;

            for (int i = 0; i <= token.length() - 3; i++) {
                String sub = token.substring(i, i + 3);
                int h2 = Math.abs(sub.hashCode());
                vec[h2 % VECTOR_DIMENSION] += 0.5;
            }
        }

        double norm = 0.0;
        for (double v : vec) norm += v * v;
        if (norm > 0) {
            norm = Math.sqrt(norm);
            for (int i = 0; i < VECTOR_DIMENSION; i++) vec[i] /= norm;
        }
        return vec;
    }

    private double dotProduct(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private Set<String> extractKeywords(String q) {
        Set<String> stopWords = Set.of("the", "a", "an", "is", "are", "in", "to", "for", "of", "and", "or", "what", "how", "can", "i", "my", "do", "you");
        return Arrays.stream(q.split("[^a-zA-Z0-9]+"))
                .map(String::trim)
                .filter(w -> w.length() > 2 && !stopWords.contains(w))
                .collect(Collectors.toSet());
    }

    public int getIndexedChunkCount() {
        return vectorIndex.size();
    }
}
