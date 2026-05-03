package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auction")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAuctions() {
        return ResponseEntity.ok(auctionService.getAuctionsWithDetails());
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(@PathVariable int id) {
        return ResponseEntity.ok(auctionService.getLeaderboard(id));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<Map<String, Object>>> getSlots(@RequestParam String date) {
        return ResponseEntity.ok(auctionService.getSlotAvailability(date));
    }
}
