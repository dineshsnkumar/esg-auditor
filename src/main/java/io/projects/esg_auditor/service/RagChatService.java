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
            You are an ESG (Environmental, Social, Governance) compliance expert assistant.
            Your role is to analyze sustainability reports and answer questions based on the provided context.
            
            Guidelines:
            1. Answer questions based ONLY on the provided context from the documents
            2. If the answer is not in the context, clearly state that you don't have that information
            3. Cite specific sections or page numbers when available in the metadata
            4. Focus on accuracy and compliance with EU sustainability reporting standards (CSRD, ESRS)
            5. Provide structured, clear responses
                
            Context from ESG Documents:
            {context}
            """;

    public String chat(String userQuestion){
        log.info("Processing question: {}", userQuestion);

        // 1. Retrieve relevant documents from the vector store
        List<Document> relevantDocuments = retrievalService.retrieveDocuments(userQuestion, 5);

        if (relevantDocuments.isEmpty()) {
            return "I don't have any relevant information in the ESG documents to answer this question.";
        }

        // 2. Build Context
        String context = relevantDocuments.stream()
                .map(doc -> {
                    Object page = doc.getMetadata().get("page");
                    return "Page " + page + ":\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n"));

        // 3. Generate response with Mistral LLM
        return generateResponse(userQuestion, context);
    }

    private String generateResponse(String userQuestion, String context) {
        // Create system message with context
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", context));

        // Create user message
        UserMessage userMessage = new UserMessage(userQuestion);

        // Build prompt with both messages
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Get response from Mistral
        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt(prompt)
                .call()
                .content();
    }


}
