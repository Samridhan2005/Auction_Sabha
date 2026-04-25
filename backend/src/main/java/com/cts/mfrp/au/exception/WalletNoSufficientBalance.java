package com.cts.mfrp.au.exception;

public class WalletNoSufficientBalance extends RuntimeException {
    public WalletNoSufficientBalance(String message) {
        super(message);
    }
}
