package com.cts.mfrp.au.service;

import com.cts.mfrp.au.model.Wallet;
import com.cts.mfrp.au.model.Transaction;
import com.cts.mfrp.au.repository.WalletRepository;
import com.cts.mfrp.au.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WalletService {
    private final WalletRepository walletRepo;
    private final TransactionRepository transactionRepo;

    public WalletService(WalletRepository walletRepo, TransactionRepository transactionRepo) {
        this.walletRepo = walletRepo;
        this.transactionRepo = transactionRepo;
    }

    // View Balance
    public Wallet getBalance(int userId) {
        return walletRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));
    }

    // Deposit Funds
    public Wallet depositFunds(int userId, float amount) {
        Wallet wallet = walletRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));

        wallet.setAvailableBalance(wallet.getAvailableBalance() + amount);
        wallet.setLastUpdated(new Date());
        walletRepo.save(wallet);

        Transaction tx = new Transaction();
        tx.setWalletId(wallet.getWalletId());
        tx.setType("deposit");
        tx.setAmount(amount);
        tx.setStatus("SUCCESS");
        tx.setCreatedAt(new Date());
        transactionRepo.save(tx);

        return wallet;
    }

    // Withdraw Funds
    public Wallet withdrawFunds(int userId, float amount) {
        Wallet wallet = walletRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));

        if (wallet.getAvailableBalance() < amount) {
            throw new RuntimeException("Insufficient balance to withdraw");
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance() - amount);
        wallet.setLastUpdated(new Date());
        walletRepo.save(wallet);

        Transaction tx = new Transaction();
        tx.setWalletId(wallet.getWalletId());
        tx.setType("withdrawal");
        tx.setAmount(amount);
        tx.setStatus("SUCCESS");
        tx.setCreatedAt(new Date());
        transactionRepo.save(tx);

        return wallet;
    }

    // Hold Bid Amount
    public Wallet holdBidAmount(int userId, int auctionId, float bidAmount) {
        Wallet wallet = walletRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));

        if (wallet.getAvailableBalance() < bidAmount) {
            throw new RuntimeException("Insufficient balance to place bid");
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance() - bidAmount);
        wallet.setFrozenBalance(wallet.getFrozenBalance() + bidAmount);
        wallet.setLastUpdated(new Date());
        walletRepo.save(wallet);

        Transaction tx = new Transaction();
        tx.setWalletId(wallet.getWalletId());
        tx.setAuctionId(auctionId);
        tx.setType("bid_hold");
        tx.setAmount(bidAmount);
        tx.setStatus("HELD");
        tx.setCreatedAt(new Date());
        transactionRepo.save(tx);

        return wallet;
    }

    // Refund Losing Bidder
    public Wallet refundBidder(int userId, int auctionId, float refundAmount) {
        Wallet wallet = walletRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));

        if (wallet.getFrozenBalance() < refundAmount) {
            throw new RuntimeException("No sufficient frozen balance to refund");
        }

        wallet.setFrozenBalance(wallet.getFrozenBalance() - refundAmount);
        wallet.setAvailableBalance(wallet.getAvailableBalance() + refundAmount);
        wallet.setLastUpdated(new Date());
        walletRepo.save(wallet);

        Transaction tx = new Transaction();
        tx.setWalletId(wallet.getWalletId());
        tx.setAuctionId(auctionId);
        tx.setType("bid_refund");
        tx.setAmount(refundAmount);
        tx.setStatus("REFUNDED");
        tx.setCreatedAt(new Date());
        transactionRepo.save(tx);

        return wallet;
    }

    // Payout Winning Seller
    public Wallet payoutSeller(int sellerUserId, int auctionId, float payoutAmount) {
        Wallet wallet = walletRepo.findByUserId(sellerUserId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for seller userId: " + sellerUserId));

        wallet.setAvailableBalance(wallet.getAvailableBalance() + payoutAmount);
        wallet.setLastUpdated(new Date());
        walletRepo.save(wallet);

        Transaction tx = new Transaction();
        tx.setWalletId(wallet.getWalletId());
        tx.setAuctionId(auctionId);
        tx.setType("payout");
        tx.setAmount(payoutAmount);
        tx.setStatus("SUCCESS");
        tx.setCreatedAt(new Date());
        transactionRepo.save(tx);

        return wallet;
    }

    // View Transaction History
    public List<Transaction> getTransactionHistory(int userId) {
        Wallet wallet = walletRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));
        return transactionRepo.findByWalletId(wallet.getWalletId());
    }
}
