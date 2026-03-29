package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public class TransactionRepository extends JpaRepository<Transaction, Integer> {
}
