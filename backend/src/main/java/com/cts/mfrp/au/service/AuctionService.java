package com.cts.mfrp.au.service;

import com.cts.mfrp.au.exception.AuctionCannotStartException;
import com.cts.mfrp.au.exception.AuctionCannotStopException;
import com.cts.mfrp.au.exception.AuctionNotFoundException;
import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.model.Product;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuctionService {
    @Autowired
    private AuctionRepository auctionRepository;

    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public void startAuction(int auctionId) throws Exception {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found"));
        if (!"CREATED".equals(auction.getStatus())) {
            throw new AuctionCannotStartException("Auction cannot be started");
        }

        auction.setStatus("LIVE");
        auction.setStartTime(LocalDateTime.now());

        auctionRepository.save(auction);
    }

    public void stopAuction(int auctionId) throws Exception {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found"));

        if (!"LIVE".equals(auction.getStatus())) {
            throw new AuctionCannotStopException("Auction is not live");
        }

        auction.setStatus("ENDED");
        auction.setEndTime(LocalDateTime.now());

        auctionRepository.save(auction);
    }

    public boolean isAuctionLive(Auction auction) {
        return "LIVE".equals(auction.getStatus());
    }


    public double getDefaultPrice(int auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found"));
        return auction.getProduct().getStartingPrice();
    }

    public Auction findById(int id){
        Optional<Auction> op= auctionRepository.findById(id);
        return op.orElse(null);
    }

    public void setHighestBidder(int auctionId, User u){
        Auction auction =auctionRepository.findById(auctionId).orElseThrow(() -> new AuctionNotFoundException("Auction not found"));
        auction.setHighestBidder(u);
        auctionRepository.save(auction);
    }

    public List<Map<String, Object>> getAuctionsWithDetails() {
        List<Auction> auctions = auctionRepository.findAllFeaturedWithDetails();
        return auctions.stream().map(a -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("auctionId", a.getAuctionId());
            map.put("status", a.getStatus());
            map.put("currentBid", a.getCurrentBid());
            map.put("isFeatured", a.isFeatured());
            map.put("startTime", a.getStartTime() != null ? a.getStartTime().toString() : null);
            map.put("endTime", a.getEndTime() != null ? a.getEndTime().toString() : null);
            map.put("highestBidder", a.getHighestBidder() != null ? a.getHighestBidder().getName() : null);

            Product p = a.getProduct();
            if (p != null) {
                map.put("productId", p.getProductId());
                map.put("productName", p.getProductName());
                map.put("description", p.getDescription());
                String img = p.getImageUrl();
                map.put("imageUrl", img != null && !img.isBlank() ? img : null);
                map.put("startingPrice", p.getStartingPrice());
                map.put("sellerName", p.getSeller() != null ? p.getSeller().getName() : "Unknown");
                map.put("categoryId", p.getCategory() != null ? p.getCategory().getCategoryId() : null);
                map.put("categoryName", p.getCategory() != null ? p.getCategory().getCategoryName() : "Uncategorized");
            }
            return map;
        }).collect(Collectors.toList());
    }
}
