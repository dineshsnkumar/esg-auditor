package io.projects.esg_auditor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IngestionService {
    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private final TextSplitter textSplitter;
    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourcePatternResolver;

    public IngestionService( TextSplitter textSplitter, VectorStore vectorStore, ResourcePatternResolver resourcePatternResolver) {
        this.textSplitter = textSplitter;
        this.vectorStore = vectorStore;
        this.resourcePatternResolver = resourcePatternResolver;
    }



    public void ingestPdf()  {
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:data/*.pdf");
            for (Resource resource : resources) {
                log.info("Started Ingesting: {}", resource.getFilename());
                PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
                List<Document> pages = reader.read();
                List<Document> chunks = textSplitter.split(pages);
                String documentId = UUID.randomUUID().toString();

                List<Document> documentWithMetaData = chunks.stream().map(doc -> {
                    Map<String, Object> metadata = new HashMap<>(doc.getMetadata());

                    metadata.put("document_id", documentId);
                    metadata.put("document_name", resource.getFilename());
                    metadata.put("company", "Apple Inc.");
                    metadata.put("report_year", 2025);

                    metadata.put("pillar", "E");
                    metadata.put("topic", "Climate Emissions");

                    metadata.put("source_type", "pdf");
                    metadata.put("ingested_at", Instant.now().toString());

                    Object pageNum = doc.getMetadata() != null ? doc.getMetadata().get("page") : null;
                    if (pageNum != null) {
                        metadata.put("page", pageNum);
                    } else {
                        metadata.put("page", 0);
                    }
                    // Replace null with empty text to prevent PGVector throwing an error
                    // TODO: Need to Fix this
                    String cleanedText = doc.getText() != null ?
                            doc.getText().replace("\u0000", "").replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "") :
                            "";

                    return Document.builder()
                            .id(doc.getId())
                            .text(cleanedText)
                            .metadata(metadata)
                            .build();
                }).toList();

                vectorStore.add(documentWithMetaData);
                log.info("Completed Ingesting: {}", resource.getFilename());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
