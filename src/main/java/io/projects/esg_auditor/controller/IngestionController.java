package io.projects.esg_auditor.controller;

import io.projects.esg_auditor.service.IngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/pdf")
    public ResponseEntity<Void> ingest() throws IOException{
        ingestionService.ingestPdf();
        return ResponseEntity.accepted().build();
    }
}
