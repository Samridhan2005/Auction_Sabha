package com.cts.mfrp.au.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "auctions")
@Data
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int auctionId;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private float currentBid;

    @ManyToOne
    @JoinColumn(name = "highest_bidder_id")
    private User highestBidder;

    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
    private String status;
    private boolean isFeatured;
}