package com.cts.mfrp.au.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String productName;
    private String description;
    private String imageUrl;
    private String documentsUrl;
    private float startingPrice;
    private String verificationStatus;
    private String adminRemarks;
    private java.time.LocalDateTime submittedAt;
    private java.time.LocalDate preferredDate;

    private int preferredSlot; // 1-10

    @Column(length = 30)
    private String aiVerdict;

    @Column(length = 512)
    private String aiVerdictSummary;

    public String getAiVerdict() { return aiVerdict; }
    public void setAiVerdict(String aiVerdict) { this.aiVerdict = aiVerdict; }
    public String getAiVerdictSummary() { return aiVerdictSummary; }
    public void setAiVerdictSummary(String aiVerdictSummary) { this.aiVerdictSummary = aiVerdictSummary; }

    // Getter and Setter for Starting Price (Fixed the error you see now)
    public float getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(float startingPrice) {
        this.startingPrice = startingPrice;
    }

    // Getter and Setter for Verification Status
    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    // Getter and Setter for Admin Remarks
    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }

    // Optional: Product Name and Category Getters (For Search/Filter logic)
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}