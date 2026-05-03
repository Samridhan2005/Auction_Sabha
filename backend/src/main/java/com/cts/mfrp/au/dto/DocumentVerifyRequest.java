package com.cts.mfrp.au.dto;

import lombok.Data;

@Data
public class DocumentVerifyRequest {
    private String documentUrl;
    private String productName;
    private String description;
}
