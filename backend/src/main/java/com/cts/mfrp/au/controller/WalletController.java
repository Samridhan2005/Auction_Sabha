package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.model.Wallet;
import com.cts.mfrp.au.model.Transaction;
import com.cts.mfrp.au.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // AUC-23: View wallet balance details
    @GetMapping("/{userId}")
    public Wallet getBalance(@PathVariable int userId) {
        return walletService.getBalance(userId);
    }

    // AUC-24: Deposit funds into wallet securely
    @PostMapping("/{userId}/deposit")
    public Wallet deposit(@PathVariable int userId, @RequestBody Map<String, Float> body) {
        float amount = body.get("amount");
        return walletService.depositFunds(userId, amount);
    }

    // AUC-25: Withdraw funds from wallet safely
    @PostMapping("/{userId}/withdraw")
    public Wallet withdraw(@PathVariable int userId, @RequestBody Map<String, Float> body) {
        float amount = body.get("amount");
        return walletService.withdrawFunds(userId, amount);
    }

    // AUC-26: Hold bid amount during auction
    @PostMapping("/{userId}/holdBid")
    public Wallet holdBid(@PathVariable int userId, @RequestBody Map<String, Object> body) {
        int auctionId = (Integer) body.get("auctionId");
        float bidAmount = ((Number) body.get("bidAmount")).floatValue();
        return walletService.holdBidAmount(userId, auctionId, bidAmount);
    }

    // AUC-27: Refund losing bidder’s frozen funds
    @PostMapping("/{userId}/refund")
    public Wallet refund(@PathVariable int userId, @RequestBody Map<String, Object> body) {
        int auctionId = (Integer) body.get("auctionId");
        float refundAmount = ((Number) body.get("refundAmount")).floatValue();
        return walletService.refundBidder(userId, auctionId, refundAmount);
    }

    // AUC-28: Payout winning seller after auction
    @PostMapping("/{sellerUserId}/payout")
    public Wallet payout(@PathVariable int sellerUserId, @RequestBody Map<String, Object> body) {
        int auctionId = (Integer) body.get("auctionId");
        float payoutAmount = ((Number) body.get("payoutAmount")).floatValue();
        return walletService.payoutSeller(sellerUserId, auctionId, payoutAmount);
    }

    // AUC-29: View complete transaction history list
    @GetMapping("/{userId}/transactions")
    public List<Transaction> getTransactions(@PathVariable int userId) {
        return walletService.getTransactionHistory(userId);
    }
}
