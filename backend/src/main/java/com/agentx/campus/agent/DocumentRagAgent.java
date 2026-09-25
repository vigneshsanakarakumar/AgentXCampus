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
        steps.add("Document/RAG Agent: Querying institutional document repository...");

        List<RagService.RagChunk> chunks = ragService.retrieveRelevantChunks(query, 3);
        steps.add(String.format("RAG Retrieval: Indexed %d relevant knowledge chunks", chunks.size()));

        if (chunks.isEmpty()) {
            steps.add("No matching document passages found in knowledge base.");
            long latency = System.currentTimeMillis() - startTime;
            return new AgentChatResponse(
                    "I searched the campus knowledge base and institutional regulations, but no specific policy or document directly answered: \"" + query + "\". Please verify with your department office or check active announcements.",
                    "Document/RAG Agent",
                    steps,
                    Map.of("ragFound", false),
                    latency
            );
        }

        StringBuilder context = new StringBuilder("VERIFIED INSTITUTIONAL KNOWLEDGE BASE PASSAGES:\n\n");
        List<String> sources = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            RagService.RagChunk c = chunks.get(i);
            sources.add(c.getCitation());
            context.append(String.format("[Source %d: %s]\n%s\n\n", i + 1, c.getCitation(), c.getContent()));
        }

        String prompt = "You are the Document/RAG Agent for AgentX Campus.\n"
                + "Answer the question based STRICTLY on the institutional knowledge base excerpts below.\n"
                + "Cite the document title and category clearly in your response.\n"
                + "If the answer is contained in the text, explain it clearly and cite the source.\n\n"
                + context.toString();

        steps.add("Generating synthesized response with source citation...");
        String answer = groqAiService.generateResponse(prompt, query);

        if (answer == null || answer.trim().isEmpty()) {
            // High quality fallback directly quoting the top retrieved chunk
            RagService.RagChunk top = chunks.get(0);
            StringBuilder sb = new StringBuilder();
            sb.append("According to **").append(top.getDocumentTitle()).append("** (Category: ").append(top.getCategory()).append("):\n\n");
            sb.append(top.getContent()).append("\n\n");
            sb.append("📌 *Source: ").append(top.getCitation()).append("*");
            answer = sb.toString();
        } else {
            // Append clean source reference footer
            answer = answer + "\n\n📌 **Verified Sources:**\n" + String.join("\n", sources.stream().map(s -> "• " + s).toList());
        }

        steps.add("✓ Cited " + sources.size() + " institutional knowledge document(s)");
        long latency = System.currentTimeMillis() - startTime;

        Map<String, Object> actionData = new HashMap<>();
        actionData.put("ragVerified", true);
        actionData.put("sources", sources);
        actionData.put("topDocument", chunks.get(0).getDocumentTitle());

        return new AgentChatResponse(answer, "Document/RAG Agent", steps, actionData, latency);
    }
}
