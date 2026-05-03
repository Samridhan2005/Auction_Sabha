package com.cts.mfrp.au.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProductSubmitRequest {
    private String productName;
    private String description;
    private String imageUrl;
    private String documentsUrl;
    private float startingPrice;
    private int categoryId;
    private LocalDate preferredDate;
    private int preferredSlot; // 1-10 (slot 1 = 09:00, slot 10 = 18:00)
}
