package com.cts.mfrp.au.service;

import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class BidTimerService {

    private static final long BID_TIMEOUT = 100;

    @Autowired
    private ScheduledExecutorService scheduler;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private WalletService walletService;

    private BroadcastWebSocketHandler broadcastWebSocketHandler;

    public BroadcastWebSocketHandler getBroadcastWebSocketHandler() {
        return broadcastWebSocketHandler;
    }

    public void setBroadcastWebSocketHandler(BroadcastWebSocketHandler broadcastWebSocketHandler) {
        this.broadcastWebSocketHandler = broadcastWebSocketHandler;
    }

    private ScheduledFuture<?> currentTimer;


    public synchronized void resetTimer(int auctionId) {
        if (currentTimer != null && !currentTimer.isDone()) {
            currentTimer.cancel(false);
        }
        currentTimer = scheduler.schedule(() -> executeStop(auctionId), BID_TIMEOUT, TimeUnit.SECONDS);
    }

    /** Called by the scheduler when slot end time is reached, bypassing the bid timer. */
    public synchronized void forceStop(int auctionId) {
        if (currentTimer != null && !currentTimer.isDone()) {
            currentTimer.cancel(false);
            currentTimer = null;
        }
        executeStop(auctionId);
    }

    private void executeStop(int auctionId) {
        try {
            auctionService.stopAuction(auctionId);
            walletService.commit(auctionId);
            broadcastWebSocketHandler.clearAuctionState();
            broadcastWebSocketHandler.broadcastSystemEvent("AUCTION_STOPPED", auctionId);
        } catch (Exception e) {
            System.out.println("Stop auction error: " + e.getMessage());
        }
    }



}
