package io.projects.esg_auditor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;

@Configuration
public class ESGDocumentIngestionConfig implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ESGDocumentIngestionConfig.class);
    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourcePatternResolver;

    public ESGDocumentIngestionConfig(VectorStore vectorStore, ResourcePatternResolver resourcePatternResolver) {
        this.vectorStore = vectorStore;
        this.resourcePatternResolver = resourcePatternResolver;
    }


    @Override
    public void run(String... args) throws Exception {
        log.info("Started Ingesting Reports");
        Resource[] resources = resourcePatternResolver.getResources("classpath:data/*.pdf");

        for (Resource resource: resources){
            log.info(resource.getFilename());
            TikaDocumentReader documentReader = new TikaDocumentReader(resource);
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            vectorStore.accept(textSplitter.split(documentReader.read()));
            log.info(vectorStore.toString());
        }

        log.info("Finished Ingesting Reports");

    }
}
