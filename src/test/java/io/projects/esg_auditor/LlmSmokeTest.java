package io.projects.esg_auditor;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mistral")
public class LlmSmokeTest {

    private final ChatClient chatClient;

    public LlmSmokeTest(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Test
    void mistralConnectionWorks(){
        var response = chatClient.prompt("ESG audit system online").call();
        assertThat(response).isNotNull();
    }
}
