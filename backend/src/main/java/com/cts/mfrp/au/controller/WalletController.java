package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.model.Wallet;
import com.cts.mfrp.au.model.Transaction;
import com.cts.mfrp.au.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{userId}")
    public Wallet getBalance(@PathVariable int userId) {
        return walletService.getBalance(userId);
    }

    @PostMapping("/{userId}/deposit")
    public Wallet deposit(@PathVariable int userId, @RequestBody Map<String, Float> body) {
        float amount = body.get("amount");
        return walletService.depositFunds(userId, amount);
    }

    @PostMapping("/{userId}/withdraw")
    public Wallet withdraw(@PathVariable int userId, @RequestBody Map<String, Float> body) {
        float amount = body.get("amount");
        return walletService.withdrawFunds(userId, amount);
    }

    @PostMapping("/{userId}/holdBid")
    public Wallet holdBid(@PathVariable int userId, @RequestBody Map<String, Object> body) {
        int auctionId = (Integer) body.get("auctionId");
        float bidAmount = ((Number) body.get("bidAmount")).floatValue();
        return walletService.holdBidAmount(userId, auctionId, bidAmount);
    }

    @PostMapping("/{userId}/refund")
    public Wallet refund(@PathVariable int userId, @RequestBody Map<String, Object> body) {
        int auctionId = (Integer) body.get("auctionId");
        float refundAmount = ((Number) body.get("refundAmount")).floatValue();
        return walletService.refundBidder(userId, auctionId, refundAmount);
    }

    @PostMapping("/{sellerUserId}/payout")
    public Wallet payout(@PathVariable int sellerUserId, @RequestBody Map<String, Object> body) {
        int auctionId = (Integer) body.get("auctionId");
        float payoutAmount = ((Number) body.get("payoutAmount")).floatValue();
        return walletService.payoutSeller(sellerUserId, auctionId, payoutAmount);
    }

    @GetMapping("/{userId}/transactions")
    public List<Transaction> getTransactions(@PathVariable int userId) {
        return walletService.getTransactionHistory(userId);
    }
}
