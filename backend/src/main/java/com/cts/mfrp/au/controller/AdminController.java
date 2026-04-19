package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auction")
public class AdminController {

    @Autowired
    private AuctionService auctionService;
    @Autowired
    private BroadcastWebSocketHandler broadcastWebSocketHandler;

    public AdminController(AuctionService auctionService, BroadcastWebSocketHandler broadcastWebSocketHandler) {
        this.auctionService = auctionService;
        this.broadcastWebSocketHandler = broadcastWebSocketHandler;
    }

    public AdminController() {
    }


    @PostMapping("/{auctionId}/start")
    public ResponseEntity<String> startAuction(@PathVariable int auctionId) throws Exception {

        Auction auction = auctionService.startAuction(auctionId);

        // Notify all clients
        broadcastWebSocketHandler.broadcastSystemEvent(
                "AUCTION_STARTED",
                auction.getAuctionId()
        );

        return ResponseEntity.ok("Auction started");
    }

    @PostMapping("/{auctionId}/stop")
    public ResponseEntity<String> stopAuction(@PathVariable int auctionId)
            throws Exception {

        Auction auction = auctionService.stopAuction(auctionId);

        broadcastWebSocketHandler.broadcastSystemEvent(
                "AUCTION_STOPPED",
                auction.getAuctionId()
        );

        return ResponseEntity.ok("Auction stopped");
    }

}
