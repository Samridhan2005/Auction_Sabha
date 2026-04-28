package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import com.cts.mfrp.au.service.AuctionService;
import com.cts.mfrp.au.service.ProductService;
import com.cts.mfrp.au.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private AuctionService             auctionService;
    @Autowired private BroadcastWebSocketHandler  broadcastWebSocketHandler;
    @Autowired private WalletService              walletService;
    @Autowired private ProductService             productService;

    @PostMapping("/auction/{auctionId}/start")
    public ResponseEntity<String> startAuction(@PathVariable int auctionId) throws Exception {
        double price = auctionService.getDefaultPrice(auctionId);
        broadcastWebSocketHandler.setCurBid((float) price);
        broadcastWebSocketHandler.setPrevUserId(0);
        auctionService.startAuction(auctionId);
        broadcastWebSocketHandler.broadcastSystemEvent("AUCTION_STARTED", auctionId);
        return ResponseEntity.ok("Auction started");
    }

    @PostMapping("/auction/{auctionId}/stop")
    public ResponseEntity<String> stopAuction(@PathVariable int auctionId) throws Exception {
        auctionService.stopAuction(auctionId);
        walletService.commit(auctionId);
        broadcastWebSocketHandler.broadcastSystemEvent("AUCTION_STOPPED", auctionId);
        return ResponseEntity.ok("Auction stopped");
    }

    @GetMapping("/products")
    public ResponseEntity<List<Map<String, Object>>> getProductRegistry() {
        return ResponseEntity.ok(productService.getAllProductsForAdmin());
    }
}
