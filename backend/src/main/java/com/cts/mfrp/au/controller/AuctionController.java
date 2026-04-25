package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

//@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/api/auction")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAuctions() {
        return ResponseEntity.ok(auctionService.getAuctionsWithDetails());
    }
}
