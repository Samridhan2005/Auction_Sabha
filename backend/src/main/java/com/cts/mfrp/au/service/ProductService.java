package com.cts.mfrp.au.service;

import com.cts.mfrp.au.model.Product;
import com.cts.mfrp.au.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

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
    public List<Product> getByCategory(int catId) { return productRepository.findByCategory_CategoryIdAndVerificationStatus(catId, "APPROVED"); }
    public List<Product> searchProducts(String name) { return productRepository.findByProductNameContainingIgnoreCaseAndVerificationStatus(name, "APPROVED"); }
    public List<Product> getPendingForVerifier() { return productRepository.findByVerificationStatus("PENDING"); }
}