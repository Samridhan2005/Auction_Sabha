package com.cts.mfrp.au.exception;

public class AuctionCannotStartException extends RuntimeException {
    public AuctionCannotStartException(String message) {
        super(message);
    }
}
