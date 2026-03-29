package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {
}
