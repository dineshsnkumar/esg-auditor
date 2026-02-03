package io.projects.esg_auditor.controller;

import io.projects.esg_auditor.dto.ChatRequest;
import io.projects.esg_auditor.service.RagChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final RagChatService ragChatService;

    public ChatController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }
    @PostMapping
    public String chat(@RequestBody ChatRequest chatRequest){
        log.info("User Question {}", chatRequest);
        String userQuestion = chatRequest.getUserRequest();
        return ragChatService.chat(userQuestion);
    }
}
