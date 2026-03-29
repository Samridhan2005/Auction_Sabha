package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
