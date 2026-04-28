package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.dto.VerifierApplicationRequest;
import com.cts.mfrp.au.dto.VerifierApprovalResponse;
import com.cts.mfrp.au.model.VerifierApplication;
import com.cts.mfrp.au.service.VerifierApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/verifier-applications")
@CrossOrigin(origins = "http://localhost:4200")
public class VerifierApplicationController {

    @Autowired
    private VerifierApplicationService service;

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody VerifierApplicationRequest request) {
        try {
            VerifierApplication saved = service.submitApplication(request);
            return ResponseEntity.ok("Application submitted successfully! ID: " + saved.getApplicationId());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<VerifierApplication>> getAll() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<VerifierApplication>> getPending() {
        return ResponseEntity.ok(service.getPendingApplications());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                     @RequestParam(defaultValue = "") String remarks) {
        try {
            VerifierApprovalResponse response = service.approveApplication(id, remarks);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                    @RequestParam(defaultValue = "") String remarks) {
        try {
            VerifierApplication updated = service.rejectApplication(id, remarks);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
