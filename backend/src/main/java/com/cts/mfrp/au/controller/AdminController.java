package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.service.AuctionService;
import com.cts.mfrp.au.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/api/admin/auction")
public class AdminController {

    @Autowired
    private AuctionService auctionService;
    @Autowired
    private BroadcastWebSocketHandler broadcastWebSocketHandler;
    @Autowired
    private WalletService walletService;

    public AdminController(AuctionService auctionService, BroadcastWebSocketHandler broadcastWebSocketHandler) {
        this.auctionService = auctionService;
        this.broadcastWebSocketHandler = broadcastWebSocketHandler;
    }

    public AdminController() {
    }


    @PostMapping("/{auctionId}/start")
    public ResponseEntity<String> startAuction(@PathVariable int auctionId) throws Exception {
        double price=auctionService.getDefaultPrice(auctionId);
        broadcastWebSocketHandler.setCurBid((float) price);
        auctionService.startAuction(auctionId);
        broadcastWebSocketHandler.broadcastSystemEvent("AUCTION_STARTED",auctionId);
        return ResponseEntity.ok("Auction started");
    }

    @PostMapping("/{auctionId}/stop")
    public ResponseEntity<String> stopAuction(@PathVariable int auctionId) throws Exception {
        auctionService.stopAuction(auctionId);
        walletService.commit(auctionId);
        broadcastWebSocketHandler.broadcastSystemEvent("AUCTION_STOPPED",auctionId);
        return ResponseEntity.ok("Auction stopped");
    }

}
