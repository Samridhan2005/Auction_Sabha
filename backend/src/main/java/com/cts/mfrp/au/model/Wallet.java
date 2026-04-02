package com.cts.mfrp.au.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int walletId;

    private int userId;
    private float availableBalance;
    private float frozenBalance;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    // Getters and Setters
    public int getWalletId() { return walletId; }
    public void setWalletId(int walletId) { this.walletId = walletId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public float getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(float availableBalance) { this.availableBalance = availableBalance; }

    public float getFrozenBalance() { return frozenBalance; }
    public void setFrozenBalance(float frozenBalance) { this.frozenBalance = frozenBalance; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }
}
