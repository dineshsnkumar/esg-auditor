package io.projects.esg_auditor.config;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IngestionConfig {
    @Bean("ingestionTextSplitter")
    public TextSplitter textSplitter() {
        int defaultChunkSize = 500;
        int minChunkSizeChars = 350; // Minimum characters per chunk
        int minChunkLengthToEmbed = 5; // Min characters to include a chunk
        int maxNumChunks = 10000; // Max chunks per document
        boolean keepSeparator = true;
        return new TokenTextSplitter(
                defaultChunkSize,
                minChunkSizeChars,
                minChunkLengthToEmbed,
                maxNumChunks,
                keepSeparator);

    }
}
