package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.dto.DocumentVerifyRequest;
import com.cts.mfrp.au.service.DocumentAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:4200")
public class AiController {

    @Autowired
    private DocumentAiService documentAiService;

    @PostMapping("/verify-document")
    public ResponseEntity<?> verifyDocument(@RequestBody DocumentVerifyRequest request) {
        DocumentAiService.AiVerdictResult result = documentAiService.verifyDocument(
            request.getDocumentUrl(),
            request.getProductName(),
            request.getDescription()
        );
        return ResponseEntity.ok(result);
    }
}
