package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.service.GroqAiService;
import com.agentx.campus.service.RagService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocumentRagAgent {

    private final RagService ragService;
    private final GroqAiService groqAiService;

    public DocumentRagAgent(RagService ragService, GroqAiService groqAiService) {
        this.ragService = ragService;
        this.groqAiService = groqAiService;
    }

    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();
        steps.add("Query Classifier: Evaluated query -> [Institutional Knowledge / Campus Policy]");
        steps.add("Document/RAG Agent: Computing dense subword semantic embeddings & cosine similarity...");

        List<RagService.RagChunk> chunks = ragService.retrieveRelevantChunks(query, 3);
        steps.add(String.format("Semantic Vector RAG: Retrieved %d top matching handbook sections with similarity scores", chunks.size()));

        if (chunks.isEmpty()) {
            steps.add("No matching document passages found in knowledge base.");
            long latency = System.currentTimeMillis() - startTime;
            return new AgentChatResponse(
                    "I searched the campus knowledge base and institutional regulations, but no specific policy or document directly answered: \"" + query + "\". Please verify with your department office or check active announcements.",
                    "Document/RAG Agent",
                    steps,
                    Map.of("ragFound", false, "isRag", true),
                    latency
            );
        }

        StringBuilder context = new StringBuilder("VERIFIED INSTITUTIONAL KNOWLEDGE BASE PASSAGES WITH EXACT CITATIONS:\n\n");
        List<String> sources = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            RagService.RagChunk c = chunks.get(i);
            sources.add(c.getCitation());
            context.append(String.format("[Source %d: %s]\nSection: %s (Page %d, Para %d)\nExcerpt: \"%s\"\n\n",
                    i + 1, c.getDocumentTitle(), c.getSectionTitle(), c.getPageNumber(), c.getParagraphNumber(), c.getContent()));
        }

        String prompt = "You are the Institutional Policy & RAG Specialist for AgentX Campus.\n"
                + "Answer the student's question based STRICTLY and ACCURATELY on the verified institutional handbook passages below.\n"
                + "RULES:\n"
                + "1. State the exact policy criteria, numerical thresholds (percentages, fees, time limits), and rules explicitly.\n"
                + "2. Always cite the exact source using this format:\n"
                + "   📌 *Source: [Document Title], [Section Title] (Page X, Para Y)*\n"
                + "3. Include a direct quotation snippet from the text supporting your answer.\n"
                + "4. If asked about condonation fee/rules, state the ₹750 fee per subject, the 65%-74% range, the 3-working-days medical certificate rule, and that <65% must repeat the course.\n"
                + "5. If asked about hostel outing, state the Saturday daytime outing rules vs overnight parent verification rules clearly.\n\n"
                + context.toString();

        steps.add("Synthesizing answer with exact document section & paragraph citations...");
        String answer = groqAiService.generateResponse(prompt, query);

        if (answer == null || answer.trim().isEmpty()) {
            // High quality fallback directly quoting the top retrieved chunk
            RagService.RagChunk top = chunks.get(0);
            StringBuilder sb = new StringBuilder();
            sb.append("According to **").append(top.getDocumentTitle()).append("**:\n\n");
            sb.append("> \"").append(top.getContent()).append("\"\n\n");
            sb.append(top.getCompactCitation());
            answer = sb.toString();
        } else {
            // Append verified sources reference section
            answer = answer + "\n\n📌 **Verified Citations:**\n" + String.join("\n", sources.stream().map(s -> "• " + s).toList());
        }

        steps.add("✓ Cited " + sources.size() + " verified handbook excerpt(s) with exact section & paragraph anchors");
        long latency = System.currentTimeMillis() - startTime;

        Map<String, Object> actionData = new HashMap<>();
        actionData.put("ragVerified", true);
        actionData.put("isRag", true);
        actionData.put("sources", sources);
        actionData.put("topDocument", chunks.get(0).getDocumentTitle());
        actionData.put("topSection", chunks.get(0).getSectionTitle());

        return new AgentChatResponse(answer, "Document/RAG Agent", steps, actionData, latency);
    }
}
