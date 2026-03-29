package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Integer> {
}
