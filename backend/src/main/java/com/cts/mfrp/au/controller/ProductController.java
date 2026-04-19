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

    @PostMapping("/add")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    @GetMapping("/category/{id}")
    public List<Product> getByCategory(@PathVariable int id) {
        return productService.getByCategory(id);
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam String name) {
        return productService.searchProducts(name);
    }

    @GetMapping("/pending")
    public List<Product> getPending() {
        return productService.getPendingForVerifier();
    }

    @PutMapping("/review/{id}")
    public ResponseEntity<Product> review(@PathVariable int id,
                                          @RequestParam String status,
                                          @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(productService.reviewProduct(id, status, remarks));
    }

//    @PostMapping("/bid/{id}")
//    public ResponseEntity<String> placeBid(@PathVariable int id, @RequestParam float amount) {
//        String response = productService.validateAndPlaceBid(id, amount);
//        if(response.startsWith("Error")) return ResponseEntity.badRequest().body(response);
//        return ResponseEntity.ok(response);
//    }
}