package com.cts.mfrp.au.service;

import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuctionService {
    @Autowired
    private AuctionRepository auctionRepository;

    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public Auction startAuction(int auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (!"CREATED".equals(auction.getStatus())) {
            throw new IllegalStateException("Auction cannot be started");
        }

        auction.setStatus("LIVE");
        auction.setStartTime(LocalDateTime.now());

        return auctionRepository.save(auction);
    }

    public Auction stopAuction(int auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (!"LIVE".equals(auction.getStatus())) {
            throw new IllegalStateException("Auction is not live");
        }

        auction.setStatus("CLOSED");
        auction.setEndTime(LocalDateTime.now());

        return auctionRepository.save(auction);
    }

    public boolean isAuctionLive(Auction auction) {
        return "LIVE".equals(auction.getStatus());
    }


}
