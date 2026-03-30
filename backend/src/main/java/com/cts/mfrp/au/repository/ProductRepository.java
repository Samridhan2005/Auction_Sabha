package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // 1. Browse by category (AUC-7)
    List<Product> findByCategory_CategoryIdAndVerificationStatus(int categoryId, String status);

    // 2. Search by name (AUC-6)
    List<Product> findByProductNameContainingIgnoreCaseAndVerificationStatus(String name, String status);

    // 3. Verifier Dashboard: All PENDING products
    List<Product> findByVerificationStatus(String status);

    // 4. Popular/Latest: Since we don't have bidCount, we show latest submitted items
    List<Product> findByVerificationStatusOrderBySubmittedAtDesc(String status);
}