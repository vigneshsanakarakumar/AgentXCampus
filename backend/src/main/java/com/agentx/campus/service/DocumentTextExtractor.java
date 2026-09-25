package com.agentx.campus.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class DocumentTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(DocumentTextExtractor.class);

    public String extractText(byte[] bytes, String filename) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        String lowerName = filename != null ? filename.toLowerCase() : "";

        // 1. PDF Documents
        if (lowerName.endsWith(".pdf") || isPdfHeader(bytes)) {
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                String extracted = stripper.getText(document);
                if (extracted != null && !extracted.isBlank()) {
                    return extracted.trim();
                }
            } catch (Exception ex) {
                log.warn("Failed to extract text from PDF using PDFBox: {}", ex.getMessage());
            }
        }

        // 2. Text / Markdown / CSV files
        if (lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(".csv") || lowerName.endsWith(".json")) {
            return new String(bytes, StandardCharsets.UTF_8).trim();
        }

        // 3. Image files (PNG, JPG, JPEG, WEBP)
        // If image, inspect bytes for any embedded text strings or generate a descriptive token string from filename
        String textFromBytes = extractAsciiStrings(bytes);
        if (textFromBytes.length() > 50) {
            return textFromBytes;
        }

        // Fallback: derive text from filename and metadata
        return "Document Upload: " + (filename != null ? filename : "Uploaded Campus Notice");
    }

    private boolean isPdfHeader(byte[] bytes) {
        if (bytes.length < 5) return false;
        return bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
    }

    private String extractAsciiStrings(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int printableCount = 0;
        for (byte b : bytes) {
            if ((b >= 32 && b <= 126) || b == '\n' || b == '\r' || b == '\t') {
                sb.append((char) b);
                printableCount++;
            } else if (printableCount > 0) {
                sb.append(' ');
                printableCount = 0;
            }
            if (sb.length() > 5000) break;
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }
}
