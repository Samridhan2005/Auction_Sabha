package com.cts.repository;

import com.cts.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public class ProductRepository extends JpaRepository<Product, Integer> {
}
