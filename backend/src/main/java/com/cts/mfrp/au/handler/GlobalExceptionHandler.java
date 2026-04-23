package com.cts.mfrp.au.handler;

import com.cts.mfrp.au.exception.AuctionCannotStartException;
import com.cts.mfrp.au.exception.AuctionCannotStopException;
import com.cts.mfrp.au.exception.AuctionNotFoundException;
import com.cts.mfrp.au.exception.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<String> handleWalletNotFound(WalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AuctionNotFoundException.class)
    public ResponseEntity<String> handleAuctionNotFound(AuctionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AuctionCannotStartException.class)
    public ResponseEntity<String> handleAuctionCannotStart(AuctionCannotStartException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(AuctionCannotStopException.class)
    public ResponseEntity<String> handleAuctionCannotStop(AuctionCannotStopException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }


}
