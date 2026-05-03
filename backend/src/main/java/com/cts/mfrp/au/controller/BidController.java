package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bids")
public class BidController {

    @Autowired
    private BidService bidService;

    @GetMapping("/my-bids")
    public ResponseEntity<List<Map<String, Object>>> getMyBids(@RequestParam int buyerId) {
        return ResponseEntity.ok(bidService.getBuyerBidHistory(buyerId));
    }
}
