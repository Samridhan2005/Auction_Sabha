package com.cts.mfrp.au.service;

import com.cts.mfrp.au.exception.WalletNoSufficientBalance;
import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.model.Wallet;
import com.cts.mfrp.au.model.Transaction;
import com.cts.mfrp.au.repository.ProductRepository;
import com.cts.mfrp.au.repository.WalletRepository;
import com.cts.mfrp.au.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WalletService {
    @Autowired
    private WalletRepository walletRepo;
    @Autowired
    private TransactionRepository transactionRepo;
    @Autowired private AuctionService auctionService;
    @Autowired private ProductRepository productRepository;

    public  WalletService(){}

    public WalletService(WalletRepository walletRepo, TransactionRepository transactionRepo) {
        this.walletRepo = walletRepo;
        this.transactionRepo = transactionRepo;
    }

    // AUC-23: View Balance — auto-creates wallet if missing (handles legacy accounts).
    // Uses try/catch for the creation to handle the race condition where two concurrent
    // requests (e.g. loadWallet + loadTransactions) both see no wallet and both try to insert.
    public Wallet getBalance(int userId) {
        return walletRepo.findByUserId(userId).orElseGet(() -> {
            try {
                Wallet wallet = new Wallet();
                wallet.setUserId(userId);
                wallet.setAvailableBalance(0);
                wallet.setFrozenBalance(0);
                wallet.setLastUpdated(new Date());
                return walletRepo.saveAndFlush(wallet);
            } catch (DataIntegrityViolationException e) {
                // Another concurrent request already created the wallet — just fetch it
                return walletRepo.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("Wallet creation failed for userId: " + userId));
            }
        });
    }

    // AUC-24: Deposit Funds
    public Wallet depositFunds(int userId, float amount) {
        Wallet wallet = getBalance(userId);

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

    // AUC-25: Withdraw Funds
    public Wallet withdrawFunds(int userId, float amount) {
        Wallet wallet = getBalance(userId);

        if (wallet.getAvailableBalance() < amount) {
            throw new WalletNoSufficientBalance("Insufficient balance to withdraw");
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

    // AUC-26: Hold Bid Amount
    public Wallet holdBidAmount(int userId, int auctionId, float bidAmount) {
        Wallet wallet = getBalance(userId);

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

    // AUC-27: Refund Losing Bidder
    public Wallet refundBidder(int userId, int auctionId, float refundAmount) {
        Wallet wallet = getBalance(userId);

        if (wallet.getFrozenBalance() < refundAmount) {
            throw new WalletNoSufficientBalance("No sufficient frozen balance to refund");
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

    // AUC-28: Payout Winning Seller
    public Wallet payoutSeller(int sellerUserId, int auctionId, float payoutAmount) {
        Wallet wallet = getBalance(sellerUserId);

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

    // AUC-29: View Transaction History
    public List<Transaction> getTransactionHistory(int userId) {
        Wallet wallet = getBalance(userId);
        return transactionRepo.findByWalletId(wallet.getWalletId());
    }

    public synchronized void freezeBal(int userId, float amount){
        Wallet wallet = getBalance(userId);
        wallet.setFrozenBalance(wallet.getFrozenBalance() + amount);
        wallet.setAvailableBalance(wallet.getAvailableBalance() - amount);
        walletRepo.save(wallet);
    }

    public synchronized void unfreezeBal(int userId, float amount){
        Wallet wallet = getBalance(userId);
        wallet.setFrozenBalance(wallet.getFrozenBalance() - amount);
        wallet.setAvailableBalance(wallet.getAvailableBalance() + amount);
        walletRepo.save(wallet);
    }
    public void commit(int auctionId){
        Auction a = auctionService.findById(auctionId);
        User winner = a.getHighestBidder();
        if (winner == null) return; // no bids were placed

        // Debit winner's frozen balance — only the winning bid, not their full frozen amount
        Wallet winnerWallet = getBalance(winner.getUserId());
        float payoutAmount = a.getCurrentBid();
        winnerWallet.setFrozenBalance(winnerWallet.getFrozenBalance() - payoutAmount);
        winnerWallet.setLastUpdated(new Date());

        Transaction debitTx = new Transaction();
        debitTx.setWalletId(winnerWallet.getWalletId());
        debitTx.setAuctionId(auctionId);
        debitTx.setType("bid_debit");
        debitTx.setAmount(payoutAmount);
        debitTx.setStatus("SUCCESS");
        debitTx.setCreatedAt(new Date());
        transactionRepo.save(debitTx);
        walletRepo.save(winnerWallet);

        // Credit seller's wallet and mark product as SOLD
        if (a.getProduct() != null) {
            if (a.getProduct().getSeller() != null) {
                payoutSeller(a.getProduct().getSeller().getUserId(), auctionId, payoutAmount);
            }
            a.getProduct().setVerificationStatus("SOLD");
            productRepository.save(a.getProduct());
        }
    }

}
