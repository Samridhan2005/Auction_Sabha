package com.cts.mfrp.au.service;

import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import com.cts.mfrp.au.model.Auction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionSchedulerService {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private BidTimerService bidTimerService;

    private BroadcastWebSocketHandler broadcastWebSocketHandler;

    public void setBroadcastWebSocketHandler(BroadcastWebSocketHandler handler) {
        this.broadcastWebSocketHandler = handler;
    }

    /** Every 30 seconds: auto-start CREATED auctions whose slot time has arrived. */
    @Scheduled(fixedRate = 30_000)
    public void autoStartAuctions() {
        if (broadcastWebSocketHandler == null) return;
        List<Auction> due = auctionService.findDueToStart();
        for (Auction a : due) {
            try {
                double price = auctionService.getDefaultPrice(a.getAuctionId());
                broadcastWebSocketHandler.initForAuction((float) price);
                auctionService.startAuction(a.getAuctionId());
                broadcastWebSocketHandler.broadcastSystemEvent("AUCTION_STARTED", a.getAuctionId());
                System.out.println("Auto-started auction #" + a.getAuctionId());
            } catch (Exception e) {
                System.out.println("Auto-start failed for auction #" + a.getAuctionId() + ": " + e.getMessage());
            }
        }
    }

    /** Every 30 seconds: force-stop LIVE auctions whose slot end time has passed. */
    @Scheduled(fixedRate = 30_000)
    public void autoStopAuctions() {
        if (broadcastWebSocketHandler == null) return;
        List<Auction> expired = auctionService.findDueToEnd();
        for (Auction a : expired) {
            try {
                bidTimerService.forceStop(a.getAuctionId());
                System.out.println("Auto-stopped auction #" + a.getAuctionId() + " (slot expired)");
            } catch (Exception e) {
                System.out.println("Auto-stop failed for auction #" + a.getAuctionId() + ": " + e.getMessage());
            }
        }
    }
}
