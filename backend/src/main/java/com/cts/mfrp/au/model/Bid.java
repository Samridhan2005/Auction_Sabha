package com.cts.mfrp.au.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "bids")
@Data
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bidId;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "bidder_id")
    private User bidder;

    private float bidAmount;
    private java.time.LocalDateTime bidTime;
}