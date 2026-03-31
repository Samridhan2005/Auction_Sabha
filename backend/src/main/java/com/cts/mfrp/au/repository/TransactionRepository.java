package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByWalletId(int walletId);
}
