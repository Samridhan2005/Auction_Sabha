package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
