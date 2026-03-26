package com.cts.repository;

import com.cts.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public class TransactionRepository extends JpaRepository<Transaction, Integer> {
}
