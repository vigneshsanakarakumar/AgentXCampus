package com.agentx.campus.service.rag;

import java.util.HashMap;
import java.util.Map;

/**
 * Normalized RAG Retrieval Result across RAGFlow and Embedded Vector Providers.
 */
public class RagRetrievalResult {
    private String documentId;
    private String documentTitle;
    private String category;
    private String sectionTitle;
    private int pageNumber;
    private int paragraphNumber;
    private String version;
    private String content;
    private double score;
    private String provider;
    private Map<String, Object> metadata = new HashMap<>();

    public RagRetrievalResult() {}

    public RagRetrievalResult(String documentId, String documentTitle, String category,
                              String sectionTitle, int pageNumber, int paragraphNumber,
                              String version, String content, double score, String provider) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.category = category;
        this.sectionTitle = sectionTitle;
        this.pageNumber = pageNumber;
        this.paragraphNumber = paragraphNumber;
        this.version = version != null ? version : "2026.1";
        this.content = content;
        this.score = score;
        this.provider = provider;
    }

    public String getCitation() {
        return String.format("%s | %s | Page %d, Para %d (Ver: %s)",
                documentTitle, sectionTitle != null ? sectionTitle : "General",
                pageNumber > 0 ? pageNumber : 1,
                paragraphNumber > 0 ? paragraphNumber : 1,
                version);
    }

    public String getCompactCitation() {
        return String.format("📌 *Source: %s, %s (Page %d, Para %d)*",
                documentTitle,
                sectionTitle != null ? sectionTitle : "Policy Regulations",
                pageNumber > 0 ? pageNumber : 1,
                paragraphNumber > 0 ? paragraphNumber : 1);
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public int getParagraphNumber() { return paragraphNumber; }
    public void setParagraphNumber(int paragraphNumber) { this.paragraphNumber = paragraphNumber; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
