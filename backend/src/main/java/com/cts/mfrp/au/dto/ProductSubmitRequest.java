package com.cts.mfrp.au.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ProductSubmitRequest {
    private String productName;
    private String description;
    private String imageUrl;
    private float startingPrice;
    private int categoryId;
}

