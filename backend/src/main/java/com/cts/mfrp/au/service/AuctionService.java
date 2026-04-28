package com.cts.mfrp.au.service;

import com.cts.mfrp.au.exception.AuctionCannotStartException;
import com.cts.mfrp.au.exception.AuctionCannotStopException;
import com.cts.mfrp.au.exception.AuctionNotFoundException;
import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.model.Product;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.repository.AuctionRepository;
import com.cts.mfrp.au.repository.BidRepository;
import com.cts.mfrp.au.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuctionService {
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private ProductRepository productRepository;

    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public void startAuction(int auctionId) throws Exception {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found"));
        if (!"CREATED".equals(auction.getStatus())) {
            throw new AuctionCannotStartException("Auction cannot be started");
        }
        if (auction.getProduct() == null || !"APPROVED".equals(auction.getProduct().getVerificationStatus())) {
            throw new AuctionCannotStartException("Product has not been verified by a verifier yet");
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
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new AuctionNotFoundException("Auction not found"));
        auction.setHighestBidder(u);
        auctionRepository.save(auction);
    }

    public void updateBid(int auctionId, User highestBidder, float currentBid) {
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new AuctionNotFoundException("Auction not found"));
        auction.setHighestBidder(highestBidder);
        auction.setCurrentBid(currentBid);
        auctionRepository.save(auction);
    }

    // ── Leaderboard ──────────────────────────────────────────────────────────
    public List<Map<String, Object>> getLeaderboard(int auctionId) {
        List<Object[]> rows = bidRepository.findLeaderboard(auctionId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rank", i + 1);
            entry.put("bidder", rows.get(i)[0]);
            entry.put("maxBid", rows.get(i)[1]);
            result.add(entry);
        }
        return result;
    }

    // ── Slot Availability ─────────────────────────────────────────────────────
    private static final int BUSINESS_START = 9;
    private static final int SLOT_COUNT     = 10;

    public List<Map<String, Object>> getSlotAvailability(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> slots = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            int hour = BUSINESS_START + i;
            LocalDateTime start = date.atTime(hour, 0);
            LocalDateTime end   = start.plusHours(1);

            String availability;
            if (end.isBefore(now) || end.isEqual(now)) {
                // Slot has already ended (or ends exactly now) — past, can't book
                availability = "UNAVAILABLE";
            } else if (!auctionRepository.findConflictingSlots(start, end).isEmpty()) {
                availability = "UNAVAILABLE";
            } else {
                int tentative = productRepository
                        .countByVerificationStatusAndPreferredDateAndPreferredSlot("PENDING", date, i + 1);
                availability = tentative > 0 ? "TENTATIVE" : "AVAILABLE";
            }

            Map<String, Object> slot = new LinkedHashMap<>();
            slot.put("slot", i + 1);
            slot.put("label", String.format("Slot %d — %02d:00–%02d:00", i + 1, hour, hour + 1));
            slot.put("availability", availability);
            slots.add(slot);
        }
        return slots;
    }

    // ── Scheduler helpers ─────────────────────────────────────────────────────
    public List<Auction> findDueToStart() {
        return auctionRepository.findDueToStart(LocalDateTime.now());
    }

    public List<Auction> findDueToEnd() {
        return auctionRepository.findDueToEnd(LocalDateTime.now());
    }

    public List<Map<String, Object>> getAuctionsWithDetails() {
        List<Auction> auctions = auctionRepository.findAllFeaturedWithDetails();

        // Build bidCount lookup in one query (avoids N+1)
        Map<Integer, Long> bidCountMap = new java.util.HashMap<>();
        for (Object[] row : bidRepository.countBidsPerAuction()) {
            bidCountMap.put((Integer) row[0], (Long) row[1]);
        }

        return auctions.stream().map(a -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("auctionId", a.getAuctionId());
            map.put("status", a.getStatus());
            map.put("currentBid", a.getCurrentBid());
            map.put("isFeatured", a.isFeatured());
            map.put("startTime", a.getStartTime() != null ? a.getStartTime().toString() : null);
            map.put("endTime", a.getEndTime() != null ? a.getEndTime().toString() : null);
            map.put("confirmedStartTime", a.getConfirmedStartTime() != null ? a.getConfirmedStartTime().toString() : null);
            map.put("slotEndTime", a.getSlotEndTime() != null ? a.getSlotEndTime().toString() : null);
            map.put("highestBidder", a.getHighestBidder() != null ? a.getHighestBidder().getName() : null);
            map.put("bidCount", bidCountMap.getOrDefault(a.getAuctionId(), 0L));

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
