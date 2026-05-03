package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Integer> {

    List<Auction> findByIsFeaturedTrueAndStatus(String status);

    Optional<Auction> findByProduct_ProductId(int productId);

    @Query("SELECT a FROM Auction a WHERE a.confirmedStartTime < :slotEnd AND a.slotEndTime > :slotStart AND a.status != 'ENDED'")
    List<Auction> findConflictingSlots(@Param("slotStart") LocalDateTime slotStart,
                                       @Param("slotEnd") LocalDateTime slotEnd);

    // Scheduler: CREATED auctions whose confirmed start time has passed
    @Query("SELECT a FROM Auction a WHERE a.status = 'CREATED' AND a.confirmedStartTime IS NOT NULL AND a.confirmedStartTime <= :now")
    List<Auction> findDueToStart(@Param("now") LocalDateTime now);

    // Scheduler: LIVE auctions whose slot end time has passed
    @Query("SELECT a FROM Auction a WHERE a.status = 'LIVE' AND a.slotEndTime IS NOT NULL AND a.slotEndTime <= :now")
    List<Auction> findDueToEnd(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Auction a " +
           "JOIN FETCH a.product p " +
           "JOIN FETCH p.category " +
           "JOIN FETCH p.seller " +
           "LEFT JOIN FETCH a.highestBidder " +
           "WHERE a.isFeatured = true " +
           "ORDER BY a.auctionId DESC")
    List<Auction> findAllFeaturedWithDetails();

    // Admin: all auctions with product/seller eagerly loaded (avoids N+1)
    @Query("SELECT a FROM Auction a " +
           "JOIN FETCH a.product p " +
           "JOIN FETCH p.seller " +
           "LEFT JOIN FETCH a.highestBidder " +
           "ORDER BY a.auctionId DESC")
    List<Auction> findAllWithProduct();
}
