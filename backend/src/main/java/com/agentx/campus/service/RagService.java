package com.agentx.campus.service;

import com.agentx.campus.model.CampusDocument;
import com.agentx.campus.repository.CampusDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final CampusDocumentRepository documentRepository;

    public static class RagChunk {
        private final Long documentId;
        private final String documentTitle;
        private final String category;
        private final String version;
        private final int chunkIndex;
        private final String content;
        private double score;

        public RagChunk(Long documentId, String documentTitle, String category, String version, int chunkIndex, String content) {
            this.documentId = documentId;
            this.documentTitle = documentTitle;
            this.category = category;
            this.version = version;
            this.chunkIndex = chunkIndex;
            this.content = content;
        }

        public Long getDocumentId() { return documentId; }
        public String getDocumentTitle() { return documentTitle; }
        public String getCategory() { return category; }
        public String getVersion() { return version; }
        public int getChunkIndex() { return chunkIndex; }
        public String getContent() { return content; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public String getCitation() {
            return String.format("%s (Category: %s, Ver: %s, Sec: #%d)", documentTitle, category, version, chunkIndex + 1);
        }
    }

    public RagService(CampusDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<RagChunk> retrieveRelevantChunks(String query, int topK) {
        List<CampusDocument> docs = documentRepository.findByActiveTrueOrderByCreatedAtDesc();
        if (docs.isEmpty() || query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Tokenize query into distinct search terms
        Set<String> queryTerms = Arrays.stream(query.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());

        List<RagChunk> scoredChunks = new ArrayList<>();

        for (CampusDocument doc : docs) {
            List<String> chunks = splitIntoChunks(doc.getContent());
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                RagChunk item = new RagChunk(
                        doc.getId(),
                        doc.getTitle(),
                        doc.getCategory(),
                        doc.getVersion(),
                        i,
                        chunkText
                );

                double score = scoreChunk(item, queryTerms, query.toLowerCase());
                if (score > 0.0) {
                    item.setScore(score);
                    scoredChunks.add(item);
                }
            }
        }

        // Sort descending by score and pick topK
        return scoredChunks.stream()
                .sorted(Comparator.comparingDouble(RagChunk::getScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    private double scoreChunk(RagChunk chunk, Set<String> queryTerms, String fullQuery) {
        double score = 0.0;
        String text = chunk.getContent().toLowerCase();
        String title = chunk.getDocumentTitle().toLowerCase();
        String cat = chunk.getCategory().toLowerCase();

        // Exact phrase match bonus
        if (text.contains(fullQuery)) {
            score += 15.0;
        }

        // Title and Category boost
        for (String term : queryTerms) {
            if (title.contains(term)) score += 6.0;
            if (cat.contains(term)) score += 4.0;
            if (text.contains(term)) {
                // Term frequency
                int count = 0;
                int idx = 0;
                while ((idx = text.indexOf(term, idx)) != -1) {
                    count++;
                    idx += term.length();
                }
                score += Math.min(count * 2.0, 10.0);
            }
        }

        return score;
    }

    private List<String> splitIntoChunks(String content) {
        if (content == null || content.trim().isEmpty()) return Collections.emptyList();
        String[] paragraphs = content.split("\n\n+");
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String p : paragraphs) {
            String trimmed = p.trim();
            if (trimmed.isEmpty()) continue;
            if (current.length() + trimmed.length() > 600) {
                if (current.length() > 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                }
            }
            if (current.length() > 0) current.append("\n\n");
            current.append(trimmed);
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        return result;
    }

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "is", "are", "in", "of", "for", "to", "with", "what", "which",
            "when", "where", "how", "can", "tell", "show", "please", "about", "according"
    );
}
