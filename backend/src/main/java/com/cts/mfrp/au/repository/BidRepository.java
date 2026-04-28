package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Integer> {

    // Returns [bidder name, max bid amount] sorted descending — top bidders per auction
    @Query("SELECT b.bidder.name, MAX(b.bidAmount) FROM Bid b WHERE b.auction.auctionId = :auctionId GROUP BY b.bidder.userId ORDER BY MAX(b.bidAmount) DESC")
    List<Object[]> findLeaderboard(@Param("auctionId") int auctionId);

    // Returns [auctionId, maxBidAmount] for every auction a buyer participated in
    @Query("SELECT b.auction.auctionId, MAX(b.bidAmount) FROM Bid b WHERE b.bidder.userId = :buyerId GROUP BY b.auction.auctionId ORDER BY MAX(b.bidAmount) DESC")
    List<Object[]> findBuyerBidSummary(@Param("buyerId") int buyerId);

    // Total bid count for a single auction
    long countByAuction_AuctionId(int auctionId);

    // Batch: [auctionId, count] for all auctions — avoids N+1 queries
    @Query("SELECT b.auction.auctionId, COUNT(b) FROM Bid b GROUP BY b.auction.auctionId")
    List<Object[]> countBidsPerAuction();
}
