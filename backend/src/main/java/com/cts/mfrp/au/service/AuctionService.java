package com.cts.mfrp.au.service;

import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuctionService {
    @Autowired
    private AuctionRepository auctionRepository;


    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }


    public void startAuction(int auctionId) throws Exception {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (!"CREATED".equals(auction.getStatus())) {
            throw new IllegalStateException("Auction cannot be started");
        }

        auction.setStatus("LIVE");
        auction.setStartTime(LocalDateTime.now());

        auctionRepository.save(auction);
    }

    public void stopAuction(int auctionId) throws Exception {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (!"LIVE".equals(auction.getStatus())) {
            throw new IllegalStateException("Auction is not live");
        }

        auction.setStatus("CLOSED");
        auction.setEndTime(LocalDateTime.now());

        auctionRepository.save(auction);
    }

    public boolean isAuctionLive(Auction auction) {

        return "LIVE".equals(auction.getStatus());
    }


    public double getDefaultPrice(int auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        return auction.getProduct().getStartingPrice();
    }

    public Auction findById(int id){
        Optional<Auction> op= auctionRepository.findById(id);
        return op.orElse(null);
    }

    public void setHighestBidder(int auctionId, User u){
        Auction auction =auctionRepository.findById(auctionId).orElseThrow(() -> new RuntimeException("Auction not found"));
        auction.setHighestBidder(u);
        auctionRepository.save(auction);
    }

}
