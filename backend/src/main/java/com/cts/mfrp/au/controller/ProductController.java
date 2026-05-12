package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.dto.AiRiskResponse;
import com.cts.mfrp.au.dto.ProductSubmitRequest;
import com.cts.mfrp.au.model.Category;
import com.cts.mfrp.au.model.Product;
import com.cts.mfrp.au.repository.CategoryRepository;
import com.cts.mfrp.au.repository.ProductRepository;
import com.cts.mfrp.au.service.AiRiskService;
import com.cts.mfrp.au.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AiRiskService aiRiskService;

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    // 0. All approved products
    @GetMapping("/all")
    public List<Product> getAll() {
        return productService.getAllApproved();
    }

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

    // 6. Seller Dashboard: All products submitted by a seller with auction results
    @GetMapping("/my-products")
    public ResponseEntity<List<Map<String, Object>>> getMyProducts(@RequestParam int sellerId) {
        return ResponseEntity.ok(productService.getSellerProducts(sellerId));
    }

    @PostMapping("/submit")
    public ResponseEntity<Product> submitProduct(
            @RequestParam("sellerId") int sellerId,
            @RequestBody ProductSubmitRequest request) {

        Product savedProduct = productService.submitProduct(sellerId, request.getCategoryId(), request);
        return ResponseEntity.ok(savedProduct);
    }

    // 7. AI Risk Check (on-demand, no caching)
    @PostMapping("/{id}/ai-check")
    public ResponseEntity<AiRiskResponse> aiCheck(@PathVariable int id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.status(404)
                    .body(AiRiskResponse.withError("Product not found."));
        }
        AiRiskResponse result = aiRiskService.analyseProduct(product);
        return ResponseEntity.ok(result);
    }

}