package com.cts.mfrp.au.service;

import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.model.Product;
import com.cts.mfrp.au.model.Bid;
import com.cts.mfrp.au.repository.AuctionRepository;
import com.cts.mfrp.au.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BidService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    public void insertBid(Bid b) {
        bidRepository.save(b);
    }

    public List<Map<String, Object>> getBuyerBidHistory(int buyerId) {
        List<Object[]> summary = bidRepository.findBuyerBidSummary(buyerId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : summary) {
            int auctionId = ((Number) row[0]).intValue();
            float myMaxBid = ((Number) row[1]).floatValue();

            auctionRepository.findById(auctionId).ifPresent(auction -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("auctionId", auctionId);
                entry.put("myMaxBid", myMaxBid);
                entry.put("auctionStatus", auction.getStatus());
                entry.put("finalBid", auction.getCurrentBid());

                boolean won = "ENDED".equals(auction.getStatus())
                        && auction.getHighestBidder() != null
                        && auction.getHighestBidder().getUserId() == buyerId;
                entry.put("won", won);
                entry.put("winner", auction.getHighestBidder() != null
                        ? auction.getHighestBidder().getName() : null);

                Product p = auction.getProduct();
                if (p != null) {
                    entry.put("productName", p.getProductName());
                    entry.put("description", p.getDescription());
                    entry.put("imageUrl", p.getImageUrl());
                    entry.put("startingPrice", p.getStartingPrice());
                    entry.put("sellerName", p.getSeller() != null ? p.getSeller().getName() : "Unknown");
                    entry.put("categoryName", p.getCategory() != null
                            ? p.getCategory().getCategoryName() : "Uncategorized");
                }
                result.add(entry);
            });
        }
        return result;
    }
}
