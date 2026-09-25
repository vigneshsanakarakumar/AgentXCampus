package com.agentx.campus.service.rag;

import java.util.List;
import java.util.Map;

/**
 * Standard SPI interface for Knowledge Retrieval and Document Ingestion.
 * Implemented by RAGFlow (Infiniflow) and Embedded Vector Provider.
 */
public interface RagProvider {

    String getProviderName();

    boolean isAvailable();

    List<RagRetrievalResult> retrieve(String query, int topK);

    RagDocumentUploadResult uploadDocument(String filename, byte[] content, String contentType, Map<String, Object> metadata);

    List<RagDocumentInfoDto> listDocuments();

    boolean deleteDocument(String documentId);
}
