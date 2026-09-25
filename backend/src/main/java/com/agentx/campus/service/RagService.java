package com.agentx.campus.service;

import com.agentx.campus.model.CampusDocument;
import com.agentx.campus.repository.CampusDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final CampusDocumentRepository documentRepository;
    private static final int VECTOR_DIMENSION = 256;

    // In-memory cache of indexed semantic vector chunks
    private final List<RagChunk> vectorIndex = Collections.synchronizedList(new ArrayList<>());
    private volatile long lastIndexTime = 0;

    public static class RagChunk {
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

        public RagChunk(Long documentId, String documentTitle, String category,
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

        public String getCitation() {
            return String.format("%s | %s | Page %d, Para %d (Ver: %s)",
                    documentTitle, sectionTitle, pageNumber, paragraphNumber, version);
        }

        public String getCompactCitation() {
            return String.format("📌 *Source: %s, %s (Page %d, Para %d)*",
                    documentTitle, sectionTitle, pageNumber, paragraphNumber);
        }
    }

    public RagService(CampusDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * Semantic Vector Retrieval using Cosine Similarity over Dense Subword Embeddings.
     */
    public List<RagChunk> retrieveRelevantChunks(String query, int topK) {
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

        List<RagChunk> candidateScored = new ArrayList<>();

        for (RagChunk chunk : vectorIndex) {
            // 1. Semantic Cosine Similarity (vectors are L2 normalized, dot product = cosine similarity)
            double cosineSim = dotProduct(queryVector, chunk.getEmbedding());

            // 2. Keyword & Concept Multipliers
            double keywordBoost = 0.0;
            String lowerContent = chunk.getContent().toLowerCase();
            String lowerSection = chunk.getSectionTitle().toLowerCase();
            String lowerTitle = chunk.getDocumentTitle().toLowerCase();

            for (String kw : queryKeywords) {
                if (lowerSection.contains(kw)) keywordBoost += 0.25;
                if (lowerTitle.contains(kw)) keywordBoost += 0.15;
                if (lowerContent.contains(kw)) keywordBoost += 0.10;
            }

            // 3. Exact phrase match bonus
            if (lowerContent.contains(lowerQuery)) {
                keywordBoost += 0.40;
            }

            double totalScore = cosineSim + keywordBoost;

            // Retain chunks with sufficient semantic affinity
            if (totalScore > 0.15) {
                RagChunk scored = new RagChunk(
                        chunk.getDocumentId(),
                        chunk.getDocumentTitle(),
                        chunk.getCategory(),
                        chunk.getSectionTitle(),
                        chunk.getPageNumber(),
                        chunk.getParagraphNumber(),
                        chunk.getVersion(),
                        chunk.getContent(),
                        chunk.getEmbedding()
                );
                scored.setScore(Math.round(totalScore * 1000.0) / 1000.0);
                candidateScored.add(scored);
            }
        }

        // Sort descending by score and pick topK
        return candidateScored.stream()
                .sorted(Comparator.comparingDouble(RagChunk::getScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Compute Dense L2-Normalized Semantic Vector Embedding using Multi-Scale Character N-Grams and Word Hashing.
     */
    public double[] computeEmbedding(String text) {
        double[] vector = new double[VECTOR_DIMENSION];
        if (text == null || text.isBlank()) return vector;

        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9%₹\\s]", " ");
        String[] tokens = cleaned.split("\\s+");

        for (String token : tokens) {
            if (token.length() < 2 || STOP_WORDS.contains(token)) continue;

            // 1. Full token hash
            int tokenHash = Math.abs(token.hashCode()) % VECTOR_DIMENSION;
            vector[tokenHash] += 2.0;

            // 2. Subword 3-grams
            for (int i = 0; i <= token.length() - 3; i++) {
                String tri = token.substring(i, i + 3);
                int triHash = Math.abs(tri.hashCode() * 31) % VECTOR_DIMENSION;
                vector[triHash] += 0.8;
            }

            // 3. Subword 4-grams
            for (int i = 0; i <= token.length() - 4; i++) {
                String quad = token.substring(i, i + 4);
                int quadHash = Math.abs(quad.hashCode() * 17) % VECTOR_DIMENSION;
                vector[quadHash] += 1.2;
            }
        }

        // L2 Normalize
        double norm = 0.0;
        for (double v : vector) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < VECTOR_DIMENSION; i++) vector[i] /= norm;
        }

        return vector;
    }

    private double dotProduct(double[] v1, double[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) return 0.0;
        double dot = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
        }
        return dot;
    }

    private synchronized void ensureIndexFreshness() {
        // Re-index every 5 minutes or if index is empty
        long now = System.currentTimeMillis();
        if (vectorIndex.isEmpty() || (now - lastIndexTime) > 300_000) {
            rebuildVectorIndex();
            lastIndexTime = now;
        }
    }

    public synchronized void rebuildVectorIndex() {
        List<CampusDocument> docs = documentRepository.findByActiveTrueOrderByCreatedAtDesc();
        vectorIndex.clear();

        for (CampusDocument doc : docs) {
            List<RagChunk> docChunks = parseDocumentIntoSemanticChunks(doc);
            vectorIndex.addAll(docChunks);
        }
    }

    /**
     * Parses document content into structured semantic chunks with section titles, page and paragraph numbers.
     */
    private List<RagChunk> parseDocumentIntoSemanticChunks(CampusDocument doc) {
        List<RagChunk> chunks = new ArrayList<>();
        if (doc.getContent() == null || doc.getContent().trim().isEmpty()) return chunks;

        String[] sections = doc.getContent().split("(?m)(?=^[0-9]+\\.\\s+)");
        int globalPage = 1;
        int runningCharCount = 0;

        for (int sIdx = 0; sIdx < sections.length; sIdx++) {
            String sectionBlock = sections[sIdx].trim();
            if (sectionBlock.isEmpty()) continue;

            String sectionTitle = "General Regulations";
            String sectionBody = sectionBlock;

            int newlineIdx = sectionBlock.indexOf('\n');
            if (newlineIdx > 0) {
                sectionTitle = sectionBlock.substring(0, newlineIdx).replaceAll("^[0-9]+\\.\\s*", "").trim();
                sectionBody = sectionBlock.substring(newlineIdx).trim();
            }

            String[] paragraphs = sectionBody.split("\n\n+");
            for (int pIdx = 0; pIdx < paragraphs.length; pIdx++) {
                String para = paragraphs[pIdx].trim();
                if (para.isEmpty()) continue;

                runningCharCount += para.length();
                // Estimate page based on 800 characters per handbook page
                int pageNum = Math.max(1, (runningCharCount / 800) + 1);

                double[] embedding = computeEmbedding(sectionTitle + " " + para);
                RagChunk chunk = new RagChunk(
                        doc.getId(),
                        doc.getTitle(),
                        doc.getCategory(),
                        sectionTitle,
                        pageNum,
                        pIdx + 1,
                        doc.getVersion() != null ? doc.getVersion() : "1.0",
                        para,
                        embedding
                );
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    private Set<String> extractKeywords(String query) {
        return Arrays.stream(query.split("[^a-z0-9%₹]+"))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());
    }

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "is", "are", "in", "of", "for", "to", "with", "what", "which",
            "when", "where", "how", "can", "tell", "show", "please", "about", "according",
            "from", "that", "this", "there", "then", "have", "been", "will", "would", "could", "should"
    );
}
