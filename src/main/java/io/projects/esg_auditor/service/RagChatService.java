package io.projects.esg_auditor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagChatService {
    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);

    private ChatClient.Builder chatClientBuilder;
    private RetrievalService retrievalService;

    public RagChatService(RetrievalService retrievalService, ChatClient.Builder chatClientBuilder) {
        this.retrievalService = retrievalService;
        this.chatClientBuilder = chatClientBuilder;
    }

    private static final String SYSTEM_PROMPT = """
            You are an AI assistant answering questions about a document.
            
            NON-NEGOTIABLE RULES:
            - Use ONLY the information in the Context.
            - Do NOT summarize sections or describe structure.
            - Do NOT infer, assume, or generalize.
            - Answer ONLY what is explicitly stated.
            - Every claim MUST be supported by a page number.
            - If no explicit targets are stated, you MUST say:
              "The document does not specify any renewable energy targets."
            
            OUTPUT FORMAT (STRICT):
            Answer:
            <one or two factual sentences>
            
            Evidence:
            - Page <page_number>: <short quote or paraphrase>
            
            If you cannot cite a page number, do not answer the question.
            
            """;

    public String chat(String userQuestion){
        log.info("Processing question: {}", userQuestion);

        // 1. Retrieve relevant documents from the vector store
        List<Document> relevantDocuments = retrievalService.retrieveDocuments(userQuestion, 7);

        if (relevantDocuments.isEmpty()) {
            return "I don't have any relevant information in the ESG documents to answer this question.";
        }

        log.info("Relevant documents: {}", relevantDocuments);

        // 2. Build Context
        String context = relevantDocuments.stream()
                .map(doc -> {
                    Object page = doc.getMetadata().get("page");
                    return "Page " + page + ":\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n"));

        log.info("Context: {}", context);

        // 3. Generate response with Mistral LLM
        return generateResponse(userQuestion, context);
    }

    private String generateResponse(String userQuestion, String context) {
        // Create system message with context
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", context));


        UserMessage userMessage = new UserMessage(userQuestion);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Get response from Mistral
        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt(prompt)
                .call()
                .content();
    }


}
