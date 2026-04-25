package com.cts.mfrp.au.service;

import com.cts.mfrp.au.dto.ProductSubmitRequest;
import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.model.Category;
import com.cts.mfrp.au.model.Product;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.repository.AuctionRepository;
import com.cts.mfrp.au.repository.CategoryRepository;
import com.cts.mfrp.au.repository.ProductRepository;
import com.cts.mfrp.au.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    // AS-PV-03: Verifier Approve/Reject logic
    public Product reviewProduct(int id, String status, String remarks) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setVerificationStatus(status);
        product.setAdminRemarks(remarks);
        return productRepository.save(product);
    }

    // Buyer Bidding Logic (Validation only)
    public String validateAndPlaceBid(int id, float bidAmount) {
        Product product = productRepository.findById(id).orElseThrow();

        // Validation: Bid must be higher than starting price
        if (bidAmount <= product.getStartingPrice()) {
            return "Error: Bid must be higher than the price: " + product.getStartingPrice();
        }

        // Note: Real bidding usually updates a 'current_bid' column or separate table.
        // For now, we return success if validation passes.
        return "Success: Bid placed successfully!";
    }

    // Browse & Search methods
    public List<Product> getAllApproved() { return productRepository.findByVerificationStatusOrderBySubmittedAtDesc("APPROVED"); }
    public List<Product> getByCategory(int catId) { return productRepository.findByCategory_CategoryIdAndVerificationStatus(catId, "APPROVED"); }
    public List<Product> searchProducts(String name) { return productRepository.findByProductNameContainingIgnoreCaseAndVerificationStatus(name, "APPROVED"); }
    public List<Product> getPendingForVerifier() { return productRepository.findByVerificationStatus("PENDING"); }

    public Product submitProduct(int sellerId, int categoryId, ProductSubmitRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setStartingPrice(request.getStartingPrice());
        product.setVerificationStatus("PENDING");
        product.setSubmittedAt(java.time.LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        Auction auction = new Auction();
        auction.setProduct(savedProduct);
        auction.setCurrentBid(savedProduct.getStartingPrice());
        auction.setStatus("CREATED");
        auction.setFeatured(true);
        auctionRepository.save(auction);

        return savedProduct;
    }
}