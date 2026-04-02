package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.model.Product;
import com.cts.mfrp.au.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. Browse by Category
    @GetMapping("/category/{id}")
    public List<Product> getByCategory(@PathVariable int id) {
        return productService.getByCategory(id);
    }

    // 2. Search
    @GetMapping("/search")
    public List<Product> search(@RequestParam String name) {
        return productService.searchProducts(name);
    }

    // 3. Verifier Dashboard (Get all Pending)
    @GetMapping("/pending")
    public List<Product> getPending() {
        return productService.getPendingForVerifier();
    }

    // 4. Verifier Action: Approve/Reject
    @PutMapping("/review/{id}")
    public ResponseEntity<Product> review(@PathVariable int id,
                                          @RequestParam String status,
                                          @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(productService.reviewProduct(id, status, remarks));
    }

    // 5. Bidding Validation
    @PostMapping("/bid/{id}")
    public ResponseEntity<String> placeBid(@PathVariable int id, @RequestParam float amount) {
        String response = productService.validateAndPlaceBid(id, amount);
        if(response.startsWith("Error")) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }
}