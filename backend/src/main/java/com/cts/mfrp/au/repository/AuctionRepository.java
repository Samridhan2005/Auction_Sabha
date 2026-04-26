package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Integer> {

    List<Auction> findByIsFeaturedTrueAndStatus(String status);

    @Query("SELECT a FROM Auction a " +
           "JOIN FETCH a.product p " +
           "JOIN FETCH p.category " +
           "JOIN FETCH p.seller " +
           "LEFT JOIN FETCH a.highestBidder " +
           "WHERE a.isFeatured = true " +
           "ORDER BY a.auctionId DESC")
    List<Auction> findAllFeaturedWithDetails();
}
