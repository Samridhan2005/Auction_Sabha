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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final int SLOT_HOURS       = 1;
    private static final int BUSINESS_START   = 9;   // 9 AM  (slot 1)
    private static final int BUSINESS_END     = 19;  // last slot starts at 18:00 (slot 10)

    @Autowired private ProductRepository  productRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private AuctionRepository  auctionRepository;
    @Autowired private EmailService       emailService;

    // ── Verifier: approve / reject ──────────────────────────────────────────
    public Product reviewProduct(int id, String status, String remarks) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setVerificationStatus(status);
        product.setAdminRemarks(remarks);
        Product saved = productRepository.save(product);

        String slotLabel = null;

        if ("APPROVED".equalsIgnoreCase(status)) {
            // Find & assign auction slot — honour seller's preferred slot if available
            auctionRepository.findByProduct_ProductId(product.getProductId()).ifPresent(auction -> {
                LocalDate preferred = product.getPreferredDate() != null
                        ? product.getPreferredDate()
                        : LocalDate.now().plusDays(1);
                LocalDateTime slotStart = resolveSlot(preferred, product.getPreferredSlot());
                auction.setConfirmedStartTime(slotStart);
                auction.setSlotEndTime(slotStart.plusHours(SLOT_HOURS));
                auction.setFeatured(true); // now visible in admin dashboard
                auctionRepository.save(auction);
            });

            // Get slot label for email
            slotLabel = auctionRepository.findByProduct_ProductId(product.getProductId())
                    .map(a -> a.getConfirmedStartTime() != null
                            ? a.getConfirmedStartTime().format(
                                DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : null)
                    .orElse(null);
        }

        // Email seller
        emailService.sendProductStatusEmail(
                product.getSeller().getEmail(),
                product.getSeller().getName(),
                product.getProductName(),
                status,
                remarks,
                slotLabel
        );

        return saved;
    }

    // ── Slot scheduling ──────────────────────────────────────────────────────

    /** Tries the seller's preferred slot first; falls back to next available. */
    private LocalDateTime resolveSlot(LocalDate preferred, int preferredSlot) {
        if (preferredSlot >= 1 && preferredSlot <= 10) {
            int hour = BUSINESS_START + (preferredSlot - 1); // slot 1=09:00, slot 10=18:00
            LocalDateTime start = preferred.atTime(hour, 0);
            LocalDateTime end   = start.plusHours(SLOT_HOURS);
            if (auctionRepository.findConflictingSlots(start, end).isEmpty()) {
                return start;
            }
        }
        return findNextAvailableSlot(preferred);
    }

    private LocalDateTime findNextAvailableSlot(LocalDate from) {
        for (int day = 0; day < 14; day++) {
            LocalDate date = from.plusDays(day);
            for (int hour = BUSINESS_START; hour < BUSINESS_END; hour++) {
                LocalDateTime start = date.atTime(hour, 0);
                LocalDateTime end   = start.plusHours(SLOT_HOURS);
                if (auctionRepository.findConflictingSlots(start, end).isEmpty()) {
                    return start;
                }
            }
        }
        // Absolute fallback: 14 days from preferred date at 9 AM
        return from.plusDays(14).atTime(BUSINESS_START, 0);
    }

    // ── Bidding validation ────────────────────────────────────────────────────
    public String validateAndPlaceBid(int id, float bidAmount) {
        Product product = productRepository.findById(id).orElseThrow();
        if (bidAmount <= product.getStartingPrice()) {
            return "Error: Bid must be higher than the price: " + product.getStartingPrice();
        }
        return "Success: Bid placed successfully!";
    }

    // ── Browse & search ───────────────────────────────────────────────────────
    public List<Product> getAllApproved()         { return productRepository.findByVerificationStatusOrderBySubmittedAtDesc("APPROVED"); }
    public List<Product> getByCategory(int catId) { return productRepository.findByCategory_CategoryIdAndVerificationStatus(catId, "APPROVED"); }
    public List<Product> searchProducts(String n) { return productRepository.findByProductNameContainingIgnoreCaseAndVerificationStatus(n, "APPROVED"); }
    public List<Product> getPendingForVerifier()  { return productRepository.findByVerificationStatus("PENDING"); }

    // ── Seller Dashboard ─────────────────────────────────────────────────────
    public List<Map<String, Object>> getSellerProducts(int sellerId) {
        List<Product> products = productRepository.findBySeller_UserIdOrderBySubmittedAtDesc(sellerId);
        return products.stream().map(p -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("productId", p.getProductId());
            row.put("productName", p.getProductName());
            row.put("description", p.getDescription());
            row.put("imageUrl", p.getImageUrl());
            row.put("startingPrice", p.getStartingPrice());
            row.put("verificationStatus", p.getVerificationStatus());
            row.put("adminRemarks", p.getAdminRemarks());
            row.put("submittedAt", p.getSubmittedAt() != null ? p.getSubmittedAt().toString() : null);
            row.put("categoryName", p.getCategory() != null ? p.getCategory().getCategoryName() : "Uncategorized");

            auctionRepository.findByProduct_ProductId(p.getProductId()).ifPresentOrElse(
                auction -> {
                    row.put("auctionId", auction.getAuctionId());
                    row.put("auctionStatus", auction.getStatus());
                    row.put("finalBid", auction.getCurrentBid());
                    row.put("winner", auction.getHighestBidder() != null
                            ? auction.getHighestBidder().getName() : null);
                    row.put("profit", "ENDED".equals(auction.getStatus())
                            ? auction.getCurrentBid() - p.getStartingPrice() : 0);
                    row.put("endTime", auction.getEndTime() != null
                            ? auction.getEndTime().toString() : null);
                },
                () -> {
                    row.put("auctionId", null);
                    row.put("auctionStatus", null);
                    row.put("finalBid", p.getStartingPrice());
                    row.put("winner", null);
                    row.put("profit", 0);
                    row.put("endTime", null);
                }
            );
            return row;
        }).collect(Collectors.toList());
    }

    // ── Admin Dashboard: all products + linked auction details ───────────────
    public List<Map<String, Object>> getAllProductsForAdmin() {
        List<Product> products = productRepository.findAllByOrderBySubmittedAtDesc();

        // Build productId → Auction map in one query (avoids N+1)
        Map<Integer, Auction> auctionByProductId = new java.util.HashMap<>();
        for (Auction a : auctionRepository.findAllWithProduct()) {
            if (a.getProduct() != null) {
                auctionByProductId.put(a.getProduct().getProductId(), a);
            }
        }

        return products.stream().map(p -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("productId",          p.getProductId());
            row.put("productName",        p.getProductName());
            row.put("description",        p.getDescription());
            String img = p.getImageUrl();
            row.put("imageUrl",           img != null && !img.isBlank() ? img : null);
            row.put("startingPrice",      p.getStartingPrice());
            row.put("verificationStatus", p.getVerificationStatus());
            row.put("adminRemarks",       p.getAdminRemarks());
            row.put("submittedAt",        p.getSubmittedAt()   != null ? p.getSubmittedAt().toString()   : null);
            row.put("preferredDate",      p.getPreferredDate() != null ? p.getPreferredDate().toString() : null);
            row.put("preferredSlot",      p.getPreferredSlot());
            row.put("categoryName",       p.getCategory() != null ? p.getCategory().getCategoryName() : "Uncategorized");
            row.put("sellerName",         p.getSeller()   != null ? p.getSeller().getName()           : "Unknown");

            Auction a = auctionByProductId.get(p.getProductId());
            if (a != null) {
                row.put("auctionId",           a.getAuctionId());
                row.put("auctionStatus",        a.getStatus());
                row.put("currentBid",           a.getCurrentBid());
                row.put("highestBidder",        a.getHighestBidder() != null ? a.getHighestBidder().getName() : null);
                row.put("confirmedStartTime",   a.getConfirmedStartTime() != null ? a.getConfirmedStartTime().toString() : null);
                row.put("slotEndTime",          a.getSlotEndTime()        != null ? a.getSlotEndTime().toString()        : null);
                row.put("startTime",            a.getStartTime()          != null ? a.getStartTime().toString()          : null);
                row.put("endTime",              a.getEndTime()            != null ? a.getEndTime().toString()            : null);
            } else {
                row.put("auctionId", null);  row.put("auctionStatus", null);
                row.put("currentBid", p.getStartingPrice()); row.put("highestBidder", null);
                row.put("confirmedStartTime", null); row.put("slotEndTime", null);
                row.put("startTime", null);  row.put("endTime", null);
            }
            return row;
        }).collect(Collectors.toList());
    }

    // ── Submit product ────────────────────────────────────────────────────────
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
        product.setDocumentsUrl(request.getDocumentsUrl());
        product.setStartingPrice(request.getStartingPrice());
        product.setPreferredDate(request.getPreferredDate());
        product.setPreferredSlot(request.getPreferredSlot());
        product.setVerificationStatus("PENDING");
        product.setSubmittedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);

        Auction auction = new Auction();
        auction.setProduct(saved);
        auction.setCurrentBid(saved.getStartingPrice());
        auction.setStatus("CREATED");
        auction.setFeatured(false); // hidden until verifier approves
        auctionRepository.save(auction);

        return saved;
    }
}
